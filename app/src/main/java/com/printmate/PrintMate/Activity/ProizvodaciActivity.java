package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Adapter.ProizvodjacAdapter;
import com.printmate.PrintMate.Modeli.Proizvodjac;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProizvodaciActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_proizvodaci);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        List<Proizvodjac> sviProizvodjaci = Arrays.asList(
                new Proizvodjac("Creality", R.drawable.creality),
                new Proizvodjac("ARTILLERY", R.drawable.artillery_logo),
                new Proizvodjac("BALCO", R.drawable.balco_logo)
        );
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProizvodjac);
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchViewProizvodjac);
        ProizvodjacAdapter adapter = new ProizvodjacAdapter(sviProizvodjaci, this, new ProizvodjacAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Proizvodjac proizvodjac) {
                Intent intent = new Intent(ProizvodaciActivity.this, PrinterActivity.class);
                intent.putExtra("nazivProizvodjaca", proizvodjac.getNaziv());
               ProizvodaciActivity.this.startActivity(intent);

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

// Filtar po početnom slovu
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Proizvodjac> filtrirani = new ArrayList<>();
                for (Proizvodjac p : sviProizvodjaci) {
                    if (p.getNaziv().toLowerCase().startsWith(newText.toLowerCase())) {
                        filtrirani.add(p);
                    }
                }
                adapter.filterList(filtrirani);
                return true;
            }
        });

    }
}