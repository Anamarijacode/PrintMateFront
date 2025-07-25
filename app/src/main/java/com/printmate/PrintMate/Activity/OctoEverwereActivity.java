package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.printmate.PrintMate.R;

public class OctoEverwereActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_octo_everwere);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        // 1) “Back” button → HomeActivity
        AppCompatButton backBtn = findViewById(R.id.buttonBackOcto);
        backBtn.setOnClickListener(v -> {
            Intent home = new Intent(OctoEverwereActivity.this, SpojNaPrinterActivity.class);
            startActivity(home);
            finish(); // optional, so this Detail screen is popped
        });

        // 2) “Forward” button → OdabirProizvođačaActivity
        AppCompatButton forwardBtn = findViewById(R.id.buttonNextOcto);
        forwardBtn.setOnClickListener(v -> {
            Intent pickMaker = new Intent(OctoEverwereActivity.this, ProizvodaciActivity.class);
            startActivity(pickMaker);
        });
    }
}