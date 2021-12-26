package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OtherUserProfile extends AppCompatActivity {

    private TextView name;
    private Button sendFriendRequest, declineFriendRequest;

    private DatabaseReference profileUserRef, userRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mAuth = FirebaseAuth.getInstance();

        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        IntializeFields();

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String otherUserName = snapshot.child("nomeInteiro").getValue().toString();
                    name.setText(otherUserName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void IntializeFields() {

        name = (TextView) findViewById(R.id.OtherUserFullName);
        sendFriendRequest = (Button) findViewById(R.id.SendFriendRequest);
        declineFriendRequest = (Button) findViewById(R.id.DeclineFriendRequest);
    }
}