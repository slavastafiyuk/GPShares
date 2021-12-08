package com.example.gpshares;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener{

    private TextView register;
    private EditText editTextTextEmailAddress, editTextTextPassword;
    private Button signIn;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register = (TextView) findViewById(R.id.ViewRegistrar);
        register.setOnClickListener(this);

        signIn = (Button) findViewById(R.id.LOGIN);
        signIn.setOnClickListener(this);

        editTextTextEmailAddress = (EditText) findViewById(R.id.editTextTextEmailAddress);
        editTextTextPassword = (EditText) findViewById(R.id.editTextTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.ViewRegistrar:
                startActivity(new Intent(this,Registrar.class));
                break;
            case R.id.LOGIN:
                userLogin();
                break;
        }
    }

    private void userLogin() {
        String email = editTextTextEmailAddress.getText().toString().trim();
        String password = editTextTextPassword.getText().toString().trim();

        if (email.isEmpty()){
            editTextTextEmailAddress.setError("O campo do email esta vazio");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextTextEmailAddress.setError("Por favor indique um email valido");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (password.isEmpty()){
            editTextTextPassword.setError("O campo da palavra-passe esta vazio");
            editTextTextPassword.requestFocus();
            return;
        }
        if (password.length()<6){
            editTextTextPassword.setError("O tamanho minimo da palavra-passe é de 6 carateres");
            editTextTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()){
                        startActivity(new Intent(Login.this, Map.class));
                    }else{
                        user.sendEmailVerification();
                        Toast.makeText(Login.this, "Verifique o seu email para ativar a conta", Toast.LENGTH_LONG).show();
                    }


                }else{
                    Toast.makeText(Login.this, "Houve uma falha durante o login, verifique se indicou as credenciais validas", Toast.LENGTH_LONG).show();
                }


            }
        });

    }
}