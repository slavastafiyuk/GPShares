package com.example.gpshares.FriendsHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gpshares.GlobalVariables;
import com.example.gpshares.Login;
import com.example.gpshares.Map;
import com.example.gpshares.OtherUserProfile;
import com.example.gpshares.PontosDeInteresseHelper.PontosDeInteresse;
import com.example.gpshares.R;
import com.example.gpshares.Setting;
import com.example.gpshares.UserFriends;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindFriends extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FriendsAdapter.onFriendsListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton SearchButton;
    private TextInputEditText SearchInput;
    private RecyclerView SearchResult;
    private DatabaseReference allUsersDatabaseRef;
    private FirebaseAuth mAuth;
    private String meuID, friendID;
    ArrayList<FindNewFriends> list;
    ArrayList<String> list2;
    FriendsAdapter friendsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        //Nav
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_find_friends);
        navigationView = (NavigationView) findViewById(R.id.navigation_view_FindFriends);
        navigationView.setNavigationItemSelectedListener(this);
        //MUDAR IMAGEM DO HEADER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        View headerView = navigationView.getHeaderView(0);
        ImageView imagemMenu = (ImageView) headerView.findViewById(R.id.imagemMenuPerfil);
        imagemMenu.setImageBitmap(GlobalVariables.imagemPerfil);
        TextView nomeDoUtilizador = (TextView) headerView.findViewById(R.id.NomeHeader);
        nomeDoUtilizador.setText(GlobalVariables.nomeUtilizador);
        TextView identificadorDoUtilizador = (TextView) headerView.findViewById(R.id.IdentificadorHeader);
        identificadorDoUtilizador.setText(GlobalVariables.identificador);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mAuth = FirebaseAuth.getInstance();
        meuID = mAuth.getCurrentUser().getUid();
        friendID = mAuth.getCurrentUser().getUid();
        //RecyclerView, Janela de Resultados
        SearchResult = (RecyclerView) findViewById(R.id.searchResult);
        SearchResult.setHasFixedSize(true);
        SearchResult.setLayoutManager(new LinearLayoutManager(this));
        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        list = new ArrayList<>();
        list2 = new ArrayList<>();
        friendsAdapter =new FriendsAdapter(this,list, this);
        SearchResult.setAdapter(friendsAdapter);
        //Menu de Pesquisa
        SearchButton = (ImageButton) findViewById(R.id.searchButton);
        SearchInput = (TextInputEditText) findViewById(R.id.searchBoxInput);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String SearchBoxInput = SearchInput.getText().toString();
                    SearchFriends(SearchBoxInput);
            }
        });

    }

    private void SearchFriends(String searchBoxInput) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();
        allUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                list2.clear();
                for (DataSnapshot i : snapshot.getChildren()){
                    FindNewFriends findnewfriends = i.getValue(FindNewFriends.class);
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXX" + i.child("identificador").getValue() + " " + searchBoxInput);
                    String id = i.getKey();
                    if (i.child("identificador").getValue().equals(searchBoxInput)){
                        if (meuID.equals(id)){
                            Toast.makeText(FindFriends.this, "Não consegue adicionar a sí proprio", Toast.LENGTH_SHORT).show();
                        }else if(friendID.equals(id)){
                            Toast.makeText(FindFriends.this, "Esse utilizador já é seu amigo", Toast.LENGTH_SHORT).show();
                        }else{
                            list.add(findnewfriends);
                            list2.add(i.getKey());
                        }
                    }
                    //if ()){
                    //    Toast.makeText(FindFriends.this, "Não encontramos o utilizador solicitado, verifique se introduziu codigo correto", Toast.LENGTH_SHORT).show();
                    //}
                }
                friendsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onFriendsClick(int position) {
        String visitUserId = list2.get(position);
        System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" + visitUserId);
        Intent profileIntent = new Intent(FindFriends.this, OtherUserProfile.class);
        profileIntent.putExtra("visitUserId",visitUserId);
        startActivity(profileIntent);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                startActivity(new Intent(this, Map.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, Setting.class));
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
                break;
        }
        item.setChecked(true);
        return true;
    }
}