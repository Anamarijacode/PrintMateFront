package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.printmate.PrintMate.R;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launch);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        // If we have a token (and you might want to verify it's not expired),
        // go straight to Home.  Otherwise on to LanguageActivity (or Login).
        Class<?> nextActivity;
        if (token != null && !token.isEmpty()) {
            nextActivity = HomeActivity.class;
        } else {
            nextActivity = LanguageActivity.class;
        }

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, nextActivity));
            finish();
        }, 1500);  // splash for 1.5s if you like
    }
}