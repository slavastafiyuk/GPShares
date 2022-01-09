package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DescricaoDoLocal extends AppCompatActivity {

    private TextView nome;
    private FirebaseAuth mAuth;
    private DatabaseReference Local_Ref;
    private String idDoUtilizador, local, nome_local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descricao_do_local);

        mAuth = FirebaseAuth.getInstance();
        idDoUtilizador = getIntent().getExtras().get("UserId").toString();
        local = getIntent().getExtras().get("place").toString();
        nome_local = getIntent().getExtras().get("nome").toString();
        Local_Ref = FirebaseDatabase.getInstance().getReference().child("Users");
        nome = findViewById(R.id.Nome_Do_Local);

        Local_Ref.child(idDoUtilizador).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String coment_do_local = snapshot.child("Estabelecimentos").child(local).child(nome_local).child("comentario").getValue().toString();
                    nome.setText(coment_do_local);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}