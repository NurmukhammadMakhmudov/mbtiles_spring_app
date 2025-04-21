package com.example.mbtiles_spring_App.Controllers;

import com.example.mbtiles_spring_App.DTO.MapInfoResponse;
import com.example.mbtiles_spring_App.DTO.TileResponse;
import com.example.mbtiles_spring_App.Services.MbtilesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Mbtiles API", description = "API для работы с файлами mbtiles")
public class MbtilesController {
    private static final Logger log = LoggerFactory.getLogger(MbtilesController.class);
    private final MbtilesService mbtilesService;

    public MbtilesController(MbtilesService mbtilesService) {
        this.mbtilesService = mbtilesService;
    }

    @Operation(summary = "Получить тайл карты", description = "Возвращает тайл карты по заданным координатам и уровню зума.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тайл успешно получен", content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "Тайл не найден")
    })
    @GetMapping("/{z}/{x}/{y}")
    public ResponseEntity<byte[]> getMbtilesByMapNameAndZoomLahLat(
            @RequestParam(required = false) String mapName,
            @PathVariable @Min(0) Integer z,
            @PathVariable @Min(0) Integer x,
            @PathVariable @Min(0) Integer y
    ) throws MBTilesReadException, IOException {
        log.info("Получен запрос на тайл: mapName={}, z={}, x={}, y={}", mapName, z, x, y);
        TileResponse tileResponse = mbtilesService.getTile(mapName, z, x, y);
        MediaType mediaType = MediaType.parseMediaType(tileResponse.getMimeType());
        log.debug("MimeType: {}", mediaType);
        return ResponseEntity.ok().contentType(mediaType).body(tileResponse.getData());
    }

    @Operation(summary = "Загрузить файл mbtiles", description = "Загружает новый mbtiles файл на сервер.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно загружена"),
            @ApiResponse(responseCode = "400", description = "Ошибка загрузки карты")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> mbtiles(
            @RequestParam String mapName,
            @RequestParam("file") MultipartFile file
    ) throws MBTilesReadException, IOException {
        log.info("Загрузка карты: mapName={}, fileName={}, size={} bytes",
                mapName, file.getOriginalFilename(), file.getSize());
        mbtilesService.saveMap(file, mapName);
        log.info("Карта успешно загружена: {}", mapName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить список карт", description = "Возвращает список всех доступных карт.")
    @GetMapping
    public ResponseEntity<List<String>> getMaps() {
        log.info("Запрос на получение списка всех карт");
        List<String> mapNames = mbtilesService.getAllMapsNames();
        log.debug("Найдено {} карт", mapNames.size());
        return ResponseEntity.ok(mapNames);
    }

    @Operation(summary = "Получить информацию о карте", description = "Возвращает информацию о карте по её названию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о карте успешно получена", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MapInfoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @GetMapping("/{mapName}")
    public ResponseEntity<MapInfoResponse> getMapInfo(@PathVariable String mapName) throws MBTilesReadException {
        log.info("Запрос информации о карте: {}", mapName);
        MapInfoResponse info = mbtilesService.getMapInfo(mapName);
        log.debug("Информация по карте {}: {}", mapName, info);
        return ResponseEntity.ok(info);
    }

    @Operation(summary = "Удалить карту", description = "Удаляет карту по её названию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteMap(@RequestParam(required = false) String mapName) throws MBTilesReadException {
        log.info("Удаление/закрытие карты: {}", mapName != null ? mapName : "(все карты)");
        mbtilesService.closeConnection(mapName);
        log.info("Карта закрыта: {}", mapName);
        return ResponseEntity.noContent().build();
    }
}
