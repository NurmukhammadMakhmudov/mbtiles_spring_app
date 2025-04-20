package com.example.mbtiles_spring_App.Repositories;


import com.example.mbtiles_spring_App.DTO.TileResponse;
import org.imintel.mbtiles4j.*;
import org.imintel.mbtiles4j.model.MetadataBounds;
import org.imintel.mbtiles4j.model.MetadataEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Repository
public class MbtilesRepository {

    private final static int MB = 1048576;

    @Value("${mbtiles.upload.dir}")
    private String uploadDir;

    @Value("${mbtiles.max-file-size}")
    private long maxFileSize;



    private final HashMap<String, MBTilesReader> loadedMaps = new HashMap<>();

    public MbtilesRepository() throws MBTilesReadException, MBTilesWriteException {
        File[] files = new File("src/main/resources/mbtiles/").listFiles();
        for (File file : files) {
            if (!file.getName().endsWith(".mbtiles")) continue;
            MBTilesReader mbTilesReader = new MBTilesReader(file);
            String fileName = file.getName();
            String fileFormattedName = fileName.substring(0, fileName.indexOf(".")) + "!" + System.currentTimeMillis();
            loadedMaps.put(fileFormattedName, mbTilesReader);
        }
    }

    public void save(MultipartFile file, String fileName) throws MBTilesReadException, IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (file.getSize() > (maxFileSize * MB)) throw new IOException("File size exceeds limit");
        if (!file.getOriginalFilename().endsWith(".mbtiles")) throw new IOException("Invalid file format");

        String systemFileName = fileName + "#" + System.currentTimeMillis();

        String filePath = uploadDir + File.separator + systemFileName + ".mbtiles";

        File newFile = new File(filePath);
        file.transferTo(newFile);
        if (!new File(filePath).exists()) {
            throw new IOException("Failed to save file");
        }
        loadedMaps.putIfAbsent(systemFileName, new MBTilesReader(new File(filePath)));
    }

    public Optional<List<String>> getMapsList() {

        return Optional.of(loadedMaps.keySet().stream().toList());
    }

    public Optional<MetadataBounds> getTileSetBounds(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of(mbTilesReader.getMetadata().getTilesetBounds());
    }
    public Optional<String> getAttribution(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of(mbTilesReader.getMetadata().getAttribution());
    }

    public Optional<String> getTileSetName(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of(mbTilesReader.getMetadata().getTilesetName());
    }
    public Optional<MetadataEntry.TileMimeType> getTileMimeType(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of(mbTilesReader.getMetadata().getTileMimeType());
    }
    public Optional<String> getTileSetVersion(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of(mbTilesReader.getMetadata().getTilesetVersion());
    }
    public Optional<String> getTileSetDescription(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of(mbTilesReader.getMetadata().getTilesetDescription());
    }

    public Optional<Integer> getMinZoom(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of( mbTilesReader.getMinZoom());
    }
    public Optional<Integer> getMaxZoom(String mapName) throws MBTilesReadException {
        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) return Optional.empty();
        return Optional.of( mbTilesReader.getMaxZoom());
    }

    public Optional<TileResponse> getMbtiles(int z, int x, int y) throws MBTilesReadException, IOException {
        for (MBTilesReader mbTilesReader: loadedMaps.values()) {
            Tile tile = mbTilesReader.getTile(z, x, y);
            if (tile != null) {
                try (InputStream is = tile.getData()) {
                    return Optional.of(new TileResponse(is.readAllBytes(), mbTilesReader.getMetadata().getTileMimeType().toString()));
                }
            }
        }
        return Optional.empty();
    }
    public Optional<TileResponse> getMbtiles(String mapName ,int z, int x, int y) throws MBTilesReadException, IOException {

        MBTilesReader mbTilesReader = loadedMaps.get(mapName);
        if (mbTilesReader == null) {
            throw new MBTilesReadException(new RuntimeException("Map Not Found"));
        }
        try (InputStream is = mbTilesReader.getTile(z,x,y).getData()) {
            return Optional.of(new TileResponse(is.readAllBytes(), mbTilesReader.getMetadata().getTileMimeType().toString()));
        }
    }

    public Optional<MetadataEntry.TileMimeType> getMapMediaType(String mapName) throws MBTilesReadException {
        return Optional.of(loadedMaps.get(mapName).getMetadata().getTileMimeType());
    }

    public void close() {
        loadedMaps.values().forEach(reader -> {
            try {
                reader.close();
            } catch (Exception e) {
                System.out.println("Error closing MBTiles reader" + e);
            }
        });
        loadedMaps.clear();
    }
}
