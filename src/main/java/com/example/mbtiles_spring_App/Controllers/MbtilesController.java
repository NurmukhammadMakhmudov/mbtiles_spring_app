package com.example.mbtiles_spring_App.Controllers;


import com.example.mbtiles_spring_App.DTO.MapInfoResponse;
import com.example.mbtiles_spring_App.DTO.TileResponse;
import com.example.mbtiles_spring_App.Services.MbtilesService;
import jakarta.validation.constraints.Min;
import org.imintel.mbtiles4j.MBTilesReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mbtiles")
public class MbtilesController {
    private static final Logger log = LoggerFactory.getLogger(MbtilesController.class);

    private final MbtilesService mbtilesService;

    public MbtilesController(MbtilesService mbtilesService) {
        this.mbtilesService = mbtilesService;
    }

    @GetMapping("/{z}/{x}/{y}")
    public ResponseEntity<byte[]> getMbtilesByZoomLahLat(
            @PathVariable @Min(0) Integer z,
            @PathVariable @Min(0) Integer x,
            @PathVariable @Min(0) Integer y
    ) throws MBTilesReadException, IOException {
        TileResponse tileResponse = mbtilesService.getTile(z, x, y);
        MediaType mediaType = MediaType.parseMediaType(tileResponse.getMimeType());
        return ResponseEntity.ok().contentType(mediaType).body(tileResponse.getData());
    }
    @GetMapping("/{mapName}/{z}/{x}/{y}")
    public ResponseEntity<byte[]> getMbtilesByMapNameAndZoomLahLat(
            @PathVariable String mapName,
            @PathVariable @Min(0) Integer z,
            @PathVariable @Min(0) Integer x,
            @PathVariable @Min(0) Integer y
    ) throws MBTilesReadException, IOException {
        TileResponse tileResponse = mbtilesService.getTile(mapName,z, x, y);
        MediaType mediaType = MediaType.parseMediaType(tileResponse.getMimeType());
        return ResponseEntity.ok().contentType(mediaType).body(tileResponse.getData());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity mbtiles(
            @RequestParam String mapName,
            @RequestParam("file") MultipartFile file
    ) throws MBTilesReadException, IOException {
         mbtilesService.saveMap(file, mapName);
         return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> getMaps() {
        return ResponseEntity.ok(mbtilesService.getAllMapsNames());
    }

    @GetMapping("/{mapName}")
    public ResponseEntity<MapInfoResponse> getMapInfo(@PathVariable String mapName) throws MBTilesReadException {
        return ResponseEntity.ok(mbtilesService.getMapInfo(mapName));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteMap(@RequestParam String mapName) throws MBTilesReadException {
        mbtilesService.closeConnection();
        return ResponseEntity.noContent().build();
    }

}
