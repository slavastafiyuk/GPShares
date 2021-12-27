package com.example.gpshares;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gpshares.MapHelper.FetchURL;
import com.example.gpshares.MapHelper.TaskLoadedCallback;
import com.example.gpshares.databinding.ActivityMapBinding;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Map extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, NavigationView.OnNavigationItemSelectedListener, Dialog.DialogListener {
    private static final int LOCATION_PERMISSION_CODE = 101;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    //Rotas------------------
    MarkerOptions place1;
    Polyline currentPolyline;
    //------------------
    ArrayList<LatLng> listPoints;
    ReentrantLock lock = new ReentrantLock();
    //--------------------------------
    double lat1;
    double lon1;
    private GoogleMap mMap;
    private ActivityMapBinding binding;

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
            drawerLayout = findViewById(R.id.drawerlayout);
            navigationView = findViewById(R.id.navigation_view);
            navigationView.setNavigationItemSelectedListener(this);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //case R.id.nav_map:
            //    startActivity(new Intent(this, Map.class));
            //    break;
            case R.id.nav_settings:
                startActivity(new Intent(this, Setting.class));
                break;
            case R.id.menu_add:
                startActivity(new Intent(this, FindFriends.class));
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(this, Login.class));
                break;
            case R.id.nav_pontos_de_interesse:
                startActivity(new Intent(this, PontosDeInteresse.class));
                break;
            case R.id.menu_friends:
                startActivity(new Intent(this, UserFriends.class));
        }
        item.setChecked(true);
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);

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
        Location myLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }
        if (myLocation != null) {
            //TESTAR
            lat1 = myLocation.getLatitude();
            lon1 = myLocation.getLongitude();
            LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 17));
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {

                @Override
                public void onLocationChanged(Location myLocation) {
                    double latitude = myLocation.getLatitude();
                    double longitude = myLocation.getLongitude();
                    lat1 = latitude;
                    lon1 = longitude;
                    place1 = new MarkerOptions().position(new LatLng(lat1, lon1)).title("MinhaLocalização");
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
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void Marcador(View view) {
        openDialog();
    }

    public void openDialog() {
        Dialog dialog = new Dialog();
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void applyTexts(String tipo_de_estabelecimento, String avaliacao_do_estabelecimento, String nome, String comment) {
        Toast.makeText(this, tipo_de_estabelecimento + avaliacao_do_estabelecimento + nome + comment, Toast.LENGTH_SHORT).show();
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        Estabelecimentos estabelecimentos = new Estabelecimentos(nome, avaliacao_do_estabelecimento, comment, myPosition.latitude, myPosition.longitude);
        FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("Estabelecimentos").child(tipo_de_estabelecimento).child(nome).setValue(estabelecimentos).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(Map.this, "Local adicionado", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Map.this, "Houve um erro", Toast.LENGTH_SHORT).show();
            }
        });
    }
}