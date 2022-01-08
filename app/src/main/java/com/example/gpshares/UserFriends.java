package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.gpshares.FriendsHelper.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserFriends extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        myFriendList = (RecyclerView) findViewById(R.id.FriendList);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(Friends.class,
                        R.layout.all_users_layout, FriendsViewHolder.class, FriendsRef) {
            @Override
            protected void populateViewHolder(FriendsViewHolder friendsViewHolder, Friends friends, int i) {
                friendsViewHolder.setDate(friends.getDate());
                final String usersIds = getRef(i).getKey();
                UsersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            final String userName = snapshot.child("nomeInteiro").getValue().toString();
                            friendsViewHolder.setNomeInteiro(userName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNomeInteiro(String nomeInteiro){
            TextView myName = (TextView) mView.findViewById(R.id.allUsersFullNames);
            myName.setText(nomeInteiro);
        }

        public void setDate(String date) {
            TextView friendsDate = (TextView) mView.findViewById(R.id.allUsersStatus);
            friendsDate.setText("Amigos desde:< " + date);
        }
    }
}