package com.example.mbtiles_spring_App.Services;


import com.example.mbtiles_spring_App.DTO.MapInfoResponse;
import com.example.mbtiles_spring_App.DTO.TileResponse;
import com.example.mbtiles_spring_App.Repositories.MbtilesRepository;
import org.imintel.mbtiles4j.MBTilesReadException;
import org.imintel.mbtiles4j.Tile;
import org.imintel.mbtiles4j.model.MetadataBounds;
import org.imintel.mbtiles4j.model.MetadataEntry.TileMimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class MbtilesService {
    private final MbtilesRepository mbtilesRepository;

    @Autowired
    public MbtilesService(MbtilesRepository mbtilesRepository) {
        this.mbtilesRepository = mbtilesRepository;
    }

    public void saveMap(MultipartFile file, String fileName) throws MBTilesReadException, IOException {
        mbtilesRepository.save(file, fileName);
    }

    public TileResponse getTile(int z, int x, int y) throws MBTilesReadException, IOException {
        validateCoordinates(z, x, y);

        TileResponse tile = mbtilesRepository.getMbtiles(z, x, y)
               .orElseThrow(() -> new MBTilesReadException(String.format("Map not found at z=%d, x=%d, y=%d", z, x, y),new RuntimeException()));

        if (tile.getData() == null) {
            throw new MBTilesReadException(String.format("Tile not found at z=%d, x=%d, y=%d", z, x, y) ,new RuntimeException());
        }
        return tile;
    }

    public TileResponse getTile(String mapName, int z, int x, int y) throws MBTilesReadException, IOException {
        if (!mapExist(mapName)) throw new MBTilesReadException("Map not found" + mapName, new RuntimeException());

        validateCoordinates(z, x, y);

        TileResponse tile = mbtilesRepository.getMbtiles(mapName,z, x, y)
                .orElseThrow(() -> new MBTilesReadException(new RuntimeException(String.format("Tile not found at z=%d, x=%d, y=%d,", z, x, y))));

        if (tile.getData() == null) {
            throw new MBTilesReadException(String.format("Tile not found at z=%d, x=%d, y=%d", z, x, y), new RuntimeException());
        }

        return tile;
    }

    private boolean mapExist(String mapName) {
        return mapName != null && mbtilesRepository.getMapsList().orElse(List.of()).contains(mapName);
    }


    public List<String> getAllMapsNames() {
        return mbtilesRepository.getMapsList().orElse(List.of());
    }

    public MapInfoResponse getMapInfo(String mapName) throws MBTilesReadException {
        return mapInfoResponseBuilder(mapName);
    }

    private MapInfoResponse mapInfoResponseBuilder(String mapName) throws MBTilesReadException {
        String name = mbtilesRepository.getTileSetName(mapName).orElse("do not have name");
        String attribution = mbtilesRepository.getAttribution(mapName).orElse("do not have attribution");
        String tileSetName = mbtilesRepository.getTileSetName(mapName).orElse("do not have tileset name");
        TileMimeType tileMimeType = mbtilesRepository.getTileMimeType(mapName).orElse(null);
        int minZoom = mbtilesRepository.getMinZoom(mapName).orElse(-1);
        int maxZoom = mbtilesRepository.getMaxZoom(mapName).orElse(-1);
        MetadataBounds metadataBounds = mbtilesRepository.getTileSetBounds(mapName).orElse(null);
        double minLon = metadataBounds != null ? metadataBounds.getLeft() : 0;
        double minLat = metadataBounds != null ? metadataBounds.getBottom() : 0;
        double maxLon = metadataBounds != null ? metadataBounds.getRight() : 0;
        double maxLat = metadataBounds != null ? metadataBounds.getTop() : 0;
        return new MapInfoResponse(name, attribution, tileSetName,
                tileMimeType != null? tileMimeType.toString() : "", minZoom, maxZoom, minLon, maxLon, minLat, maxLat);
    }



    public void closeConnection() {
        mbtilesRepository.close();
    }
    private void validateCoordinates(int z, int x, int y) {
        if (z < 0 || x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
    }
}
