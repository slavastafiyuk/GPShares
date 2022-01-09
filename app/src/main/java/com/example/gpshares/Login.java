package com.example.gpshares;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class Login extends AppCompatActivity implements View.OnClickListener {

    private final static int RC_SIGN_IN = 123;
    private static final String TAG = "FacebookAuthentication";
    private TextView register, resetPW;
    private EditText editTextTextEmailAddress, editTextTextPassword;
    private Button signIn, signInGoogle;
    private LoginButton loginFacebook;
    private FirebaseAuth mAuth, myUID;
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

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

        FacebookSdk.sdkInitialize(getApplicationContext());
        loginFacebook = findViewById(R.id.login_button);
        mCallbackManager = CallbackManager.Factory.create();
        loginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.d(TAG, "onError" + e);
            }
        });
    }
    private void handleFacebookToken(AccessToken token) {
        Log.d(TAG, "handleFacebookToken" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String email = "FacebookAuthenticated";
                    String user = mAuth.getCurrentUser().getUid();
                    String nomeInteiro = mAuth.getCurrentUser().getDisplayName();
                    Utilizador utilizador = new Utilizador(nomeInteiro, email);
                    Log.d(TAG, "sign in with credential: successful");
                    databaseReference.child("user").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                startActivity(new Intent(Login.this, Map.class));
                            }else{
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(utilizador).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            startActivity(new Intent(Login.this, Map.class));
                                        }
                                    }
                                });
                            }
                            //int existencia = 0;
                            //for (DataSnapshot i : snapshot.getChildren()){
                            //    String z = i.getKey();
                            //    if (Objects.equals(z, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                            //        existencia=1;
                            //    }
                            //    if (!snapshot.getChildren().iterator().hasNext() && existencia == 0){
                            //        FirebaseDatabase.getInstance().getReference("Users")
                            //                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                            //                .setValue(utilizador).addOnCompleteListener(new OnCompleteListener<Void>() {
                            //            @Override
                            //            public void onComplete(@NonNull Task<Void> task) {
                            //                if (task.isSuccessful()) {
                            //                    FirebaseUser user = mAuth.getCurrentUser();
                            //                    startActivity(new Intent(Login.this, Map.class));
                            //                }
                            //            }
                            //        });
                            //    }else if(existencia == 1){
                            //        FirebaseUser user = mAuth.getCurrentUser();
                            //        startActivity(new Intent(Login.this, Map.class));
                            //    }
                            //}
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else {
                    Log.d(TAG, "sign in with credential: failure", task.getException());
                    Toast.makeText(Login.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    //UpdateUI(null);
                }
            }
        });
    }

    //private void UpdateUI (FirebaseUser user){
    //    if (user != null){
    //    }
    //}

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getEmail().equals("")){
                startActivity(new Intent(Login.this, Map.class));
            }else if (user.isEmailVerified()){
                startActivity(new Intent(Login.this, Map.class));
            }else if (!user.isEmailVerified()){
                Toast.makeText(this, "Tem de verificar o email", Toast.LENGTH_SHORT).show();
            }
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

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
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
        String email = "GoogleAuthenticated";
        String nomeInteiro = acct.getDisplayName();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String user = mAuth.getCurrentUser().getUid();
                            Utilizador utilizador = new Utilizador(nomeInteiro, email);
                            databaseReference.child(user).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startActivity(new Intent(Login.this, Map.class));
                                    }else{
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                .setValue(utilizador).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    startActivity(new Intent(Login.this, Map.class));
                                                }
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        } else {
                            //Snackbar.make(findViewById(R.id.LOGIN), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(Login.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @SuppressLint("NonConstantResourceId")
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
                    assert user != null;
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