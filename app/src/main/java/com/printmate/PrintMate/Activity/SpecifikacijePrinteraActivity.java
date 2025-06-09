package com.printmate.PrintMate.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.printmate.PrintMate.Fragmenti.DetaljiFragment;
import com.printmate.PrintMate.R;

public class SpecifikacijePrinteraActivity extends AppCompatActivity {
private DetaljiFragment  stepFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_specifikacije_printera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            stepFragment = new DetaljiFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stepFragment)
                    .commitNow();
            return insets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}