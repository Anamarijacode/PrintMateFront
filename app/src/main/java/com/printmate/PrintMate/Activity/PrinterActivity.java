package com.printmate.PrintMate.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Adapter.PrinterAdapter;
import com.printmate.PrintMate.Model.Printer;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrinterActivity extends AppCompatActivity {

    private List<Printer> sviPrinteri;
    private PrinterAdapter printerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_printer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Dohvati naziv proizvođača iz Intenta
        String nazivProizvodjaca = getIntent().getStringExtra("nazivProizvodjaca");

        // 2. Inicijaliziraj listu svih printera
        sviPrinteri = Arrays.asList(
                new Printer("Ender 3 V2", R.drawable.ender3v2, "Creality"),
                new Printer("CR-10", R.drawable.cr10, "Creality"),
                new Printer("Sidewinder X1", R.drawable.sidewinder_x1, "ARTILLERY"),
                new Printer("BALCO Touch", R.drawable.balco_touch, "BALCO")
        );

        // 3. Priprema RecyclerView-a
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPrinter);
        printerAdapter = new PrinterAdapter(this, new ArrayList<Printer>());

        recyclerView.setAdapter(printerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 4. Filtriraj na temelju dobivenog proizvođača
        if (nazivProizvodjaca != null) {
            filtrirajPoProizvodjacu(nazivProizvodjaca);
        }
    }


    public void filtrirajPoProizvodjacu(String naziv) {
        List<Printer> filtrirani = new ArrayList<>();
        for (Printer p : sviPrinteri) {
            if (p.getProizvodjacNaziv().equalsIgnoreCase(naziv)) {
                filtrirani.add(p);
            }
        }
        printerAdapter.updateList(filtrirani);
    }
}
