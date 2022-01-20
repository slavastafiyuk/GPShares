package com.example.gpshares;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpshares.FriendsHelper.FindFriends;
import com.example.gpshares.PontosDeInteresseHelper.FindNewRestaurante;
import com.example.gpshares.PontosDeInteresseHelper.Local;
import com.example.gpshares.PontosDeInteresseHelper.MyAdapter;
import com.example.gpshares.PontosDeInteresseHelper.PontosDeInteresse;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Setting extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MyAdapter.onAdapterListener {
    //SideMenu--------------------------------------------------------------------------------------
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    MyAdapter myAdapter;
    ArrayList<FindNewRestaurante> list;
    private String mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private ByteArrayOutputStream bytes;
    private Bitmap bitmap;
    //imagem
    private ImageView imageView_S;
    //Storage
    private StorageReference objectStorageReference;
    private FirebaseFirestore objectFirebaseFirestore;
    private EditText nameOfImage;
    private RecyclerView recyclerView;
    private DatabaseReference rota;
    ArrayList<Local> listIDS;
    private DatabaseReference Local_Ref_place;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mAuth = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        //Storage
        objectStorageReference = FirebaseStorage.getInstance().getReference(mAuth);
        objectFirebaseFirestore = FirebaseFirestore.getInstance();
        //Imagem
        imageView_S = findViewById(R.id.imageView_Settings);
        imageView_S.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseProfilePicture();
            }
        });
        imageView_S.setImageBitmap(GlobalVariables.imagemPerfil);
        //SideMenu----------------------------------------------------------------------------------
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_settings);
        navigationView = (NavigationView) findViewById(R.id.navigation_viewSettings);
        navigationView.setNavigationItemSelectedListener(this);
        //MUDAR IMAGEM DO HEADER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        View headerView = navigationView.getHeaderView(0);
        ImageView imagemMenu = (ImageView) headerView.findViewById(R.id.imagemMenuPerfil);
        imagemMenu.setImageBitmap(GlobalVariables.imagemPerfil);
        TextView nomeDoUtilizador = (TextView) headerView.findViewById(R.id.NomeHeader);
        nomeDoUtilizador.setText(GlobalVariables.nomeUtilizador);
        TextView identificadorDoUtilizador = (TextView) headerView.findViewById(R.id.IdentificadorHeader);
        identificadorDoUtilizador.setText(GlobalVariables.nomeUtilizador);
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
        fullNameTextView.setText(GlobalVariables.nomeUtilizador);
        emailTextView.setText(GlobalVariables.formaAuth);
        identificadorTextView.setText(GlobalVariables.identificador);

        //Lista dos meus lugares
        listIDS = new ArrayList<>();
        rota = FirebaseDatabase.getInstance().getReference("Users");
        list = new ArrayList<>();
        recyclerView = findViewById(R.id.meus_locais);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter(this, list, this);
        recyclerView.setAdapter(myAdapter);


        rota.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                listIDS.clear();
                String utilizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot i : snapshot.getChildren()) {
                    if (i.hasChild("Estabelecimentos")) {
                        if (i.child("Estabelecimentos").hasChild("Restaurantes")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Restaurantes").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                if (utilizador.equals(i.getKey())) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Restaurantes", findNewRestaurante.getNome());
                                    listIDS.add(local);
                                }
                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Cinemas")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Cinemas").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                if (utilizador.equals(i.getKey())) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Cinemas", findNewRestaurante.getNome());
                                    listIDS.add(local);
                                }

                            }
                        }
                        if (i.child("Estabelecimentos").hasChild("Centros Comerciais")) {
                            Iterable<DataSnapshot> z = i.child("Estabelecimentos").child("Centros Comerciais").getChildren();
                            while (z.iterator().hasNext()) {
                                FindNewRestaurante findNewRestaurante = z.iterator().next().getValue(FindNewRestaurante.class);
                                if (utilizador.equals(i.getKey())) {
                                    list.add(findNewRestaurante);
                                    Local local = new Local(i.getKey(), "Centros Comerciais", findNewRestaurante.getNome());
                                    listIDS.add(local);
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
            case R.id.menu_friends:
                startActivity(new Intent(this, UserFriends.class));
                break;
        }
        item.setChecked(true);
        return true;
    }

    private void chooseProfilePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_profile_picture, null);
        builder.setCancelable(true);
        builder.setView(dialogView);

        ImageView imageViewCamera = dialogView.findViewById(R.id.imageView_dialogCamera);
        ImageView imageViewGallery = dialogView.findViewById(R.id.imageView_dialogGallery);

        final AlertDialog alertDialogProfilePicture = builder.create();
        alertDialogProfilePicture.show();

        imageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
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

    private void takePictureFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void takePictureFromCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        bytes = new ByteArrayOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
                    byte bb[] = bytes.toByteArray();
                    GlobalVariables.imagemPerfil = bitmap;
                    imageView_S.setImageBitmap(bitmap);
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
                            objectFirebaseFirestore.collection(mAuth).document(mAuth)
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
                            Toast.makeText(Setting.this, "ERRO", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmapImage = (Bitmap) bundle.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
                    byte bb[] = bytes.toByteArray();
                    GlobalVariables.imagemPerfil = bitmapImage;
                    imageView_S.setImageBitmap(GlobalVariables.imagemPerfil);
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
                            objectFirebaseFirestore.collection(mAuth).document(mAuth)
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
                            Toast.makeText(Setting.this, "ERRO", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = ActivityCompat.checkSelfPermission(Setting.this, Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Setting.this, new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(this, "Permissões não garantidas", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAdapterClick(int position) {
        String UserId = listIDS.get(position).getUserId();
        String place = listIDS.get(position).getPlace();
        String nome = listIDS.get(position).getNomeDoLocal();
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_meus_locais, null);
        AutoCompleteTextView autoCompleteTextView;
        autoCompleteTextView = dialogView.findViewById(R.id.alterar_visibilidade_adapter);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getApplicationContext(), R.array.tipo_visibilidade, R.layout.dropdown_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCompleteTextView.setAdapter(adapter3);
        builder.setCancelable(true);
        builder.setView(dialogView);
        Local_Ref_place = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId).child("Estabelecimentos").child(place).child(nome);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (autoCompleteTextView.getText().toString().equals("Publico/Privado ...")){
                    autoCompleteTextView.setError("Necessida de decidir a visibilidade");
                    autoCompleteTextView.requestFocus();
                }else{
                    Local_Ref_place.child("visibilidade").setValue(autoCompleteTextView.getText().toString());
                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNeutralButton("Vista Geral", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent localIntent = new Intent(getApplicationContext(), DescricaoDoLocal.class);
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAA" + UserId + " " + place + " " + nome);
                localIntent.putExtra("UserId", UserId);
                localIntent.putExtra("place", place);
                localIntent.putExtra("nome", nome);
                startActivity(localIntent);
            }
        });
        final AlertDialog alertDialogProfilePicture = builder.create();
        alertDialogProfilePicture.show();

    }


}