package com.example.gpshares;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gpshares.Dialogs.Dialog_map;
import com.example.gpshares.FriendsHelper.FindFriends;
import com.example.gpshares.MapHelper.FetchURL;
import com.example.gpshares.MapHelper.TaskLoadedCallback;
import com.example.gpshares.PontosDeInteresseHelper.Estabelecimentos;
import com.example.gpshares.PontosDeInteresseHelper.FindNewRestaurante;
import com.example.gpshares.PontosDeInteresseHelper.Local;
import com.example.gpshares.PontosDeInteresseHelper.PontosDeInteresse;
import com.example.gpshares.databinding.ActivityMapBinding;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Map extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, NavigationView.OnNavigationItemSelectedListener, Dialog_map.DialogListener {
    private static final int LOCATION_PERMISSION_CODE = 101;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    //BottomMenu----------------------------------------------------------------------------
    BottomNavigationView navigationViewBottom;
    Toolbar toolbar;
    //Rotas------------------
    MarkerOptions place1, MinhaLocalizacao;
    Polyline currentPolyline;
    //------------------
    ReentrantLock lock = new ReentrantLock();
    //----------------------Pontos de interesse
    ArrayList<FindNewRestaurante> list;
    ArrayList<Local> list2;
    //--------------------------------
    double lat1;
    double lon1;
    private DatabaseReference rota, amigos;
    private GoogleMap mMap;
    private ActivityMapBinding binding;
    //--------------------------------
    private Button parar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Definir Raio de interesse
        if (GlobalVariables.AreaDeInteresse == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_area_de_interesse,null);
            TextInputEditText area = dialogView.findViewById(R.id.Kms);
            Button submeter = dialogView.findViewById(R.id.submeter_Kms);
            builder.setCancelable(false);
            builder.setView(dialogView);
            final AlertDialog alertDialogProfilePicture = builder.create();
            alertDialogProfilePicture.show();
            submeter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (area.toString().isEmpty()){
                        area.setError("Tem de introduzir um valor para a area de interesse");
                        area.requestFocus();
                    }else {
                        try {
                            int Area_De_Interesse = Integer.parseInt(area.getText().toString());
                            System.out.println("OOOOOOOOOOOOOOOOOOO" + Area_De_Interesse);
                            if (Area_De_Interesse <= 0){
                                area.setError("Valor tem de ser superior a zero");
                                area.requestFocus();
                            }else{
                                GlobalVariables.AreaDeInteresse = Area_De_Interesse;
                                System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOO" + FirebaseDatabase.getInstance().getReference("Users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .child("AreaDeInteresse")
                                        .setValue(Area_De_Interesse);
                                alertDialogProfilePicture.cancel();
                            }
                        }catch (NumberFormatException e){
                            area.setError("Tem de introduzir um valor valido");
                            area.requestFocus();
                        }
                    }
                }
            });
        }
        //lista para obter informação dos pontos de interesse
        list = new ArrayList<>();
        list2 = new ArrayList<>();
        //------------------------------
        rota = FirebaseDatabase.getInstance().getReference("Users");
        amigos = FirebaseDatabase.getInstance().getReference("Friends");
        rota.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot i : snapshot.getChildren()) {
                    if (i.hasChild("Estabelecimentos")) {
                        if (i.child("Estabelecimentos").hasChild("Restaurantes")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                    list2.add(local);
                                } else if (visibilidade.equals("Amigos")) {
                                    if (utilizador.equals(i.getKey())) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                        list2.add(local);
                                    } else {
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                            @Override
                                            public void onCallback(boolean i) {
                                                if (i) {
                                                    list.add(findNewRestaurante);
                                                    Local local = new Local(amigo, "Restaurantes", findNewRestaurante.getNome());
                                                    list2.add(local);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Cinemas")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                    list2.add(local);
                                } else if (visibilidade.equals("Amigos")) {
                                    if (utilizador.equals(i.getKey())) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                        list2.add(local);
                                    } else {
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                            @Override
                                            public void onCallback(boolean i) {
                                                if (i) {
                                                    list.add(findNewRestaurante);
                                                    Local local = new Local(amigo, "Cinemas", findNewRestaurante.getNome());
                                                    list2.add(local);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Centros Comerciais")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Centros Comerciais").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                    list2.add(local);
                                } else if (visibilidade.equals("Amigos")) {
                                    if (utilizador.equals(i.getKey())) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                        list2.add(local);
                                    } else {
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                            @Override
                                            public void onCallback(boolean i) {
                                                if (i) {
                                                    list.add(findNewRestaurante);
                                                    Local local = new Local(amigo, "Centros Comerciais", findNewRestaurante.getNome());
                                                    list2.add(local);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //--------------------------------
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
            //ConstraintLayout viewLayout = findViewById(R.id.)
            navigationViewBottom = findViewById(R.id.bottom_navigation);
            navigationViewBottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.bottom_menu_areainteresse:
                            AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.alert_area_de_interesse,null);
                            TextInputEditText area = dialogView.findViewById(R.id.Kms);
                            Button submeter = dialogView.findViewById(R.id.submeter_Kms);
                            builder.setCancelable(true);
                            builder.setView(dialogView);
                            final AlertDialog alertDialogProfilePicture = builder.create();
                            alertDialogProfilePicture.show();
                            submeter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (area.toString().isEmpty()){
                                        area.setError("Tem de introduzir um valor para a area de interesse");
                                        area.requestFocus();
                                    }else {
                                        try {
                                            int Area_De_Interesse = Integer.parseInt(area.getText().toString());
                                            if (Area_De_Interesse <= 0){
                                                area.setError("Valor tem de ser superior a zero");
                                                area.requestFocus();
                                            }else{
                                                GlobalVariables.AreaDeInteresse = Area_De_Interesse;
                                                FirebaseDatabase.getInstance().getReference("Users")
                                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                        .child("AreaDeInteresse")
                                                        .setValue(Area_De_Interesse);
                                                Intent reopenMap = new Intent(Map.this, Map.class);
                                                startActivity(reopenMap);
                                                alertDialogProfilePicture.cancel();
                                            }
                                        }catch (NumberFormatException e){
                                            area.setError("Tem de introduzir um valor valido");
                                            area.requestFocus();
                                        }
                                    }
                                }
                            });
                            break;
                        case R.id.bottom_menu_addlocation:
                            openDialog();
                            break;
                    }
                    return true;
                }
            });
            //SideMenu------------------------------------------------------------------------------
            drawerLayout = findViewById(R.id.drawerlayout);
            navigationView = findViewById(R.id.navigation_view);
            navigationView.setNavigationItemSelectedListener(this);
            //MUDAR IMAGEM DO HEADER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            System.out.println("BITMAP" + GlobalVariables.imagemPerfil);
            View headerView = navigationView.getHeaderView(0);
            ImageView imagemMenu = headerView.findViewById(R.id.imagemMenuPerfil);
            imagemMenu.setImageBitmap(GlobalVariables.imagemPerfil);
            TextView nomeDoUtilizador = headerView.findViewById(R.id.NomeHeader);
            nomeDoUtilizador.setText(GlobalVariables.nomeUtilizador);
            TextView identificadorDoUtilizador = headerView.findViewById(R.id.IdentificadorHeader);
            identificadorDoUtilizador.setText(GlobalVariables.identificador);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            //Obter latitude e longitude do lugar
            //try {
            //    longitude_from_Intent = Double.parseDouble(getIntent().getExtras().get("longitude").toString());
            //    latitude_from_Intent = Double.parseDouble(getIntent().getExtras().get("latitude").toString());
            //
            //} catch (Exception e) {
            //    longitude_from_Intent = 0.0F;
            //}
            parar = findViewById(R.id.buttonParar);
            if (GlobalVariables.MinhaLocalizacao != null && GlobalVariables.PontoDeInteresse != null){
                String url = getRequestURL(GlobalVariables.MinhaLocalizacao, GlobalVariables.PontoDeInteresse, "driving");
                new FetchURL(Map.this).execute(url, "driving");
                parar.setVisibility(View.VISIBLE);
                parar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       currentPolyline.remove();
                       parar.setVisibility(View.GONE);
                    }
                });
            }
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
            MinhaLocalizacao = new MarkerOptions().position(new LatLng(lat1, lon1)).title("MinhaLocalização");

            //Obter Dados dos lugares
            //------------------------------------
            for (int i = 0; i < list.size(); i++) {
                double longitude_from_list = list.get(i).getLongitude();
                double latitude_from_list = list.get(i).getLatitude();
                String nome_from_list = list.get(i).getNome();
                String comentario_from_list = list.get(i).getComentario();
                String avaliacao = list.get(i).getAvaliacao();
                LatLng place_from_list = new LatLng(latitude_from_list, longitude_from_list);
                MarkerOptions markerOptions = new MarkerOptions();
                String distance = distance(MinhaLocalizacao.getPosition().latitude, MinhaLocalizacao.getPosition().longitude,place_from_list.latitude, place_from_list.longitude);
                double dist = Double.parseDouble(distance.trim().replace(",","."));
                double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                double dif = dist-area_int;
                if (dif <=  0){
                    mMap.addMarker(markerOptions
                            .position(place_from_list)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title(nome_from_list)
                            .snippet("\nAvaliação: " + avaliacao + "\nDescrição:" + comentario_from_list));
                }
            }
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location myLocation) {
                    double latitude = myLocation.getLatitude();
                    double longitude = myLocation.getLongitude();
                    lat1 = latitude;
                    lon1 = longitude;
                    place1 = new MarkerOptions().position(new LatLng(lat1, lon1)).title("MinhaLocalização");
                    GlobalVariables.MinhaLocalizacao = place1.getPosition();
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            String distance = distance(place1.getPosition().latitude, place1.getPosition().longitude,marker.getPosition().latitude, marker.getPosition().longitude);
                            new AlertDialog.Builder(Map.this)
                                    .setTitle(marker.getTitle())
                                    .setMessage(marker.getSnippet() + "\nDistancia: " + distance)
                                    .setPositiveButton("Ir", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            GlobalVariables.PontoDeInteresse = marker.getPosition();
                                            String url = getRequestURL(place1.getPosition(), marker.getPosition(), "driving");
                                            new FetchURL(Map.this).execute(url, "driving");
                                            parar.setVisibility(View.VISIBLE);
                                            parar.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    currentPolyline.remove();
                                                    parar.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setNeutralButton("Descrição", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (int z = 0; z < list2.size(); z++) {
                                        if (list2.get(z).getNomeDoLocal().equals(marker.getTitle())) {
                                            String UserId = list2.get(z).getUserId();
                                            String place = list2.get(z).getPlace();
                                            String nome = list2.get(z).getNomeDoLocal();
                                            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA " + nome + " " + place + " " + UserId);
                                            Intent localIntent = new Intent(Map.this, DescricaoDoLocal.class);
                                            localIntent.putExtra("UserId", UserId);
                                            localIntent.putExtra("place", place);
                                            localIntent.putExtra("nome", nome);
                                            startActivity(localIntent);
                                        }
                                    }
                                }
                            }).show();
                            return false;
                        }
                    });
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

    private String distance(double lat1, double long1, double lat2, double long2){
        double longDiff = long1 - long2;
        double distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(longDiff));
        distance = Math.acos(distance);
        // radian to degree
        distance = rad2deg(distance);
        //em miles
        distance = distance*60*1.1515;
        //em km
        distance = distance * 1.609344;
        return String.format(Locale.getDefault(),"%.3f", distance);
    }
    private double rad2deg(double distance){
        return (distance*180/Math.PI);
    }
    //degree to radian
    private double deg2rad (double lat1){
        return (lat1*Math.PI/180);
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


    public void openDialog() {
        Dialog_map dialog = new Dialog_map();
        dialog.show(getSupportFragmentManager(), "dialog");

    }

    @Override
    public void applyTexts(String tipo_de_estabelecimento, String avaliacao_do_estabelecimento, String nome, String comment, String visibilidade, ByteArrayOutputStream imagem) {
        StorageReference objectStorageReference;
        FirebaseFirestore objectFirebaseFirestore;
        String mAuth;
        mAuth = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        objectStorageReference = FirebaseStorage.getInstance().getReference(mAuth);
        objectFirebaseFirestore = FirebaseFirestore.getInstance();
        Toast.makeText(this, tipo_de_estabelecimento + avaliacao_do_estabelecimento + nome + comment + visibilidade, Toast.LENGTH_SHORT).show();
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        String caminho = mAuth + tipo_de_estabelecimento + nome + myPosition.longitude + myPosition.longitude + ".jpg";
        int reports = 0;
        Estabelecimentos estabelecimentos = new Estabelecimentos(nome, avaliacao_do_estabelecimento, comment, myPosition.latitude, myPosition.longitude, visibilidade, caminho, reports);
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


        byte[] bb = imagem.toByteArray();
        StorageReference sr = objectStorageReference.child(caminho);
        sr.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplication(), "Golo", Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                java.util.Map<String, String> objectMap = new HashMap<>();
                objectMap.put("url", task.getResult().toString());
                objectFirebaseFirestore.collection(mAuth).document(caminho)
                        .set(objectMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplication(), "Golo", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplication(), "Fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplication(), "ERRRO", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void verificarAmizade(String id_utilizador, String id_outro, Map.FirebaseCallback firebaseCallback) {
        amigos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.child(id_utilizador).getChildren()) {
                    if (i.getKey().equals(id_outro)) {
                        firebaseCallback.onCallback(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private interface FirebaseCallback {
        void onCallback(boolean i);
    }
}