package com.example.gpshares;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;


public class Login extends AppCompatActivity implements View.OnClickListener {

    private final static int RC_SIGN_IN = 123;
    private static final String TAG = "FacebookAuthentication";
    private TextView register, resetPW;
    private EditText editTextTextEmailAddress, editTextTextPassword;
    private Button signIn;
    private ImageButton signInGoogle, signInFacebook;
    private LoginButton loginFacebook;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private DatabaseReference databaseIdentificadores;
    private String userID;
    private StorageReference objectStorageReference;
    static final String code = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static SecureRandom rnd = new SecureRandom();
    //private String FBemail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        databaseIdentificadores = FirebaseDatabase.getInstance().getReference("identificadores");
        register = (TextView) findViewById(R.id.ViewRegistrar);
        register.setOnClickListener(this);

        signIn = (Button) findViewById(R.id.LOGIN);
        signIn.setOnClickListener(this);

        signInGoogle = (ImageButton) findViewById(R.id.googleAuth);
        signInGoogle.setOnClickListener(this);

        signInFacebook = (ImageButton) findViewById(R.id.facebookAuth);
        signInFacebook.setOnClickListener(this);

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
                    String identificador = randomCode(8);
                    String nomeInteiro = mAuth.getCurrentUser().getDisplayName();
                    Log.d(TAG, "sign in with credential: successful");
                    verificarIdentificador(identificador, new FirebaseCallback() {
                        @Override
                        public void onCallback(boolean i) {
                            if (i){
                                identificador.replace(identificador, randomCode(8));
                            }
                        }
                    });
                    Utilizador utilizador = new Utilizador(nomeInteiro, email, identificador, 0);
                    databaseReference.child(user).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + user.getEmail());
                                userID = user.getUid();
                                objectStorageReference = FirebaseStorage.getInstance().getReference(userID);
                                GlobalVariables.nomeUtilizador = snapshot.child("nomeInteiro").getValue().toString();
                                GlobalVariables.identificador = snapshot.child("identificador").getValue().toString();
                                GlobalVariables.formaAuth = snapshot.child("email").getValue().toString();
                                try{
                                    Glide.with(getApplicationContext())
                                            .asBitmap()
                                            .load(objectStorageReference.child(userID + ".jpg"))
                                            .centerCrop()
                                            .fitCenter()
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    GlobalVariables.imagemPerfil = resource;
                                                }
                                            });
                                }catch (Exception e){
                                    System.out.println("EXCEPTIONAAAAAAAAAAAA" + e);
                                    GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                            R.drawable.unknowuser);
                                }
                                try {
                                    Thread.sleep(2000);
                                    //Any other code to execute after 5 min execution pause.
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                startActivity(new Intent(Login.this, Map.class));
                            }else{
                                System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(utilizador).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            GlobalVariables.nomeUtilizador = nomeInteiro;
                                            GlobalVariables.formaAuth = email;
                                            GlobalVariables.identificador = identificador;
                                            GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(getResources(),
                                                    R.drawable.unknowuser);
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
                            String identificador = randomCode(8);

                            verificarIdentificador(identificador, new FirebaseCallback() {
                                @Override
                                public void onCallback(boolean i) {
                                    if (i){
                                        identificador.replace(identificador, randomCode(8));
                                    }
                                }
                            });
                            Utilizador utilizador = new Utilizador(nomeInteiro, email, identificador, 0);
                            databaseReference.child(user).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        userID = user.getUid();
                                        objectStorageReference = FirebaseStorage.getInstance().getReference(userID);
                                        GlobalVariables.nomeUtilizador = snapshot.child("nomeInteiro").getValue().toString();
                                        GlobalVariables.identificador = snapshot.child("identificador").getValue().toString();
                                        GlobalVariables.formaAuth = snapshot.child("email").getValue().toString();
                                        try{
                                            Glide.with(getApplicationContext())
                                                    .asBitmap()
                                                    .load(objectStorageReference.child(userID + ".jpg"))
                                                    .centerCrop()
                                                    .fitCenter()
                                                    .into(new SimpleTarget<Bitmap>() {
                                                        @Override
                                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                            GlobalVariables.imagemPerfil = resource;
                                                        }
                                                    });
                                        }catch (Exception e){
                                            System.out.println("EXCEPTIONAAAAAAAAAAAAAAAAA" + e);
                                            GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                                    R.drawable.unknowuser);
                                        }
                                        try {
                                            Thread.sleep(2000);
                                            //Any other code to execute after 5 min execution pause.
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        startActivity(new Intent(Login.this, Map.class));
                                    }else{
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                .setValue(utilizador).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    GlobalVariables.nomeUtilizador = nomeInteiro;
                                                    GlobalVariables.formaAuth = email;
                                                    GlobalVariables.identificador = identificador;
                                                    GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(getResources(),
                                                            R.drawable.unknowuser);
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
            case R.id.facebookAuth:
                loginFacebook.performClick();
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
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    GlobalVariables.nomeUtilizador = snapshot.child("nomeInteiro").getValue().toString();
                                    GlobalVariables.identificador = snapshot.child("identificador").getValue().toString();
                                    GlobalVariables.formaAuth = snapshot.child("email").getValue().toString();
                                    try {
                                        Glide.with(getApplicationContext())
                                                .asBitmap()
                                                .load(objectStorageReference.child(userID + ".jpg"))
                                                .centerCrop()
                                                .fitCenter()
                                                .into(new SimpleTarget<Bitmap>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                        GlobalVariables.imagemPerfil = resource;
                                                    }
                                                });
                                    } catch (Exception e) {
                                        System.out.println("EXCEPTION" + e);
                                        GlobalVariables.imagemPerfil = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                                R.drawable.unknowuser);
                                    }try {
                                        Thread.sleep(2000);
                                        //Any other code to execute after 5 min execution pause.
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(Login.this, Map.class));
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

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