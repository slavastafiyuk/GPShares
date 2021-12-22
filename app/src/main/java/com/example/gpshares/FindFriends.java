package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FindFriends extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton SearchButton;
    private EditText SearchInput;
    private RecyclerView SearchResult;
    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //Nav
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_find_friends);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
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

        //Menu de Pesquisa
        SearchButton = (ImageButton) findViewById(R.id.searchButton);
        SearchInput = (EditText) findViewById(R.id.searchBoxInput);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String SearchBoxInput = SearchInput.getText().toString();
                    SearchFriends(SearchBoxInput);
            }
        });

    }

    private void SearchFriends(String searchBoxInput)
    {
        FirebaseRecyclerAdapter<FindNewFriends, FindNewFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindNewFriends, FindNewFriendsViewHolder>(
                        FindNewFriends.class,
                        R.layout.all_users_layout,
                        FindNewFriendsViewHolder.class,
                        allUsersDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(FindNewFriendsViewHolder findNewFriendsViewHolder, FindNewFriends findNewFriends, int i) {

            }
        };
    }

    public static class FindNewFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindNewFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}