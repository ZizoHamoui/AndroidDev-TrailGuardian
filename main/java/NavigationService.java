package com.example.trailguardian;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class NavigationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback locationCallback;

    private FileOutputStream trailsOutputStream;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "channel",
                    "channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        startForeground(1,
                new NotificationCompat.Builder(this, "channel")
                        .setContentTitle("Foreground Service")
                        .setContentText("Running...")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
        );

        try {
            trailsOutputStream = new FileOutputStream(new File(this.getFilesDir(), "trails.bin"), true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        SharedPreferences tempPreferences = getSharedPreferences("temp", Context.MODE_PRIVATE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval((long)(tempPreferences.getFloat("pollingRate", 15) * 1000));
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) { return; }

                for (Location location : locationResult.getLocations()) {

                    buffer.putFloat((float)location.getLatitude());
                    buffer.putFloat((float)location.getLongitude());
                    try {
                        trailsOutputStream.write(buffer.array());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    buffer.clear();

                    if (NavigationActivity.trail != null) {
                        List<LatLng> points = NavigationActivity.trail.getPoints();
                        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        NavigationActivity.trail.setPoints(points);
                    }

                    //Log.d("service", "Poll location");
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Please start this service instead of binding it");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }



}