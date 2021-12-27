package com.example.gpshares;

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
    private DatabaseReference rota, amigos;
    private RecyclerView searchResults;
    ArrayList<FindNewRestaurante> list;
    MyAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontos_de_interesse);
        rota = FirebaseDatabase.getInstance().getReference("Users");
        amigos = FirebaseDatabase.getInstance().getReference("Friends");
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
                        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + snapshot.getChildren().iterator().next().getKey());
                        String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        for (DataSnapshot i : snapshot.getChildren()){
                            //System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBB" + " " + snapshot.getChildren());
                            if (i.hasChild("Estabelecimentos")){
                                Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                                while(z.iterator().hasNext()){
                                    FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + findNewRestaurante.getVisibilidade());
                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    String visibilidade = findNewRestaurante.getVisibilidade();
                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + visibilidade + " " + i.getKey());
                                    if (visibilidade.equals("Publico")){
                                        list.add(findNewRestaurante);
                                    }else if (visibilidade.equals("Amigos")){
                                        verificarAmizade(utilizador, i.getKey(), new FirebaseCallback(){
                                            @Override
                                            public void onCallback(boolean i) {
                                                if (i){
                                                    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                }
                                            }
                                        });
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
        cinemasButton=findViewById(R.id.cinemaButton);
        cinemasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + snapshot.getChildren().iterator().next().getKey());
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
    public void verificarAmizade(String id_utilizador, String id_outro, FirebaseCallback firebaseCallback){
        amigos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.child(id_utilizador).getChildren()){
                    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA" + " " + i + "id_utilizador:" + " " + id_utilizador + "od_outro: " + id_outro);
                    System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBB" + " " + i.getKey());
                    if (i.getKey().equals(id_outro)){
                        firebaseCallback.onCallback(false);
                    }
                }
                myAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPP ");
    }


    private interface FirebaseCallback{
        void onCallback(boolean i);
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