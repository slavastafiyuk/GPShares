package com.example.gpshares;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.gpshares.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polygon;

import java.util.concurrent.locks.ReentrantLock;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private static final int LOCATION_PERMISSION_CODE = 101;
    ReentrantLock lock = new ReentrantLock();
    //--------------------------------
    double lat1;
    double lon1;
    Polygon poly;
    //ArrayList<Polygon> polys = new ArrayList<Polygon>();
    //ArrayList<Polygon> pl = new ArrayList<Polygon>();
    //int i=0;
    //--------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isLocationPermissionGranted()) {
            try {
                requestLocationPermission();
            } finally {
                lock.unlock();
            }
            lock.lock();
            binding = ActivityMapBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else if (isLocationPermissionGranted()) {
            binding = ActivityMapBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }

    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }
        if (myLocation != null) {
            lat1 = myLocation.getLatitude();
            lon1 = myLocation.getLongitude();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location myLocation) {
                    double latitude = myLocation.getLatitude();
                    double longitude = myLocation.getLongitude();
                    //List<LatLng> polygon = new ArrayList<>();
                    //polygon.add(new LatLng(lat1, lon1));
                    //polygon.add(new LatLng(latitude, longitude));
                    //poly = mMap.addPolygon(new PolygonOptions()
                    //        .addAll(polygon)
                    //        .strokeColor(Color.argb(0, 0, 255, 255))
                    //        .strokeWidth(15)
                    //        .fillColor(Color.argb(0, 0, 255, 255))
                    //);
                    //if (desenhar(findViewById(R.id.comecarButton))){
                    //    poly.setFillColor(Color.argb(255, 0, 255, 255));
                    //    poly.setStrokeColor(Color.argb(255, 0, 255, 255));
                    //}else{
                    //    poly.setFillColor(Color.argb(0, 0, 255, 255));
                    //    poly.setStrokeColor(Color.argb(0, 0, 255, 255));
                    //}
                    lat1 = latitude;
                    lon1 = longitude;
                }
                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }
            });
        }
    }
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }
    //@SuppressLint("SetTextI18n")
    //public void Caminho(View view) {
    //    Button button = (Button) view;
    //    String z = (String) button.getText();
//
    //    LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    //    if (z.equals("Iniciar")) {
    //        button.setText("Parar");
    //        //------------------------------------------
    //        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    //            // TODO: Consider calling
    //            //    ActivityCompat#requestPermissions
    //            return;
    //        }
    //        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    //        LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
    //        mMap.addMarker(new MarkerOptions().position(userLocation)
    //                .title("O começo")
    //                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    //        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20), 1000, null);
    //    }else if(z.equals("Parar")){
    //        button.setText("Iniciar");
    //        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    //            // TODO: Consider calling
    //            //    ActivityCompat#requestPermissions
    //            return;
    //        }
    //        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    //        LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
    //        mMap.addMarker(new MarkerOptions().position(userLocation)
    //                .title("O Fim")
    //                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    //        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20), 1000, null);
    //        //-----------------------------------------------------
//
//
    //    }
    //}
    //public boolean desenhar(View view){
    //    Button button = (Button) view;
    //    String z = (String) button.getText();
    //    if (z.equals("Iniciar")) {
    //        return false;
    //    }else return z.equals("Parar");
    //}
}