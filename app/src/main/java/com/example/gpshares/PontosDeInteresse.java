package com.example.gpshares;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpshares.PontosDeInteresseHelper.FindNewRestaurante;
import com.example.gpshares.PontosDeInteresseHelper.MyAdapter;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PontosDeInteresse extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //SideMenu--------------------------------------------------------------------------------------
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private Button restaurantesButton, cinemasButton;
    private DatabaseReference rota;
    private RecyclerView searchResults;
    ArrayList<FindNewRestaurante> list;
    MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontos_de_interesse);
        rota = FirebaseDatabase.getInstance().getReference("Users");
        //SideMenu----------------------------------------------------------------------------------
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_pontosDeInteresse);
        navigationView = (NavigationView) findViewById(R.id.navigation_viewPontosDeInteresse);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //RecyclerView, Janela dos resultados dos restaurantes
        list = new ArrayList<>();
        searchResults = (RecyclerView) findViewById(R.id.searchResultRestaurantes);
        searchResults.setHasFixedSize(true);
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        myAdapter =new MyAdapter(this,list);
        searchResults.setAdapter(myAdapter);
        restaurantesButton=findViewById(R.id.buttonRestaurantes);
        restaurantesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + snapshot.getChildren().iterator().next().getKey());
                        for (DataSnapshot i : snapshot.getChildren()){
                            if (i.hasChild("Estabelecimentos")){
                                Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                                while(z.iterator().hasNext()){
                                    FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                    list.add(findNewRestaurante);
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
        cinemasButton=findViewById(R.id.cinemaButton);
        cinemasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + snapshot.getChildren().iterator().next().getKey());
                        for (DataSnapshot i : snapshot.getChildren()){
                            if (i.hasChild("Estabelecimentos")){
                                Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                                while(z.iterator().hasNext()){
                                    FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                    list.add(findNewRestaurante);
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
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(this, Login.class));
                break;
            //case R.id.nav_pontos_de_interesse:
            //    startActivity(new Intent(this, PontosDeInteresse.class));
            //    break;
        }
        item.setChecked(true);
        return true;
    }
}