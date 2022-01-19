package com.example.gpshares;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gpshares.FriendsHelper.FindFriends;
import com.example.gpshares.PontosDeInteresseHelper.PontosDeInteresse;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

//
public class DescricaoDoLocal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    Button submitRate;
    RatingBar ratingStars;
    private TextView nome;
    private FirebaseAuth mAuth;
    private DatabaseReference Local_Ref, Post_ref, Rate_Ref, Local_Ref_place;
    private String idDoUtilizador, userID, local, nome_local;
    private StorageReference objectStorageReference;
    private ImageView imageView;
    private FloatingActionButton floatingActionButton, floatingRateButton, floatingReportButton;
    private RecyclerView CommentList;
    private ImageButton postComment;
    private EditText commentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descricao_do_local);

        CommentList = (RecyclerView) findViewById(R.id.commentList);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);
        commentInput = (EditText) findViewById(R.id.addComment);
        postComment = (ImageButton) findViewById(R.id.sendComment);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        idDoUtilizador = getIntent().getExtras().get("UserId").toString();
        local = getIntent().getExtras().get("place").toString();
        nome_local = getIntent().getExtras().get("nome").toString();
        Local_Ref = FirebaseDatabase.getInstance().getReference().child("Users");
        Local_Ref_place = Post_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(idDoUtilizador).child("Estabelecimentos").child(local).child(nome_local);
        Post_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(idDoUtilizador).child("Estabelecimentos").child(local).child(nome_local).child("Comments");
        Rate_Ref = FirebaseDatabase.getInstance().getReference().child("Users").child(idDoUtilizador).child("Estabelecimentos").child(local).child(nome_local).child("OutrasAvaliacoes");
        nome = findViewById(R.id.Nome_Do_Local);
        imageView = findViewById(R.id.imageView_Local);
        firebaseFirestore = FirebaseFirestore.getInstance();
        floatingActionButton = findViewById(R.id.make_a_route);

        floatingRateButton = findViewById(R.id.avaliarButton);
        floatingRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogRate();
            }
        });

        floatingReportButton = findViewById(R.id.reportButton);
        floatingReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogReport();
            }
        });

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Local_Ref.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String userName = snapshot.child("nomeInteiro").getValue().toString();
                            validateComment(userName);
                            commentInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        Local_Ref.child(idDoUtilizador).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                objectStorageReference = FirebaseStorage.getInstance().getReference(idDoUtilizador);
                String caminho_da_imagem = snapshot.child("Estabelecimentos").child(local).child(nome_local).child("imagem").getValue().toString();
                System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" + " " + idDoUtilizador);
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(objectStorageReference.child(caminho_da_imagem))
                        .centerCrop()
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                System.out.println("ola");
                                imageView.setImageBitmap(resource);
                            }
                        });
                System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" + nome_local);
                String coment_do_local = snapshot.child("Estabelecimentos").child(local).child(nome_local).child("comentario").getValue().toString();
                nome.setText(coment_do_local);
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double latitude = Float.parseFloat(snapshot.child("Estabelecimentos").child(local).child(nome_local).child("latitude").getValue().toString());
                        double longitude = Float.parseFloat(snapshot.child("Estabelecimentos").child(local).child(nome_local).child("longitude").getValue().toString());
                        System.out.println("ASDASDASD" + latitude + longitude);
                        Intent localIntent = new Intent(getApplicationContext(), Map.class);
                        LatLng place_de_interesse = new LatLng(latitude, longitude);
                        GlobalVariables.PontoDeInteresse = place_de_interesse;
                        startActivity(localIntent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        drawerLayout = findViewById(R.id.drawerlayout_Desc_De_Local);
        navigationView = findViewById(R.id.navigation_viewDesc_De_Local);
        navigationView.setNavigationItemSelectedListener(this);
        //MUDAR IMAGEM DO HEADER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        System.out.println("BITMAP" + GlobalVariables.imagemPerfil);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        Post_ref
        ) {
            @Override
            protected void populateViewHolder(CommentsViewHolder commentsViewHolder, Comments comments, int i) {
                commentsViewHolder.setNomeInteiro(comments.getNomeInteiro());
                commentsViewHolder.setComment(comments.getComment());
                commentsViewHolder.setDate(comments.getDate());
            }
        };
        CommentList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNomeInteiro(String nomeInteiro) {
            TextView userName = (TextView) mView.findViewById(R.id.commentUserName);
            userName.setText(nomeInteiro + " ");
        }

        public void setComment(String comment) {
            TextView CommentText = (TextView) mView.findViewById(R.id.commentText);
            CommentText.setText(comment);
        }

        public void setDate(String date) {
            TextView data = (TextView) mView.findViewById(R.id.commentDate);
            data.setText(date + " ");
        }
    }

    private void validateComment(String userName) {
        String commentText = commentInput.getText().toString();
        if (TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "O comentário não tem texto...", Toast.LENGTH_SHORT).show();
        }else{
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy 'ás' HH:mm:ss");
            final String saveCurrentDate =  currentDate.format(callForDate.getTime());
            final String randomKey = userID + saveCurrentDate;
            HashMap commentsMap = new HashMap();
            commentsMap.put("idUtilizador", userID);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("nomeInteiro", userName);
            Post_ref.child(randomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(DescricaoDoLocal.this, "Comentário enviado", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DescricaoDoLocal.this, "Erro ao enviar, tente de novo.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public void openDialogRate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DescricaoDoLocal.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_rate,null);
        builder.setCancelable(true);
        builder.setView(dialogView);
        submitRate = dialogView.findViewById(R.id.sentRate);
        ratingStars = dialogView.findViewById(R.id.rantingBar);
        submitRate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Local_Ref.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String userName = snapshot.child("nomeInteiro").getValue().toString();
                            validateRate();
                            ratingStars.setRating(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        final AlertDialog alert_rate = builder.create();
        alert_rate.show();
    }

    private void validateRate() {
        float Rate = ratingStars.getRating();
        if (Rate == 0){
            Toast.makeText(this, "A Avaliação não pode ser 0...", Toast.LENGTH_SHORT).show();
        }else{
            final String Key = userID;
            HashMap RateMap = new HashMap();
            RateMap.put("Nota", Rate);
            Rate_Ref.child(Key).updateChildren(RateMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(DescricaoDoLocal.this, "Avaliação enviada com sucesso.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DescricaoDoLocal.this, "Erro ao enviar", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        Rate_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(idDoUtilizador)){
                    Local_Ref_place.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String valor_da_avaliacao = snapshot.child("avaliacao").getValue().toString();
                            HashMap aval = new HashMap();
                            aval.put("Nota", valor_da_avaliacao);
                            Rate_Ref.child(idDoUtilizador).setValue(aval);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
                Iterator<DataSnapshot> k = snapshot.getChildren().iterator();
                int count = 0;
                float z = 0;
                while (k.hasNext()){
                    Object nota = k.next().child("Nota").getValue();
                    System.out.println("BBBBBBBBBBBBBBBBBBBBBBBB" + Integer.parseInt(nota.toString()));
                    z = z + Integer.parseInt(nota.toString());
                    count ++;

                }
                z = z/count;

                Local_Ref_place.child("avaliacao").setValue(String.valueOf(z));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void openDialogReport(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DescricaoDoLocal.this);
        builder.setCancelable(true);
        builder.setTitle("Pretende mesmo reportar o lugar ?");
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Local_Ref_place.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String Key = userID;
                        if (!snapshot.child("Reports").hasChild(Key)){
                            Local_Ref_place.child("Reports").child(Key).setValue("True");
                            Local_Ref_place.child("reports").setValue(+1);
                        }
                        Local_Ref_place.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child("reports").getValue().toString().equals(String.valueOf(2))){
                                    Local_Ref_place.child("visibilidade").setValue("Privado");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        final AlertDialog alert_rate = builder.create();
        alert_rate.show();
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



}