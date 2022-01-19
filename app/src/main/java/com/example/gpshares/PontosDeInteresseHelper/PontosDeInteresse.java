package com.example.gpshares.PontosDeInteresseHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpshares.DescricaoDoLocal;
import com.example.gpshares.Dialogs.Dialog_filter_PontosDeInteresse;
import com.example.gpshares.FriendsHelper.FindFriends;
import com.example.gpshares.GlobalVariables;
import com.example.gpshares.Login;
import com.example.gpshares.Map;
import com.example.gpshares.R;
import com.example.gpshares.Setting;
import com.example.gpshares.UserFriends;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class PontosDeInteresse extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Dialog_filter_PontosDeInteresse.DialogListenerFilter, MyAdapter.onAdapterListener {
    //SideMenu--------------------------------------------------------------------------------------
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ArrayList<FindNewRestaurante> list;
    ArrayList<Local> listIDS;
    MyAdapter myAdapter;
    private Button restaurantesButton, cinemasButton, filt;
    private DatabaseReference rota, amigos;
    private RecyclerView searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontos_de_interesse);
        rota = FirebaseDatabase.getInstance().getReference("Users");
        amigos = FirebaseDatabase.getInstance().getReference("Friends");
        //SideMenu----------------------------------------------------------------------------------
        drawerLayout = findViewById(R.id.drawerlayout_pontosDeInteresse);
        navigationView = findViewById(R.id.navigation_viewPontosDeInteresse);
        navigationView.setNavigationItemSelectedListener(this);
        //MUDAR IMAGEM DO HEADER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        View headerView = navigationView.getHeaderView(0);
        ImageView imagemMenu = (ImageView) headerView.findViewById(R.id.imagemMenuPerfil);
        imagemMenu.setImageBitmap(GlobalVariables.imagemPerfil);
        TextView nomeDoUtilizador = (TextView) headerView.findViewById(R.id.NomeHeader);
        nomeDoUtilizador.setText(GlobalVariables.nomeUtilizador);
        TextView identificadorDoUtilizador = (TextView) headerView.findViewById(R.id.IdentificadorHeader);
        identificadorDoUtilizador.setText(GlobalVariables.nomeUtilizador);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        filt = findViewById(R.id.buttonFiltrar);
        filt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filtrar(v);
            }
        });
        //RecyclerView, Janela dos resultados dos restaurantes
        listIDS = new ArrayList<>();
        list = new ArrayList<>();
        searchResults = findViewById(R.id.searchResultRestaurantes);
        searchResults.setHasFixedSize(true);
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter(this, list, this);
        searchResults.setAdapter(myAdapter);
        restaurantesButton = findViewById(R.id.buttonRestaurantes);
        //Lista inicial!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        rota.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                listIDS.clear();
                String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot i : snapshot.getChildren()) {
                    if (i.hasChild("Estabelecimentos")) {
                        if (i.child("Estabelecimentos").hasChild("Restaurantes")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                                double dist = Double.parseDouble(distancia.trim().replace(",","."));
                                double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                                double dif = dist-area_int;
                                if (dif <=  0){
                                    if (visibilidade.equals("Publico")) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                        listIDS.add(local);
                                    } else if (visibilidade.equals("Amigos")) {
                                        if (utilizador.equals(i.getKey())) {
                                            list.add(findNewRestaurante);
                                            Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                            listIDS.add(local);
                                        } else {
                                            String amigo = i.getKey();
                                            verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                                @Override
                                                public void onCallback(boolean i) {
                                                    if (i) {
                                                        list.add(findNewRestaurante);
                                                        Local local = new Local(amigo, "Restaurantes", findNewRestaurante.getNome());
                                                        listIDS.add(local);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Cinemas")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                                double dist = Double.parseDouble(distancia.trim().replace(",","."));
                                double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                                double dif = dist-area_int;
                                if (dif <=  0){
                                    if (visibilidade.equals("Publico")) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                        listIDS.add(local);
                                    } else if (visibilidade.equals("Amigos")) {
                                        if (utilizador.equals(i.getKey())) {
                                            list.add(findNewRestaurante);
                                            Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                            listIDS.add(local);
                                        } else {
                                            String amigo = i.getKey();
                                            verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                                @Override
                                                public void onCallback(boolean i) {
                                                    if (i) {
                                                        list.add(findNewRestaurante);
                                                        Local local = new Local(amigo, "Cinemas", findNewRestaurante.getNome());
                                                        listIDS.add(local);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }

                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Centros Comerciais")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Centros Comerciais").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                                double dist = Double.parseDouble(distancia.trim().replace(",","."));
                                double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                                double dif = dist-area_int;
                                if (dif <=  0){
                                    if (visibilidade.equals("Publico")) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                        listIDS.add(local);
                                    } else if (visibilidade.equals("Amigos")) {
                                        if (utilizador.equals(i.getKey())) {
                                            list.add(findNewRestaurante);
                                            Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                            listIDS.add(local);
                                        } else {
                                            String amigo = i.getKey();
                                            verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                                @Override
                                                public void onCallback(boolean i) {

                                                    if (i) {
                                                        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                        list.add(findNewRestaurante);
                                                        Local local = new Local(amigo, "Centros Comerciais", findNewRestaurante.getNome());
                                                        listIDS.add(local);
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
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //Lista quando carregamos no botão de restaurantes!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        restaurantesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        listIDS.clear();
                        String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        for (DataSnapshot i : snapshot.getChildren()) {
                            if (i.hasChild("Estabelecimentos")) {
                                Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                                while (z.iterator().hasNext()) {
                                    FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                    String visibilidade = findNewRestaurante.getVisibilidade();
                                    String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                                    double dist = Double.parseDouble(distancia.trim().replace(",","."));
                                    double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                                    double dif = dist-area_int;
                                    if (dif <=  0){
                                        if (visibilidade.equals("Publico")) {
                                            list.add(findNewRestaurante);
                                            Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                            listIDS.add(local);
                                        } else if (visibilidade.equals("Amigos")) {
                                            if (utilizador.equals(i.getKey())) {
                                                list.add(findNewRestaurante);
                                                Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                                listIDS.add(local);
                                            } else {
                                                String amigo = i.getKey();
                                                verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                                    @Override
                                                    public void onCallback(boolean i) {
                                                        if (i) {
                                                            //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                            list.add(findNewRestaurante);
                                                            Local local = new Local(amigo, "Restaurantes", findNewRestaurante.getNome());
                                                            listIDS.add(local);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        myAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        //Lista quando carregamos no botão de cinemas!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        cinemasButton = findViewById(R.id.cinemaButton);
        cinemasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        listIDS.clear();
                        String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        for (DataSnapshot i : snapshot.getChildren()) {
                            if (i.hasChild("Estabelecimentos")) {
                                Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                                while (z.iterator().hasNext()) {
                                    FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                    String visibilidade = findNewRestaurante.getVisibilidade();
                                    String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                                    double dist = Double.parseDouble(distancia.trim().replace(",","."));
                                    double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                                    double dif = dist-area_int;
                                    if (dif <=  0){
                                        if (visibilidade.equals("Publico")) {
                                            list.add(findNewRestaurante);
                                            Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                            listIDS.add(local);
                                        } else if (visibilidade.equals("Amigos")) {
                                            if (utilizador.equals(i.getKey())) {
                                                list.add(findNewRestaurante);
                                                Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                                listIDS.add(local);
                                            } else {
                                                String amigo = i.getKey();
                                                verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                                    @Override
                                                    public void onCallback(boolean i) {

                                                        if (i) {
                                                            //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                            list.add(findNewRestaurante);
                                                            Local local = new Local(amigo, "Cinemas", findNewRestaurante.getNome());
                                                            listIDS.add(local);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        myAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }
    //VERIFICA AMIZADE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void verificarAmizade(String id_utilizador, String id_outro, FirebaseCallback firebaseCallback) {
        amigos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.child(id_utilizador).getChildren()) {
                    if (i.getKey().equals(id_outro)) {
                        //System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" + id_utilizador);
                        //System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD" + i.getKey() + id_outro);
                        firebaseCallback.onCallback(true);
                    }
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //MENU!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                startActivity(new Intent(this, Map.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, Setting.class));
                break;
            case R.id.menu_add:
                startActivity(new Intent(this, FindFriends.class));
                break;
            case R.id.menu_friends:
                startActivity(new Intent(this, UserFriends.class));
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(this, Login.class));
                break;
        }
        item.setChecked(true);
        return true;
    }

    public void Filtrar(View view) {
        openDialogFiltro();
    }

    public void openDialogFiltro() {
        Dialog_filter_PontosDeInteresse dialog = new Dialog_filter_PontosDeInteresse();
        dialog.show(getSupportFragmentManager(), "dialog");
    }





    //FILTRO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Override
    public void applyTextsFilter(String restaurantes, String cinemas, String centroComercial, int size) {
        Toast.makeText(this, restaurantes + cinemas + centroComercial + " " + size, Toast.LENGTH_SHORT).show();
        rota.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                listIDS.clear();
                String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot i : snapshot.getChildren()) {
                    if (i.hasChild("Estabelecimentos") && restaurantes.equals("Restaurantes")) {
                        if (i.child("Estabelecimentos").hasChild("Restaurantes")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                                double dist = Double.parseDouble(distancia.trim().replace(",","."));
                                double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                                double dif = dist-area_int;
                                if (dif <=  0){
                                    if (visibilidade.equals("Publico")) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                        listIDS.add(local);
                                    } else if (visibilidade.equals("Amigos")) {
                                        if (utilizador.equals(i.getKey())) {
                                            list.add(findNewRestaurante);
                                            Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                            listIDS.add(local);
                                        } else {
                                            String amigo = i.getKey();
                                            verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                                @Override
                                                public void onCallback(boolean i) {
                                                    if (i) {
                                                        //.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                        list.add(findNewRestaurante);
                                                        Local local = new Local(amigo, "Restaurantes", findNewRestaurante.getNome());
                                                        listIDS.add(local);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (i.child("Estabelecimentos").hasChild("Cinemas") && cinemas.equals("Cinemas")) {
                        Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                        while (z.iterator().hasNext()) {
                            FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                            String visibilidade = findNewRestaurante.getVisibilidade();
                            String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                            double dist = Double.parseDouble(distancia.trim().replace(",","."));
                            double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                            double dif = dist-area_int;
                            if (dif <=  0){
                                if (visibilidade.equals("Publico")) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                    listIDS.add(local);
                                } else if (visibilidade.equals("Amigos")) {
                                    if (utilizador.equals(i.getKey())) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                        listIDS.add(local);
                                    } else {
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                            @Override
                                            public void onCallback(boolean i) {

                                                if (i) {
                                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                    Local local = new Local(amigo, "Cinemas", findNewRestaurante.getNome());
                                                    listIDS.add(local);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                    System.out.println("LLLLLLLLLLLLLLLLLLLLL" + (i.child("Estabelecimentos").hasChild("Centros Comerciais") && centroComercial.equals("cComercial")));
                    if (i.child("Estabelecimentos").hasChild("Centros Comerciais") && centroComercial.equals("cComercial")) {

                        Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Centros Comerciais").getChildren();
                        while (z.iterator().hasNext()) {
                            FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                            String visibilidade = findNewRestaurante.getVisibilidade();
                            String distancia = distance(GlobalVariables.MinhaLocalizacao.latitude, GlobalVariables.MinhaLocalizacao.longitude, findNewRestaurante.getLatitude(), findNewRestaurante.getLongitude());
                            double dist = Double.parseDouble(distancia.trim().replace(",","."));
                            double area_int = Double.parseDouble(String.valueOf(GlobalVariables.AreaDeInteresse));
                            double dif = dist-area_int;
                            if (dif <=  0){
                                if (visibilidade.equals("Publico")) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                    listIDS.add(local);
                                } else if (visibilidade.equals("Amigos")) {
                                    if (utilizador.equals(i.getKey())) {
                                        list.add(findNewRestaurante);
                                        Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                        listIDS.add(local);
                                    } else {
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback() {
                                            @Override
                                            public void onCallback(boolean i) {

                                                if (i) {
                                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                    Local local = new Local(amigo, "Centros Comerciais", findNewRestaurante.getNome());
                                                    listIDS.add(local);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
                myAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    @Override
    public void onAdapterClick(int position) {
        String UserId = listIDS.get(position).getUserId();
        String place = listIDS.get(position).getPlace();
        String nome = listIDS.get(position).getNomeDoLocal();
        //System.out.println("OOOOOOOOOOOOOOOOOOOOLLLLLLLLLLLLLLLLLLLAAAAAAAAAAAAAAAAAA" + UserId);
        Intent localIntent = new Intent(this, DescricaoDoLocal.class);
        localIntent.putExtra("UserId", UserId);
        localIntent.putExtra("place", place);
        localIntent.putExtra("nome", nome);
        startActivity(localIntent);
    }

    private interface FirebaseCallback {
        void onCallback(boolean i);
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

}