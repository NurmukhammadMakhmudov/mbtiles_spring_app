package com.example.mbtiles_spring_App.Exceptions;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import org.imintel.mbtiles4j.MBTilesReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MBTilesReadException.class)
    public ResponseEntity<Map<String, String>> handleMBTilesReadException(MBTilesReadException ex) {
        log.error("Ошибка чтения MBTiles: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, String>> handleMissingFileException(MissingServletRequestPartException ex) {
        log.error("Ошибка: отсутствует часть запроса (файл): {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Файл не был прикреплён. Убедитесь, что поле 'file' присутствует в форме.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        log.error("Такого тайла для пользовательских данных не существует {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Такого тайла для пользовательских данных не существует");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Внутренняя ошибка сервера: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
