package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OtherUserProfile extends AppCompatActivity {

    private TextView name;
    private Button sendFriendRequest, declineFriendRequest;

    private DatabaseReference friendRequestRef, userRef, friendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, currentState, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        IntializeFields();

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String otherUserName = snapshot.child("nomeInteiro").getValue().toString();
                    name.setText(otherUserName);

                    maintananceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        declineFriendRequest.setVisibility(View.INVISIBLE);
        declineFriendRequest.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){
            sendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendFriendRequest.setEnabled(false);

                    if (currentState.equals("not_friends")){
                        sendFriendRequestToaPerson();
                    }if (currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }if (currentState.equals("request_received")){
                        acceptFriendRequest();
                    }if (currentState.equals("friends")){
                        unfriend();
                    }
                }
            });
        }else{
            sendFriendRequest.setVisibility(View.INVISIBLE);
            declineFriendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void unfriend() {
        friendsRef.child(senderUserId).child(receiverUserId).removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(receiverUserId).child(senderUserId).removeValue().
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendFriendRequest.setEnabled(true);
                                                currentState = "not_friends";
                                                sendFriendRequest.setText("Pedir Amizade");
                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                declineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        friendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserId).child(senderUserId).child("date").
                                    setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUserId).child(receiverUserId).removeValue().
                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    friendRequestRef.child(receiverUserId)
                                                                            .child(senderUserId).removeValue().
                                                                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        sendFriendRequest.setEnabled(true);
                                                                                        currentState = "friends";
                                                                                        sendFriendRequest.setText("Terminar Amizade");
                                                                                        declineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                        declineFriendRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId).removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).child(senderUserId).removeValue().
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendFriendRequest.setEnabled(true);
                                currentState = "not_friends";
                                sendFriendRequest.setText("Pedir Amizade");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void maintananceOfButtons() {
        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        currentState = "request_sent";
                        sendFriendRequest.setText("Cancelar Pedido");
                        declineFriendRequest.setVisibility(View.INVISIBLE);
                        declineFriendRequest.setEnabled(false);
                    }else if(request_type.equals("received")){
                        currentState = "request_received";
                        sendFriendRequest.setText("Aceitar Pedido");
                        declineFriendRequest.setVisibility(View.VISIBLE);
                        declineFriendRequest.setEnabled(true);
                        declineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelFriendRequest();
                            }
                        });
                    }
                }else{
                    friendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                currentState = "friends";
                                sendFriendRequest.setText("Terminar Amizade");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendFriendRequestToaPerson() {
        friendRequestRef.child(senderUserId).child(receiverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).child(senderUserId).child("request_type")
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendFriendRequest.setEnabled(true);
                                currentState = "request_sent";
                                sendFriendRequest.setText("Cancelar Pedido");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void IntializeFields() {

        name = (TextView) findViewById(R.id.OtherUserFullName);
        sendFriendRequest = (Button) findViewById(R.id.SendFriendRequest);
        declineFriendRequest = (Button) findViewById(R.id.DeclineFriendRequest);
        currentState = "not_friends";
    }
}