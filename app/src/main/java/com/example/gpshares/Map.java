package com.example.gpshares;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gpshares.MapHelper.FetchURL;
import com.example.gpshares.MapHelper.TaskLoadedCallback;
import com.example.gpshares.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Map extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    //Rotas------------------
    MarkerOptions place1;
    Polyline currentPolyline;
    ArrayList<LatLng> listPoints;
    //------------------

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private static final int LOCATION_PERMISSION_CODE = 101;
    ReentrantLock lock = new ReentrantLock();
    //--------------------------------
    double lat1;
    double lon1;

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
            //Rotas---------------------------------------------------------------------------------
            //place1 = new MarkerOptions().position(new LatLng(41.14961, -8.61099)).title("Porto");
            //place2 = new MarkerOptions().position(new LatLng(38.7166700, -9.1333300)).title("Porto");
            //String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");
            //new FetchURL(Map.this).execute(url, "driving");
            listPoints = new ArrayList<>();
            //SideMenu------------------------------------------------------------------------------
            drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
            navigationView = (NavigationView) findViewById(R.id.navigation_view);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(Map.this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(@NonNull LatLng latLng) {
                    //Reset marker quando ja existem 2
                    if (listPoints.size() == 1) {
                        listPoints.clear();
                        mMap.clear();
                    }
                    //save first point selected
                    listPoints.add(latLng);
                    //criar marker
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    if (listPoints.size() == 1) {
                        //add first marker to the map
                        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        //mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    }
                    //else {
                    //    //add second marker to the map
                    //    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    //}
                    //mMap.addMarker(markerOptions);
                    //if (listPoints.size() == 2) {
                    //    //Create the URL to ge trrequest from first marker to second marker
                    //    String url = getRequestURL(listPoints.get(0), listPoints.get(1), "driving");
                    //    new FetchURL(Map.this).execute(url, "driving");
                    //    //TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    //    //taskRequestDirections.execute(url);
                    //}
                }
            });

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
                    lat1 = latitude;
                    lon1 = longitude;
                    place1 = new MarkerOptions().position(new LatLng(lat1,lon1)).title("MinhaLocalização");
                    if (listPoints.size() == 1) {
                        String url = getRequestURL(place1.getPosition(), listPoints.get(0), "driving");
                        new FetchURL(Map.this).execute(url, "driving");
                    }
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

    //Rotas-----------------------------------------------------------------------------------------
    private String getRequestURL(LatLng origin, LatLng dest, String directionMode) {
        //Origem da rota
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        //Destino da rota
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value of sensor
        //String sensor = "sensor=false";
        //Mode
        String mode = "mode=" + directionMode;
        //Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        //Output format
        String output = "json";
        //Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getResources().getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline!=null){
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

    }
    //private String requestDirections(String reqUrl) throws IOException {
    //    String responseString = "";
    //    InputStream inputStream = null;
    //    HttpURLConnection httpURLConnection = null;
    //    try {
    //        URL url = new URL(reqUrl);
    //        httpURLConnection = (HttpURLConnection) url.openConnection();
    //        httpURLConnection.connect();
    //        //Get the response result
    //        inputStream = httpURLConnection.getInputStream();
    //        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    //        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    //        StringBuffer stringBuffer = new StringBuffer();
    //        String line = "";
    //        while((line = bufferedReader.readLine()) !=null){
    //            stringBuffer.append(line);
    //        }
    //        responseString = stringBuffer.toString();
    //        bufferedReader.close();
    //        inputStreamReader.close();
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }finally {
    //        if (inputStream != null){
    //            inputStream.close();
    //        }
    //        httpURLConnection.disconnect();
    //    }
    //    return  responseString;
    //}
    //public class TaskRequestDirections extends AsyncTask<String, Void, String> {
    //    @Override
    //    protected String doInBackground(String... strings) {
    //        String responseString = "";
    //        try {
    //            responseString = requestDirections(strings[0]);
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        return responseString;
    //    }
    //    @Override
    //    protected void onPostExecute(String s) {
    //        super.onPostExecute(s);
    //        //parse json here
    //        TaskParser taskParser = new TaskParser();
    //        taskParser.execute(s);
    //    }
    //}
    //public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>>{
    //    @Override
    //    protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
    //        JSONObject jsonObject = null;
    //        List<List<HashMap<String, String>>> routes = null;
    //        try {
    //            jsonObject = new JSONObject(strings[0]);
    //            DirectionsParser directionsParser = new DirectionsParser();
    //            routes = directionsParser.parse(jsonObject);
    //        } catch (JSONException e) {
    //            e.printStackTrace();
    //        }
    //        return routes;
    //    }
    //    @Override
    //    protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
    //        //Get list route and display it into the map
    //        ArrayList points = null;
    //        PolylineOptions polylineOptions = null;
    //        for (List<HashMap<String, String>> path: lists){
    //            points = new ArrayList();
    //            polylineOptions = new PolylineOptions();
    //            for (HashMap<String, String> point: path){
    //                double lat = Double.parseDouble(point.get("lat"));
    //                double lon = Double.parseDouble(point.get("lon"));
    //                points.add(new LatLng(lat, lon));
    //            }
    //            polylineOptions.addAll( points);
    //            polylineOptions.width(15);
    //            polylineOptions.color(Color.BLUE);
    //            polylineOptions.geodesic(true);
    //        }
    //        if (polylineOptions!=null){
    //            mMap.addPolyline(polylineOptions);
    //        }else{
    //            Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
    //        }
    //    }
    //}
}