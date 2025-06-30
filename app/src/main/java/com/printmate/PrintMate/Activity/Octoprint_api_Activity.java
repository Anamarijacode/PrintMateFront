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

public class Octoprint_api_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_octoprint_api);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 1) “Back” button → HomeActivity
        AppCompatButton backBtn = findViewById(R.id.buttonbackAndroidOcto);
        backBtn.setOnClickListener(v -> {
            Intent home = new Intent(Octoprint_api_Activity.this, SpojNaPrinterActivity.class);
            startActivity(home);
            finish(); // optional, so this Detail screen is popped
        });
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 2) “Forward” button → OdabirProizvođačaActivity
        AppCompatButton forwardBtn = findViewById(R.id.buttonfowordAndroidOcto);
        forwardBtn.setOnClickListener(v -> {
            Intent pickMaker = new Intent(Octoprint_api_Activity.this, ProizvodaciActivity.class);
            startActivity(pickMaker);
        });
    }
}