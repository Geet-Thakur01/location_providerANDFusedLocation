package com.flotingera.flushpro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GPSTracker.LocationListnerCustom {
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int GPS_REQUEST = 1112;
    private TextView locaiton_txt;
    private long startTime, endTime;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isGPS;
    private GPSTracker gps;
    private GPSTrackManager gpsTrackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button get_latlng = (Button) findViewById(R.id.get_latlng);
        Button clear = (Button) findViewById(R.id.clear);
        Button locationmgr = findViewById(R.id.locationmgr);

        locaiton_txt = findViewById(R.id.locaiton_txt);
        get_latlng.setOnClickListener(this);
        clear.setOnClickListener(this);
        locationmgr.setOnClickListener(this);
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_latlng:
                startTime = System.currentTimeMillis();
                takepermission();
                break;

            case R.id.clear:
                locaiton_txt.setText("we lost");
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                    locationRequest = null;
                    locationCallback = null;
                    mFusedLocationClient = null;
                }else if(gps!=null){
                    gps.removeUpdates();
                    gps=null;
                }else if(gpsTrackManager!=null){
                    gpsTrackManager.stopLocationUpdate();
                }
                break;
            case R.id.locationmgr:
                startTime = System.currentTimeMillis();
//                if(gpsTrackManager==null){
//                    gpsTrackManager=new GPSTrackManager(this);
//                }
//                gpsTrackManager.startLocaitonUpdate();
                startTrackingLocationManager();
                break;
            default:
                break;
        }
    }

    private void startTrackingLocationManager() {
        gps = new GPSTracker(this);
        int status = 0;
        if (gps.canGetLocation()) {
            status = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(getApplicationContext());

            if (status == ConnectionResult.SUCCESS) {
                Log.e("TAG: ","connected");
            } else {
                gps.showSettingsAlert();
            }
        }
    }

    private void takepermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                do something
                fucedLocationEnableFunction();

            } else {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
////                    permissionDialog();
//                    Log.e("TAG", "tag permission");
//                } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//                }
            }
        }
    }

    private void fucedLocationEnableFunction() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0); // 10 seconds
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        try {
                            endTime = System.currentTimeMillis();
                            float diff = endTime - startTime;
                            String newLat = String.valueOf(location.getLatitude());
                            String newLong = String.valueOf(location.getLongitude());
                            locaiton_txt.setText(newLat + "___" + newLong + "___Time diff: " + diff);
                            startTime = 0;
                            endTime = 0;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationUpdate(double latitude,double longitude) {
        endTime = System.currentTimeMillis();
        float diff = endTime - startTime;
        String newLat = String.valueOf(latitude);
        String newLong = String.valueOf(longitude);
        locaiton_txt.setText(newLat + "___" + newLong + "___Time diff: " + diff);
        startTime = 0;
        endTime = 0;
    }
}
