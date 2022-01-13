package com.example.gpshares.loadHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.gpshares.GlobalVariables;
import com.example.gpshares.Login;
import com.example.gpshares.Map;
import com.example.gpshares.R;
import com.example.gpshares.Setting;
import com.example.gpshares.Utilizador;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProgressBarAnimation extends Animation {
    private Context context;
    private ProgressBar progressBar;
    private TextView textView;
    private float from;
    private float to;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private String userID;
    private StorageReference objectStorageReference;


    public ProgressBarAnimation(Context context, ProgressBar progressBar, TextView textView, float from, float to){
        this.context = context;
        this.progressBar = progressBar;
        this.textView = textView;
        this.from = from;
        this.to = to;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        if (user != null) {
            userID = user.getUid();
            objectStorageReference = FirebaseStorage.getInstance().getReference(userID);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
            textView.setText((int) value + " %");
            reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Utilizador userProfile = snapshot.getValue(Utilizador.class);
                    if (userProfile != null) {
                        GlobalVariables.nomeUtilizador = userProfile.nomeInteiro;
                        GlobalVariables.formaAuth = userProfile.email;
                        GlobalVariables.identificador = userProfile.identificador;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            objectStorageReference.child(userID + ".jpg").getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    GlobalVariables.imagemPerfil = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.unknowuser);
                }
            });
            if (value == to) {

                if (user.getEmail().equals("")) {
                    context.startActivities(new Intent[]{new Intent(context, Map.class)});
                } else if (user.isEmailVerified()) {
                    context.startActivities(new Intent[]{new Intent(context, Map.class)});
                } else if (!user.isEmailVerified()) {
                    context.startActivities(new Intent[]{new Intent(context, Login.class)});
                }
            }
        }
        if (user == null) {
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
            textView.setText((int) value + " %");
            if (value == to) {
                context.startActivities(new Intent[]{new Intent(context, Login.class)});
            }
        }
    }
}
