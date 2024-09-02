package com.example.trailguardian;

public class Trail {
    private String date;
    private double latitude;
    private double longitude;

    //get information from the trail components
    public Trail(String date, double latitude, double longitude) {
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
