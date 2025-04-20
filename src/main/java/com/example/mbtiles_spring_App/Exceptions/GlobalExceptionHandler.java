package com.example.mbtiles_spring_App.Exceptions;


import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import org.imintel.mbtiles4j.MBTilesReadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MBTilesReadException.class)
    public ResponseEntity<Map<String, String>> handleMBTilesReadException(MBTilesReadException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, String>> handleMissingFileException(MissingServletRequestPartException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Файл не был прикреплён. Убедитесь, что поле 'file' присутствует в форме.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Внутренняя ошибка сервера: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
