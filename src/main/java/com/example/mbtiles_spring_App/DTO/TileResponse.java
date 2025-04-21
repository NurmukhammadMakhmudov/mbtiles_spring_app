package com.example.mbtiles_spring_App.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

public class TileResponse {

    @Schema(description = "Данные тайла", example = "base64 encoded image data")
    private byte[] data;

    @Schema(description = "Тип MIME данных тайла", example = "image/png")
    private String mimeType;

    public TileResponse(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
