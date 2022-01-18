package com.example.gpshares.loadHelper;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gpshares.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Loading extends AppCompatActivity {

    ProgressBar progressBar;
    TextView textView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        progressBar = findViewById(R.id.loadProgress);
        textView = findViewById(R.id.progress_percentage);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        progressBar.setMax(100);
        progressBar.setScaleY(3f);
        progressAnimation();
    }

    public void progressAnimation(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        ProgressBarAnimation animation = new ProgressBarAnimation(this, progressBar, textView, 0f, 100f);
        if (user != null){
            animation.setDuration(6000);
        }else {
            animation.setDuration(0);
        }
        progressBar.setAnimation(animation);
    }
}