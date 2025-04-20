package com.example.mbtiles_spring_App.DTO;

import java.util.Arrays;

public class TileResponse {
    private byte[] data;
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
}
