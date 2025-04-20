package com.example.mbtiles_spring_App.DTO;


import jakarta.validation.constraints.*;

public class MapInfoResponse {
    @NotBlank
    private String name;
    private String attribution;
    @NotBlank
    private String tileSetName;
    @NotBlank
    private String tileMimeType;
    @Min(0)
    private int minZoom;
    @Min(0)
    private int maxZoom;
    @DecimalMin("-180")
    @DecimalMax("180")
    private double minLon;
    @DecimalMin("-180")
    @DecimalMax("180")
    private double maxLon;
    @DecimalMin("-180")
    @DecimalMax("180")
    private double minLat;
    @DecimalMin("-180")
    @DecimalMax("180")
    private double maxLat;

    public MapInfoResponse(String name,
                           String attribution,
                           String tileSetName,
                           String tileMimeType,
                           int minZoom,
                           int maxZoom,
                           double minLon,
                           double maxLon,
                           double minLat,
                           double maxLat) {
        this.name = name;
        this.attribution = attribution;
        this.tileSetName = tileSetName;
        this.tileMimeType = tileMimeType;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getTileSetName() {
        return tileSetName;
    }

    public void setTileSetName(String tileSetName) {
        this.tileSetName = tileSetName;
    }

    public String getTileMimeType() {
        return tileMimeType;
    }

    public void setTileMimeType(String tileMimeType) {
        this.tileMimeType = tileMimeType;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public double getMinLon() {
        return minLon;
    }

    public void setMinLon(double minLon) {
        this.minLon = minLon;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(double maxLon) {
        this.maxLon = maxLon;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }
}
