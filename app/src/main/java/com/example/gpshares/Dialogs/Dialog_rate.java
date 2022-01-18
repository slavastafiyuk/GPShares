package com.example.gpshares.Dialogs;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.gpshares.R;

public class Dialog_rate extends AppCompatActivity {
    Button submitRate;
    RatingBar ratingStars;
    float myrating;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.layout_dialog_rate);

        submitRate = findViewById(R.id.sentRate);
        ratingStars = findViewById(R.id.rantingBar);

        ratingStars.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                int rating = (int) v;
                myrating = ratingBar.getRating();
                if (rating == 0) {
                    Toast.makeText(Dialog_rate.this, "Tem que avaliar primeiro antes de confirmar", Toast.LENGTH_SHORT).show();;
                }
            }
        });

        submitRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    public void show(FragmentManager supportFragmentManager, String dialog) {
    }
}
