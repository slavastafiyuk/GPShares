package com.example.gpshares.PontosDeInteresseHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.example.gpshares.Login;
import com.example.gpshares.Map;
import com.example.gpshares.R;
import com.example.gpshares.Setting;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PontosDeInteresse extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Dialog_filter_PontosDeInteresse.DialogListenerFilter, MyAdapter.onAdapterListener {
    //SideMenu--------------------------------------------------------------------------------------
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private Button restaurantesButton, cinemasButton;
    private DatabaseReference rota, amigos;
    private RecyclerView searchResults;
    ArrayList<FindNewRestaurante> list;
    ArrayList<String> listIDS;
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
        listIDS = new ArrayList<>();
        list = new ArrayList<>();
        searchResults = (RecyclerView) findViewById(R.id.searchResultRestaurantes);
        searchResults.setHasFixedSize(true);
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        //list = new ArrayList<>();
        myAdapter =new MyAdapter(this, list, this);
        searchResults.setAdapter(myAdapter);
        restaurantesButton=findViewById(R.id.buttonRestaurantes);
        //Lista inicial
        rota.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                listIDS.clear();
                String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot i : snapshot.getChildren()){
                    if (i.hasChild("Estabelecimentos")){
                        if (i.child("Estabelecimentos").hasChild("Restaurantes")){
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                            while(z.iterator().hasNext()){
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + i.getKey());
                                System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" + findNewRestaurante.getNome());
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")){
                                    list.add(findNewRestaurante);
                                    listIDS.add(i.getKey());
                                }else if (visibilidade.equals("Amigos")){
                                    if (utilizador.equals(i.getKey())){
                                        list.add(findNewRestaurante);
                                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + i.getKey());
                                        listIDS.add(i.getKey());
                                    }else{
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback(){
                                            @Override
                                            public void onCallback(boolean i) {
                                                if (i){
                                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                    listIDS.add(amigo);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Cinemas")){
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                            while(z.iterator().hasNext()){
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")){
                                    list.add(findNewRestaurante);
                                    listIDS.add(i.getKey());
                                }else if (visibilidade.equals("Amigos")){
                                    if (utilizador.equals(i.getKey())){
                                        list.add(findNewRestaurante);
                                        listIDS.add(i.getKey());
                                    }else{
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback(){
                                            @Override
                                            public void onCallback(boolean i) {

                                                if (i){
                                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                    listIDS.add(amigo);
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
        //Lista quando carregamos no botão de restaurantes
        restaurantesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        listIDS.clear();
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
                                        listIDS.add(i.getKey());
                                    }else if (visibilidade.equals("Amigos")){
                                        if (utilizador.equals(i.getKey())){
                                            list.add(findNewRestaurante);
                                            listIDS.add(i.getKey());
                                        }else{
                                            String amigo = i.getKey();
                                            verificarAmizade(utilizador, amigo, new FirebaseCallback(){
                                                @Override
                                                public void onCallback(boolean i) {
                                                    if (i){
                                                        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                        list.add(findNewRestaurante);
                                                        listIDS.add(amigo);
                                                    }
                                                }
                                            });
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
        //Lista quando carregamos no botão de cinemas
        cinemasButton=findViewById(R.id.cinemaButton);
        cinemasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rota.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        listIDS.clear();
                        String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        for (DataSnapshot i : snapshot.getChildren()){
                            if (i.hasChild("Estabelecimentos")){
                                Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                                while(z.iterator().hasNext()){
                                    FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                    String visibilidade = findNewRestaurante.getVisibilidade();
                                    if (visibilidade.equals("Publico")){
                                        list.add(findNewRestaurante);
                                        listIDS.add(i.getKey());
                                    }else if (visibilidade.equals("Amigos")){
                                        if (utilizador.equals(i.getKey())){
                                            list.add(findNewRestaurante);
                                            listIDS.add(i.getKey());
                                        }else{
                                            String amigo = i.getKey();
                                            verificarAmizade(utilizador, amigo, new FirebaseCallback(){
                                                @Override
                                                public void onCallback(boolean i) {

                                                    if (i){
                                                        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                        list.add(findNewRestaurante);
                                                        listIDS.add(amigo);
                                                    }
                                                }
                                            });
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
    public void verificarAmizade(String id_utilizador, String id_outro, FirebaseCallback firebaseCallback){
        amigos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.child(id_utilizador).getChildren()){
                    if (i.getKey().equals(id_outro)){
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
        }
        item.setChecked(true);
        return true;
    }
    public void Filtrar(View view) {
        openDialogFiltro();
    }
    public void openDialogFiltro(){
        Dialog_filter_PontosDeInteresse dialog = new Dialog_filter_PontosDeInteresse();
        dialog.show(getSupportFragmentManager(), "dialog");
    }
    @Override
    public void applyTextsFilter(String restaurantes, String cinemas, int size) {
        Toast.makeText(this, restaurantes + cinemas +" "+size, Toast.LENGTH_SHORT).show();

        rota.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                listIDS.clear();
                String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot i : snapshot.getChildren()){
                    if (i.hasChild("Estabelecimentos") && restaurantes.equals("Restaurantes")){
                        if (i.child("Estabelecimentos").hasChild("Restaurantes")){
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                            while(z.iterator().hasNext()){
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")){
                                    list.add(findNewRestaurante);
                                    listIDS.add(i.getKey());
                                }else if (visibilidade.equals("Amigos")){
                                    if (utilizador.equals(i.getKey())){
                                        list.add(findNewRestaurante);
                                        listIDS.add(i.getKey());
                                    }else{
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback(){
                                            @Override
                                            public void onCallback(boolean i) {
                                                if (i){
                                                    //.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                    listIDS.add(amigo);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Cinemas") && cinemas.equals("Cinemas")){
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                            while(z.iterator().hasNext()){
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                String visibilidade = findNewRestaurante.getVisibilidade();
                                if (visibilidade.equals("Publico")){
                                    list.add(findNewRestaurante);
                                    listIDS.add(i.getKey());
                                }else if (visibilidade.equals("Amigos")){
                                    if (utilizador.equals(i.getKey())){
                                        list.add(findNewRestaurante);
                                        listIDS.add(i.getKey());
                                    }else{
                                        String amigo = i.getKey();
                                        verificarAmizade(utilizador, amigo, new FirebaseCallback(){
                                            @Override
                                            public void onCallback(boolean i) {

                                                if (i){
                                                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + findNewRestaurante.getAvaliacao());
                                                    list.add(findNewRestaurante);
                                                    listIDS.add(amigo);
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
        //String visitUserId = listIDS.get(position);
        System.out.println("OOOOOOOOOOOOOOOOOOOOLLLLLLLLLLLLLLLLLLLAAAAAAAAAAAAAAAAAA");
        Intent localIntent = new Intent(this, DescricaoDoLocal.class);
        //localIntent.putExtra("visitUserId", visitUserId);
        startActivity(localIntent);
    }

    private interface FirebaseCallback{
        void onCallback(boolean i);
    }

}