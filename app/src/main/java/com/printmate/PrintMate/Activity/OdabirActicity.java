package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.printmate.PrintMate.R;

public class OdabirActicity extends AppCompatActivity {

    private AppCompatButton btnPostavljen;
    private AppCompatButton btnWindows;
    private AppCompatButton btnAndroid;
    private AppCompatButton btnRaPi;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odabir_acticity);

        // Hide the ActionBar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Find views
        btnPostavljen     = findViewById(R.id.btnPostavljen);
        btnWindows        = findViewById(R.id.btnWindows);
        btnAndroid        = findViewById(R.id.btnAndroid);
        btnRaPi           = findViewById(R.id.btnRaPi);
        fragmentContainer = findViewById(R.id.fragment_container_odabir);

        // Launch a new Activity for "Postavljen"
        btnPostavljen.setOnClickListener(v ->
                startActivity(new Intent(this, ProizvodaciActivity.class))
        );

        // Swap in the appropriate fragment
        btnWindows.setOnClickListener(v ->
                startActivity(new Intent(this, DetaljiWindowsActivity.class))
        );

        btnAndroid.setOnClickListener(v ->
                startActivity(new Intent(this, DetaljiAndroidActivity2.class))
        );

        btnRaPi.setOnClickListener(v ->
                startActivity(new Intent(this, DetaljiActivity.class))
        );
    }


}
