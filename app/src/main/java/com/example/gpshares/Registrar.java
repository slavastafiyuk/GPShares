package com.example.gpshares;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.Objects;

public class Registrar extends AppCompatActivity implements View.OnClickListener {
    private TextView RegisterBtn;
    private EditText nomeDoUtilizador, idadeDoUtilizador, editTextTextEmailAddress2, editTextTextPassword2;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    DatabaseReference databaseIdentificadores, databaseUser;
    static final String code = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static SecureRandom rnd = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        mAuth = FirebaseAuth.getInstance();
        databaseIdentificadores = FirebaseDatabase.getInstance().getReference("identificadores");
        databaseUser = FirebaseDatabase.getInstance().getReference("Users");

        RegisterBtn = (Button) findViewById(R.id.RegisterBtn);
        RegisterBtn.setOnClickListener(this);

        nomeDoUtilizador = (EditText) findViewById(R.id.nomeDoUtilizador);
        idadeDoUtilizador =(EditText) findViewById(R.id.idadeDoUtilizador);
        editTextTextEmailAddress2 =(EditText) findViewById(R.id.editTextTextEmailAddress2);
        editTextTextPassword2 =(EditText) findViewById(R.id.editTextTextPassword2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.RegisterBtn:
                registrarUtilizador();
        }
    }

    private void registrarUtilizador() {
        String email = editTextTextEmailAddress2.getText().toString().trim();
        String password = editTextTextPassword2.getText().toString().trim();
        String nomeInteiro = nomeDoUtilizador.getText().toString().trim();
        String idade = idadeDoUtilizador.getText().toString().trim();
        //VALIDAR O NOME
        if(nomeInteiro.isEmpty()){
            nomeDoUtilizador.setError("Tem de indicar o seu nome!");
            nomeDoUtilizador.requestFocus();
            return;
        }
        //VALIDAR A IDADE
        if (idade.isEmpty()){
            idadeDoUtilizador.setError("Tem de indicar a sua idade!");
            idadeDoUtilizador.requestFocus();
            return;
        }
        //VALIDAR O EMAIL
        if (email.isEmpty()){
            editTextTextEmailAddress2.setError("Temn de indicar um email!");
            editTextTextEmailAddress2.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextTextEmailAddress2.setError("Por favor indique um email valido");
            editTextTextEmailAddress2.requestFocus();
            return;
        }
        //VALIDAR A PASSWORD
        if (password.isEmpty()){
            editTextTextPassword2.setError("Tem de indicar uma password!");
            editTextTextPassword2.requestFocus();
            return;
        }
        if(password.length()<6){
            editTextTextPassword2.setError("A palavra-passe tem de ter no minimo 6 carateres");
            editTextTextPassword2.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String identificador = randomCode(8);
                            String user = mAuth.getCurrentUser().getUid();
                            verificarIdentificador(identificador, new FirebaseCallback() {
                                @Override
                                public void onCallback(boolean i) {
                                    if (i){
                                        identificador.replace(identificador, randomCode(8));
                                    }
                                }
                            });
                            Utilizador utilizador = new Utilizador(nomeInteiro, email, identificador);
                            databaseUser.child(user).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Toast.makeText(Registrar.this, "O utilizador ja existe", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }else{
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                .setValue(utilizador).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                    user.sendEmailVerification();
                                                    Toast.makeText(Registrar.this, "Registro efetuado com sucesso, foi-lhe enviado um email para verificar a sua conta", Toast.LENGTH_LONG).show();
                                                    progressBar.setVisibility(View.GONE);
                                                }else{
                                                    Toast.makeText(Registrar.this, "Houve um problema durante o registro, Tente de novo", Toast.LENGTH_LONG).show();
                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }else{
                            Toast.makeText(Registrar.this, "Houve um problema durante o registro, Tente de novo", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    String randomCode (int t){
        StringBuilder sb = new StringBuilder(t);
        for (int i = 0; i < t; i++){
            sb.append(code.charAt(rnd.nextInt(code.length())));
        }
        return sb.toString();
    }

    private interface FirebaseCallback {
        void onCallback(boolean i);
    }

    public void verificarIdentificador(String identificador, FirebaseCallback firebaseCallback){
        databaseIdentificadores.child(identificador).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    firebaseCallback.onCallback(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}