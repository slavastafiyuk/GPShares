package com.example.gpshares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DescricaoDoLocal extends AppCompatActivity {

    private TextView nome;
    private FirebaseAuth mAuth;
    private DatabaseReference Local_Ref;
    private String idDoUtilizador, local, nome_local;
    private StorageReference objectStorageReference;
    private ImageView imageView;
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
        imageView = findViewById(R.id.imageView_Local);
        Local_Ref.child(idDoUtilizador).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    objectStorageReference = FirebaseStorage.getInstance().getReference(idDoUtilizador);
                    System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" + nome_local);
                    String coment_do_local = snapshot.child("Estabelecimentos").child(local).child(nome_local).child("comentario").getValue().toString();
                    try {
                        String caminho_da_imagem = snapshot.child("Estabelecimentos").child(local).child(nome_local).child("imagem").getValue().toString();
                        System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" + " " + caminho_da_imagem);
                        objectStorageReference.child(caminho_da_imagem).getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                //GlobalVariables.imagemPerfil = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(getResources(),
                                //        R.drawable.unknowuser);
                                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
                            }
                        });
                    }catch (Exception e){
                        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
                    }
                    nome.setText(coment_do_local);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}