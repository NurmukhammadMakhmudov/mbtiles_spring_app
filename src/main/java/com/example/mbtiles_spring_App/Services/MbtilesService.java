package com.example.mbtiles_spring_App.Services;

import com.example.mbtiles_spring_App.DTO.MapInfoResponse;
import com.example.mbtiles_spring_App.DTO.TileResponse;
import com.example.mbtiles_spring_App.Repositories.MbtilesRepository;
import org.imintel.mbtiles4j.MBTilesReadException;
import org.imintel.mbtiles4j.model.MetadataBounds;
import org.imintel.mbtiles4j.model.MetadataEntry.TileMimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MbtilesService {

    private static final Logger logger = LoggerFactory.getLogger(MbtilesService.class);

    private final MbtilesRepository mbtilesRepository;

    @Autowired
    public MbtilesService(MbtilesRepository mbtilesRepository) {
        this.mbtilesRepository = mbtilesRepository;
    }

    public void saveMap(MultipartFile file, String fileName) throws MBTilesReadException, IOException {
        logger.info("Сохраняю карту с именем файла: {}", fileName);
        mbtilesRepository.save(file, fileName);
        logger.info("Карта {} успешно сохранена", fileName);
    }

    public TileResponse getTile(String mapName, int z, int x, int y) throws MBTilesReadException, IOException {
        logger.info("Запрос тайла для карты: {} на уровне z={}, x={}, y={}", mapName, z, x, y);
        if (mapName != null && !mapName.isBlank()) {
            return getTileWithMapName(mapName, z, x, y);
        }
        return getTileWithoutMapName(z, x, y);
    }

    public TileResponse getTileWithoutMapName(int z, int x, int y) throws MBTilesReadException, IOException, NullPointerException {
        validateCoordinates(z, x, y);
        logger.debug("Запрос тайла без имени карты на уровне z={}, x={}, y={}", z, x, y);

        TileResponse tile = mbtilesRepository.getMbtiles(z, x, y)
                .orElseThrow(() -> new MBTilesReadException(String.format("Карта не найдена на уровне z=%d, x=%d, y=%d", z, x, y), new RuntimeException()));

        if (tile.getData() == null) {
            logger.error("Тайл не найден на уровне z={}, x={}, y={}", z, x, y);
            throw new MBTilesReadException(String.format("Тайл не найден на уровне z=%d, x=%d, y=%d", z, x, y), new RuntimeException());
        }

        logger.debug("Тайл найден на уровне z={}, x={}, y={}", z, x, y);
        return tile;
    }

    public TileResponse getTileWithMapName(String mapName, int z, int x, int y) throws MBTilesReadException, IOException {
        logger.debug("Запрос тайла для карты: {} на уровне z={}, x={}, y={}", mapName, z, x, y);

        if (!mapExist(mapName)) {
            logger.error("Карта не найдена: {}", mapName);
            throw new MBTilesReadException("Карта не найдена: " + mapName, new RuntimeException());
        }

        validateCoordinates(z, x, y);

        TileResponse tile = mbtilesRepository.getMbtiles(mapName, z, x, y)
                .orElseThrow(() -> new MBTilesReadException(new RuntimeException(String.format("Тайл не найден на уровне z=%d, x=%d, y=%d,", z, x, y))));

        if (tile.getData() == null) {
            logger.error("Тайл не найден на уровне z={}, x={}, y={}", z, x, y);
            throw new MBTilesReadException(String.format("Тайл не найден на уровне z=%d, x=%d, y=%d", z, x, y), new RuntimeException());
        }

        logger.debug("Тайл найден на уровне z={}, x={}, y={}", z, x, y);
        return tile;
    }

    private boolean mapExist(String mapName) {
        return mapName != null && mbtilesRepository.getMapsList().orElse(List.of()).contains(mapName);
    }

    public List<String> getAllMapsNames() {
        logger.info("Запрашиваю все имена карт");
        return mbtilesRepository.getMapsList().orElse(List.of());
    }

    public MapInfoResponse getMapInfo(String mapName) throws MBTilesReadException {
        logger.info("Запрашиваю информацию о карте: {}", mapName);
        return mapInfoResponseBuilder(mapName);
    }

    private MapInfoResponse mapInfoResponseBuilder(String mapName) throws MBTilesReadException {
        logger.debug("Строю ответ с информацией о карте: {}", mapName);
        if (!mapExist(mapName)) throw new MBTilesReadException("Не правильное имя карты", new RuntimeException());
        String name = mbtilesRepository.getTileSetName(mapName).orElse(null);
        String attribution = mbtilesRepository.getAttribution(mapName).orElse(null);
        String tileSetName = mbtilesRepository.getTileSetName(mapName).orElse(null);
        TileMimeType tileMimeType = mbtilesRepository.getTileMimeType(mapName).orElse(null);
        int minZoom = mbtilesRepository.getMinZoom(mapName).orElse(-1);
        int maxZoom = mbtilesRepository.getMaxZoom(mapName).orElse(-1);
        MetadataBounds metadataBounds = mbtilesRepository.getTileSetBounds(mapName).orElse(null);
        double minLon = metadataBounds != null ? metadataBounds.getLeft() : 0;
        double minLat = metadataBounds != null ? metadataBounds.getBottom() : 0;
        double maxLon = metadataBounds != null ? metadataBounds.getRight() : 0;
        double maxLat = metadataBounds != null ? metadataBounds.getTop() : 0;

        MapInfoResponse mapInfoResponse = new MapInfoResponse(name, attribution, tileSetName,
                tileMimeType != null ? tileMimeType.toString() : "", minZoom, maxZoom, minLon, maxLon, minLat, maxLat);

        logger.debug("Информация о карте для: {} построена", mapName);
        return mapInfoResponse;
    }

    public void closeConnection(String mapName) throws MBTilesReadException {
        logger.info("Закрываю соединение для карты: {}", mapName);
        if (mapExist(mapName)) {
            mbtilesRepository.close(mapName);
            logger.info("Соединение для карты {} закрыто", mapName);
        } else if (mapName == null) {
            mbtilesRepository.closeAll();
            logger.info("Все соединения с картами закрыты");
        } else {
            logger.error("Карта не найдена: {}", mapName);
            throw new MBTilesReadException("Карта не найдена: " + mapName, new RuntimeException());
        }
    }

    private void validateCoordinates(int z, int x, int y) {
        if (z < 0 || x < 0 || y < 0) {
            logger.error("Некорректные координаты z={}, x={}, y={}", z, x, y);
            throw new IllegalArgumentException("Координаты должны быть положительными");
        }
    }
}
