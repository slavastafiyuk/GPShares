package com.example.gpshares;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gpshares.FriendsHelper.FindFriends;
import com.example.gpshares.PontosDeInteresseHelper.PontosDeInteresse;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

//
public class DescricaoDoLocal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    private TextView nome;
    private FirebaseAuth mAuth;
    private DatabaseReference Local_Ref, Post_ref;
    private String idDoUtilizador, userID, local, nome_local;
    private StorageReference objectStorageReference;
    private ImageView imageView;
    private FloatingActionButton floatingActionButton;
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
        Post_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(idDoUtilizador).child("Estabelecimentos").child(local).child(nome_local).child("Comments");
        nome = findViewById(R.id.Nome_Do_Local);
        imageView = findViewById(R.id.imageView_Local);
        firebaseFirestore = FirebaseFirestore.getInstance();
        floatingActionButton = findViewById(R.id.make_a_route);

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
                System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" + " " + caminho_da_imagem);
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(objectStorageReference.child(caminho_da_imagem))
                        .centerCrop()
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
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
                        localIntent.putExtra("longitude", longitude);
                        localIntent.putExtra("latitude", latitude);
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
                commentsViewHolder.setTime(comments.getTime());
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

        public void setTime(String time) {
            TextView tempo = (TextView) mView.findViewById(R.id.commentTime);
            tempo.setText("ás" + time);
        }
    }

    private void validateComment(String userName) {
        String commentText = commentInput.getText().toString();
        if (TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "O comentário não tem texto...", Toast.LENGTH_SHORT).show();
        }else{
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate =  currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime =  currentTime.format(callForDate.getTime());

            final String randomKey = userID + saveCurrentDate + saveCurrentTime;
            HashMap commentsMap = new HashMap();
            commentsMap.put("idUtilizador", userID);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
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
        }
        item.setChecked(true);
        return true;
    }
}