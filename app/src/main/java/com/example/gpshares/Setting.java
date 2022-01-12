package com.example.gpshares;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gpshares.FriendsHelper.FindFriends;
import com.example.gpshares.PontosDeInteresseHelper.PontosDeInteresse;
import com.facebook.CustomTabMainActivity;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Objects;

import io.grpc.Context;

public class Setting extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //SideMenu--------------------------------------------------------------------------------------
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private String mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    //imagem
    ImageView imageView_S;
    //Storage
    StorageReference objectStorageReference;
    FirebaseFirestore objectFirebaseFirestore;
    EditText nameOfImage;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mAuth = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //Storage
        objectStorageReference = FirebaseStorage.getInstance().getReference(mAuth);
        objectFirebaseFirestore=FirebaseFirestore.getInstance();

        //Imagem
        imageView_S = findViewById(R.id.imageView_Settings);
        imageView_S.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseProfilePicture();
            }
        });

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA" + mAuth+".png");
        objectStorageReference.child(mAuth+".jpg").getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                imageView_S.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Setting.this, "FUUUUCK", Toast.LENGTH_SHORT).show();
            }
        });

        //SideMenu----------------------------------------------------------------------------------
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_settings);
        navigationView = (NavigationView) findViewById(R.id.navigation_viewSettings);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_Open, R.string.menu_Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //Display user info
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();
        final TextView fullNameTextView = (TextView) findViewById(R.id.textViewName);
        final TextView emailTextView = (TextView) findViewById(R.id.textViewEmail);
        final TextView identificadorTextView = (TextView) findViewById(R.id.textViewIdentificador);
        //final TextView ageTextView = (TextView) findViewById(R.id.textViewAge);
        //GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Utilizador userProfile = snapshot.getValue(Utilizador.class);
                if (userProfile != null) {
                    fullNameTextView.setText(userProfile.nomeInteiro);
                    emailTextView.setText(userProfile.email);
                    identificadorTextView.setText(userProfile.identificador);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Setting.this, "Algo correu mal a processar os seus dados", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
    private void chooseProfilePicture(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_profile_picture,null);
        builder.setCancelable(true);
        builder.setView(dialogView);

        ImageView imageViewCamera = dialogView.findViewById(R.id.imageView_dialogCamera);
        ImageView imageViewGallery = dialogView.findViewById(R.id.imageView_dialogGallery);

        final AlertDialog alertDialogProfilePicture = builder.create();
        alertDialogProfilePicture.show();

        imageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()){
                    takePictureFromCamera();
                    alertDialogProfilePicture.cancel();
                }

            }
        });

        imageViewGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureFromGallery();
                alertDialogProfilePicture.cancel();
            }
        });
    }

    private void takePictureFromGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void takePictureFromCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicture, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    Uri selectedImageUri = data.getData();
                    imageView_S.setImageURI(selectedImageUri);
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + selectedImageUri);
                    String nameOfImage = mAuth + "." + getExtension(selectedImageUri);
                    StorageReference imageRef=objectStorageReference.child(nameOfImage);
                    UploadTask objectUploadTask = imageRef.putFile(selectedImageUri);
                    objectUploadTask.continueWith(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            return imageRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Task<Uri>>() {
                        @Override
                        public void onComplete(@NonNull Task<Task<Uri>> task) {
                            if (task.isSuccessful()){
                                java.util.Map<String, String> objectMap = new HashMap<>();
                                objectMap.put("url", task.getResult().toString());
                                objectFirebaseFirestore.collection("Links").document(mAuth)
                                        .set(objectMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(Setting.this, "Golo", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Setting.this, "Fail", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }else if (!task.isSuccessful()){
                                Toast.makeText(Setting.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                break;
            case 2:
                if (resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Bitmap bitmapImage = (Bitmap) bundle.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    byte bb[] = bytes.toByteArray();
                    imageView_S.setImageBitmap(bitmapImage);
                    StorageReference sr = objectStorageReference.child(mAuth + ".jpg");
                    sr.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Setting.this, "Golo", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            java.util.Map<String, String> objectMap = new HashMap<>();
                            objectMap.put("url", task.getResult().toString());
                            objectFirebaseFirestore.collection("Links").document(mAuth)
                                    .set(objectMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(Setting.this, "Golo", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Setting.this, "Fail", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Setting.this, "Fuuuckkk", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
    }

    private boolean checkAndRequestPermissions(){
        if (Build.VERSION.SDK_INT >= 23){
            int cameraPermission = ActivityCompat.checkSelfPermission(Setting.this, Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Setting.this, new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            
        }else {
            Toast.makeText(this, "Permissões não garantidas", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadImage(View view){
        try {

        }catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getExtension(Uri uri){
        try {
            ContentResolver objectContentResolver = getContentResolver();
            MimeTypeMap objectMimeTypeMap = MimeTypeMap.getSingleton();
            return objectMimeTypeMap.getExtensionFromMimeType(objectContentResolver.getType(uri));
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}