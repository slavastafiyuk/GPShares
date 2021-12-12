package com.example.gpshares;

import android.content.Intent;
import android.os.Bundle;
import android.os.TestLooperManager;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private TextView register, resetPW;
    private EditText editTextTextEmailAddress, editTextTextPassword;
    private Button signIn, signInGoogle;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register = (TextView) findViewById(R.id.ViewRegistrar);
        register.setOnClickListener(this);

        signIn = (Button) findViewById(R.id.LOGIN);
        signIn.setOnClickListener(this);

        signInGoogle = (Button) findViewById(R.id.googleAuth);
        signInGoogle.setOnClickListener(this);

        editTextTextEmailAddress = (EditText) findViewById(R.id.editTextTextEmailAddress);
        editTextTextPassword = (EditText) findViewById(R.id.editTextTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        resetPW = (TextView) findViewById(R.id.ResetPW);
        resetPW.setOnClickListener(this);

        createRequest();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(Login.this, Map.class));
            //Toast.makeText(Login.this, "ALL DONE", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(getApplicationContext(), Map.class);
            //startActivity(intent);
        }
    }

    private void createRequest() {
        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1071669500941-81773rj25om5q4licl7gp5ujpsmdmfth.apps.googleusercontent.com")
                .requestEmail()
                .build();
        //Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Toast.makeText(Login.this, "COOLE", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(Login.this, Map.class));
                            //Toast.makeText(Login.this, "ALL DONE", Toast.LENGTH_SHORT).show();
                            //startActivity(intent);
                        } else {
                            //Snackbar.make(findViewById(R.id.LOGIN), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(Login.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ViewRegistrar:
                startActivity(new Intent(this, Registrar.class));
                break;
            case R.id.LOGIN:
                userLogin();
                break;
            case R.id.ResetPW:
                startActivity(new Intent(this, ResetPassword.class));
                break;
            case R.id.googleAuth:
                signIn();
                break;
        }
    }

    private void userLogin() {
        String email = editTextTextEmailAddress.getText().toString().trim();
        String password = editTextTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextTextEmailAddress.setError("O campo do email esta vazio");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextTextEmailAddress.setError("Por favor indique um email valido");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextTextPassword.setError("O campo da palavra-passe esta vazio");
            editTextTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextTextPassword.setError("O tamanho minimo da palavra-passe é de 6 carateres");
            editTextTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()) {
                        startActivity(new Intent(Login.this, Map.class));
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(Login.this, "Verifique o seu email para ativar a conta", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(Login.this, "Houve uma falha durante o login, verifique se indicou as credenciais validas", Toast.LENGTH_LONG).show();
                }


            }
        });

    }
}