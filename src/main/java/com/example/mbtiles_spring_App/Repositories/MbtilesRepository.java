package com.example.mbtiles_spring_App.Repositories;

import com.example.mbtiles_spring_App.DTO.TileResponse;
import jakarta.annotation.PostConstruct;
import org.imintel.mbtiles4j.*;
import org.imintel.mbtiles4j.model.MetadataBounds;
import org.imintel.mbtiles4j.model.MetadataEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MbtilesRepository {

    private static final Logger log = LoggerFactory.getLogger(MbtilesRepository.class);
    private static final int MB = 1048576;

    @Value("${mbtiles.upload.dir}")
    private String uploadDir;

    @Value("${mbtiles.max-file-size}")
    private long maxFileSize;

    private final Map<String, MBTilesReader> loadedMaps = new ConcurrentHashMap<>();


    @PostConstruct
    public void scanningForMaps() throws MBTilesReadException {
        log.info("Сканирование директории '{}' на наличие .mbtiles файлов...", uploadDir);
        File[] files = new File(uploadDir).listFiles();
        if (files == null) {
            log.warn("Директория '{}' не существует или пуста.", uploadDir);
            return;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".mbtiles")) continue;
            String originalName = file.getName();
            String formattedName = originalName.substring(0, originalName.indexOf(".")) + "!" + System.currentTimeMillis();
            loadedMaps.put(formattedName, new MBTilesReader(file));
            log.info("Файл '{}' загружен как '{}'", originalName, formattedName);
        }
        log.info("Сканирование завершено. Загружено {} карт.", loadedMaps.size());
    }

    public void save(MultipartFile file, String fileName) throws IOException, MBTilesReadException {
        if (file.getSize() > (maxFileSize * MB)) {
            log.warn("Файл '{}' превышает максимальный размер: {} байт", file.getOriginalFilename(), file.getSize());
            throw new IOException("File size exceeds limit");
        }

        if (file.getOriginalFilename() != null && !file.getOriginalFilename().endsWith(".mbtiles")) {
            log.warn("Файл '{}' имеет неверный формат", file.getOriginalFilename());
            throw new IOException("Invalid file format");
        }

        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("Не удалось создать директорию '{}'", uploadDir);
            throw new IOException("Failed to create upload directory");
        }

        String systemFileName = fileName + "#" + System.currentTimeMillis();
        String filePath = uploadDir + File.separator + systemFileName + ".mbtiles";

        File newFile = new File(filePath);
        file.transferTo(newFile);

        if (!newFile.exists()) {
            log.error("Файл '{}' не был сохранён", filePath);
            throw new IOException("Failed to save file");
        }

        loadedMaps.putIfAbsent(systemFileName, new MBTilesReader(newFile));
        log.info("Файл '{}' успешно загружен как '{}'", file.getOriginalFilename(), systemFileName);
    }

    public Optional<List<String>> getMapsList() {
        log.debug("Запрос списка загруженных карт");
        return Optional.of(new ArrayList<>(loadedMaps.keySet()));
    }

    public Optional<MetadataBounds> getTileSetBounds(String mapName) {
        return Optional.ofNullable(loadedMaps.get(mapName)).map(reader -> {
            log.debug("Получение границ тайлов для карты '{}'", mapName);
            try {
                return reader.getMetadata().getTilesetBounds();
            } catch (MBTilesReadException e) {
                throw new RuntimeException("Границ тайлов для карты не найдены");
            }
        });
    }

    public Optional<String> getAttribution(String mapName) {
        return Optional.ofNullable(loadedMaps.get(mapName)).map(reader -> {
            log.debug("Получение атрибуции для карты '{}'", mapName);
            try {
                return reader.getMetadata().getAttribution();
            } catch (MBTilesReadException e) {
                throw new RuntimeException("Атрибуции для карты не найдены ");
            }
        });
    }

    public Optional<String> getTileSetName(String mapName) {
        return Optional.ofNullable(loadedMaps.get(mapName)).map(reader -> {
            log.debug("Получение имени тайлсета карты '{}'", mapName);
            try {
                return reader.getMetadata().getTilesetName();
            } catch (MBTilesReadException e) {
                throw new RuntimeException("Имя тайлсета карты не найдено");
            }
        });
    }

    public Optional<MetadataEntry.TileMimeType> getTileMimeType(String mapName) {
        return Optional.ofNullable(loadedMaps.get(mapName)).map(reader -> {
            log.debug("Получение типа тайла для карты '{}'", mapName);
            try {
                return reader.getMetadata().getTileMimeType();
            } catch (MBTilesReadException e) {
                throw new RuntimeException("Тип тайла карты не найден");
            }
        });
    }

    public Optional<Integer> getMinZoom(String mapName) {
        return Optional.ofNullable(loadedMaps.get(mapName)).map(reader -> {
            log.debug("Получение минимального масштаба для карты '{}'", mapName);
            try {
                return reader.getMinZoom();
            } catch (MBTilesReadException e) {
                throw new RuntimeException("Минимальный маштаб карты не найден");
            }
        });
    }

    public Optional<Integer> getMaxZoom(String mapName) {
        return Optional.ofNullable(loadedMaps.get(mapName)).map(reader -> {
            log.debug("Получение максимального масштаба для карты '{}'", mapName);
            try {
                return reader.getMaxZoom();
            } catch (MBTilesReadException e) {
                throw new RuntimeException("Максимальный масштаб карты не найден");
            }
        });
    }

    public Optional<TileResponse> getMbtiles(int z, int x, int y) throws MBTilesReadException, IOException , NumberFormatException {
        for (Map.Entry<String, MBTilesReader> entry : loadedMaps.entrySet()) {
            Tile tile = entry.getValue().getTile(z, x, y);
            if (tile != null) {
                InputStream is = tile.getData();
                log.debug("Найден тайл по координатам z={}, x={}, y={} в карте '{}'", z, x, y, entry.getKey());
                TileResponse tileResponse = new TileResponse(is.readAllBytes(), entry.getValue().getMetadata().getTileMimeType().toString());
                is.close();
                return Optional.of(tileResponse);
            }
        }
        log.warn("Тайл по координатам z={}, x={}, y={} не найден", z, x, y);
        return Optional.empty();
    }

    public Optional<TileResponse> getMbtiles(String mapName, int z, int x, int y) throws MBTilesReadException, IOException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) {
            log.warn("Карта '{}' не найдена", mapName);
            throw new MBTilesReadException(new RuntimeException("Карта не найдена"));
        }

        try (InputStream is = mbTilesReader.getTile(z, x, y).getData()) {
            log.debug("Получение тайла карты '{}' по координатам z={}, x={}, y={}", mapName, z, x, y);
            return Optional.of(new TileResponse(is.readAllBytes(), mbTilesReader.getMetadata().getTileMimeType().toString()));
        }
    }

    public void close(String mapName) {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) {
            log.warn("Попытка закрыть несуществующую карту '{}'", mapName);
            return;
        }
        mbTilesReader.close();
        loadedMaps.remove(mapName);
        log.info("Карта '{}' успешно закрыта и удалена из памяти", mapName);
    }

    public void closeAll() {
        log.info("Закрытие всех карт...");
        loadedMaps.forEach((name, reader) -> {
            try {
                reader.close();
                log.info("Карта '{}' успешно закрыта", name);
            } catch (Exception e) {
                log.error("Ошибка при закрытии карты '{}': {}", name, e.getMessage(), e);
            }
        });
        loadedMaps.clear();
        log.info("Все карты закрыты и очищены из памяти.");
    }
}
