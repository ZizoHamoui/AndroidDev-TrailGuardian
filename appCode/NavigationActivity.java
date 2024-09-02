package com.example.trailguardian;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener, AdapterView.OnItemSelectedListener, SensorEventListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap myMap;

    public static Polyline trail;

    FusedLocationProviderClient fusedLocationClient;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView timeTextView, flashTextView, fallTextView;
    private Sensor accelerometer;
    private MediaPlayer mediaPlayer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private boolean fallDetected = false;
    private static final int SHAKE_THRESHOLD = 600;
    private static final int PICKUP_THRESHOLD = 200;
    private boolean isFlashlightOn = false;

    private Sensor rotationSensor;

    Button returnB;

    Spinner typeS;
    Button trailB;

    private static Intent navigationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.navigation_page);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        //initiate all elements and activates/registers listeners
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        returnB = findViewById(R.id.returnB);
        returnB.setOnClickListener(this);

        trailB = findViewById(R.id.trailB);
        trailB.setOnClickListener(this);

        if (navigationService == null) {
            trailB.setText("Begin Trail");
        } else {
            trailB.setText("End Trail");
        }

        typeS = findViewById(R.id.typeS);
        typeS.setOnItemSelectedListener(this);

        timeTextView= findViewById(R.id.time);
        flashTextView= findViewById(R.id.flash);
        fallTextView= findViewById(R.id.fall);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //initiates ringtone for phone to ring when it falls
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), alert);

        //internal menu items
        String[] types = new String[]{"Terrain", "Satellite"};
        typeS.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
    }

    //memory optimization, stop sensor from working in background
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //memory optimization, start sensor on app resume
    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "The device doesn't have a light sensor", Toast.LENGTH_SHORT).show();
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void updateCamera(double azimuth, double pitch) {
        if (myMap != null) {

            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, (Location location) -> {

                    // Assuming bearing is your azimuth (rotation around the z axis)
                    myMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude())) // keep the current target
                            .zoom(myMap.getCameraPosition().zoom) // keep the current zoom
                            .bearing((float) azimuth) // set the rotation of the camera
                            .tilt(-(float) Math.min(pitch, 0)) // set the tilt of the camera
                            .build()));
                });
            } catch (SecurityException e) {}

        }
    }

    //Manages sensors
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == rotationSensor) {
            // Convert the rotation-vector to orientations
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientations = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientations);

            // Use orientations to adjust the map camera
            updateCamera(Math.toDegrees(orientations[0]), Math.toDegrees(orientations[1]));
        }

        //checks if light is less than 10 indicating poor visibility and suggesting turn
        //flashlight on else good visibility and no need for flash
        else if (event.sensor == lightSensor) {
            float lux = event.values[0];
            if (lux < 10) {
                timeTextView.setText("Time status: night");
                flashTextView.setText("Please use a flashlight.");
            } else {
                timeTextView.setText("Time status: day");
                flashTextView.setText("No need for a flashlight.");
            }
        }

        //checks to see if phone fell and if it did ring alarm
        //when phone picked up return to usual states of safe
        else if (event.sensor == accelerometer) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastUpdate) > 100) {
                long diffTime = (currentTime - lastUpdate);
                lastUpdate = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (fallDetected) {
                    if (speed > PICKUP_THRESHOLD) {
                        fallTextView.setText("Phone Status: safe");
                        fallDetected = false;
                    }
                } else if (speed > SHAKE_THRESHOLD) {
                    fallDetected = true;
                    fallTextView.setText("Phone Status: Fell");
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.stop());
                        mediaPlayer.seekTo(0);
                        getWindow().getDecorView().postDelayed(() -> {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                        }, 5000);
                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //This code initializes a Google Map, sets its type, checks for
    // location permissions, loads markers, centers the map on a specific
    // location, and configures location-related settings and listeners.
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        myMap = map;

        myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        checkLocationPermission();
        loadMarkers();

        myMap.setMyLocationEnabled(true);
        myMap.setOnMyLocationClickListener(this);
        myMap.setOnMapLongClickListener(this);
        myMap.setIndoorEnabled(false);
        myMap.setTrafficEnabled(false);
        myMap.setBuildingsEnabled(true);
        myMap.setOnMarkerClickListener(this);

        UiSettings myMapUI = myMap.getUiSettings();

        myMapUI.setMyLocationButtonEnabled(false);
        //myMapUI.setZoomControlsEnabled(true);
        myMapUI.setCompassEnabled(false);
        myMapUI.setMapToolbarEnabled(true);

        PolylineOptions polylineOptions = new PolylineOptions();

        File file = new File(this.getFilesDir(), "trails.bin");
        if (file.exists()) {

            FileInputStream trailsInputStream;
            try {
                trailsInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            byte[] bytes;
            try {
                bytes = new byte[trailsInputStream.available()];
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                trailsInputStream.read(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            try {
                while (true) {
                    polylineOptions.add(new LatLng(buffer.getFloat(), buffer.getFloat()));
                }
            } catch (BufferUnderflowException e) {}

        }

        polylineOptions.width(15); // Set the polyline's width
        polylineOptions.color(Color.GREEN); // Set the polyline's color

        trail = myMap.addPolyline(polylineOptions);

    }

    //just lat lng
    private void gotoLocation(double lat, double lng) {
        LatLng latlng = new LatLng(lat, lng);

        myMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    //lan lng and zoom
    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latlng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, zoom);
        myMap.moveCamera(update);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    //checks if the app has permission to access the device's fine location
    // and, if not, requests it from the user
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                ActivityCompat.requestPermissions(NavigationActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    //loads saved markers through shared preferences
    private void loadMarkers() {
        SharedPreferences prefs = getSharedPreferences("Markers", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String savedValue = (String) entry.getValue();
            String[] parts = savedValue.split(",");
            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            String title = parts[2];

            LatLng position = new LatLng(lat, lng);
            myMap.addMarker(new MarkerOptions().position(position).title(title));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {


                }
                return;
            }

        }
    }

    //adds markers to the screen
    private void addMarkerToMap(LatLng latLng, String title) {
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        myMap.addMarker(options);

        saveMarker(latLng, title);
    }

    //Saves marker to be visible when app restarted
    private void saveMarker(LatLng latLng, String title) {
        SharedPreferences prefs = getSharedPreferences("Markers", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String key = "marker_" + System.currentTimeMillis();
        String value = latLng.latitude + "," + latLng.longitude + "," + title;

        editor.putString(key, value);
        editor.apply();
    }

    //checks to see if finger on screen for a while and if so adds marker to screen
    @Override
    public void onMapLongClick(LatLng latLng) {
        String markerTitle = "Custom Location";
        addMarkerToMap(latLng, markerTitle);
    }

    //manages button clicks
    @Override
    public void onClick(View v) {
        if (v == returnB) {
            // Return button action
            startActivity(new Intent(this, MainActivity.class));
        } else if (v == trailB) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (navigationService == null) {
                    // Start the trail
                    navigationService = new Intent(this, NavigationService.class);
                    startService(navigationService);
                    trailB.setText("End Trail");
                } else {
                    // End the trail, attempt to get the last known location
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Location is available, save trail information
                            saveTrailInfo(location);
                        } else {
                            // Location is null, show a Toast message
                            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Stop the service as the trail is ending
                    stopService(navigationService);
                    navigationService = null;
                    trailB.setText("Begin Trail");
                }
            } else {
                // Request location permission if it's not granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }



    //manages map view toggle
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case 1:
                myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //use camera to manage turn on and off the flashlight
    public void switcher(View view) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                String cameraId = cameraManager.getCameraIdList()[0];

                if (isFlashAvailable()) {
                    cameraManager.setTorchMode(cameraId, !isFlashlightOn);
                    isFlashlightOn = !isFlashlightOn;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //checks if a flashlight is availabe
    private boolean isFlashAvailable() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    //removes marker when clicked on
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        marker.remove();

        String markerKey = getKeyForMarker(marker);
        if (markerKey != null) {
            SharedPreferences prefs = getSharedPreferences("Markers", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(markerKey);
            editor.apply();
        }

        return true;
    }

    //reverses the marker address in order to find the clicked marker to remove
    private String getKeyForMarker(Marker marker) {
        SharedPreferences prefs = getSharedPreferences("Markers", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        LatLng markerLatLng = marker.getPosition();
        String markerTitle = marker.getTitle();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String savedValue = (String) entry.getValue();
            String[] parts = savedValue.split(",");

            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            String title = parts[2];

            // Check if the saved marker matches the clicked marker
            if (markerLatLng.latitude == lat && markerLatLng.longitude == lng && markerTitle.equals(title)) {
                return entry.getKey(); // Return the key of the matching marker
            }
        }
        return null;
    }

    //gets the information of the trail including date, lang and lat
    private void saveTrailInfo(Location location) {
        SharedPreferences prefs = getSharedPreferences("Trails", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Get the current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // Save the route information. Consider using a more robust key or a database for multiple entries.
        String trailInfo = currentDate + "," + lat + "," + lng;
        editor.putString("trail_" + System.currentTimeMillis(), trailInfo);
        editor.apply();
    }

}