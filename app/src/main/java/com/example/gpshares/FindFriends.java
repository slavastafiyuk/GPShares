package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gpshares.FriendsHelper.FindNewFriends;
import com.example.gpshares.FriendsHelper.FriendsAdapter;
import com.example.gpshares.PontosDeInteresseHelper.FindNewRestaurante;
import com.example.gpshares.PontosDeInteresseHelper.MyAdapter;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class FindFriends extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton SearchButton;
    private TextInputEditText SearchInput;
    private RecyclerView SearchResult;
    private DatabaseReference allUsersDatabaseRef;
    ArrayList<FindNewFriends> list;
    FriendsAdapter friendsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        //Nav
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_find_friends);
        navigationView = (NavigationView) findViewById(R.id.navigation_view_FindFriends);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //RecyclerView, Janela de Resultados
        SearchResult = (RecyclerView) findViewById(R.id.searchResult);
        SearchResult.setHasFixedSize(true);
        SearchResult.setLayoutManager(new LinearLayoutManager(this));
        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        list = new ArrayList<>();
        friendsAdapter =new FriendsAdapter(this,list);
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
                for (DataSnapshot i : snapshot.getChildren()){
                    FindNewFriends findnewfriends = i.getValue(FindNewFriends.class);
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXX" + i.child("nomeInteiro").getValue() + " " + searchBoxInput);
                    if (i.child("nomeInteiro").getValue().equals(searchBoxInput)){
                        list.add(findnewfriends);
                    }
                }
                friendsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Query searchFriendsQuery = allUsersDatabaseRef.orderByChild("nomeInteiro")
        //        .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA" + " " + allUsersDatabaseRef.child("Dados").child("nomeInteiro"));
        //FirebaseRecyclerAdapter<FindNewFriends, FindNewFriendsViewHolder> firebaseRecyclerAdapter
        //        = new FirebaseRecyclerAdapter<FindNewFriends, FindNewFriendsViewHolder>(
        //                FindNewFriends.class,
        //                R.layout.all_users_layout,
        //                FindNewFriendsViewHolder.class,
        //                searchFriendsQuery
        //) {
        //    @Override
        //    protected void populateViewHolder(FindNewFriendsViewHolder findNewFriendsViewHolder, FindNewFriends findNewFriends, int i) {
        //        findNewFriendsViewHolder.setNomeInteiro(findNewFriends.getNomeInteiro());
        //        findNewFriendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                String visitUserId = getRef(i).getKey();
        //                Intent profileIntent = new Intent(FindFriends.this, OtherUserProfile.class);
        //                profileIntent.putExtra("visitUserId",visitUserId);
        //                startActivity(profileIntent);
        //            }
        //        });
        //    }
        //};
        //SearchResult.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindNewFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindNewFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNomeInteiro(String nomeInteiro){
            TextView myName = (TextView) mView.findViewById(R.id.allUsersFullNames);
            myName.setText(nomeInteiro);
        }
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
            case R.id.nav_pontos_de_interesse:
                startActivity(new Intent(this, PontosDeInteresse.class));
                break;
        }
        item.setChecked(true);
        return true;
    }
}