package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.printmate.PrintMate.Adapter.ProizvodjacAdapter;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.Proizvodjac;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProizvodaciActivity extends AppCompatActivity {
    private ProizvodjacAdapter adapter;
    private final List<Proizvodjac> sviProizvodjaci = new ArrayList<>();

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
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProizvodjac);
        SearchView searchView = findViewById(R.id.searchViewProizvodjac);

        adapter = new ProizvodjacAdapter(sviProizvodjaci, this, proizvodjac -> {
            Intent intent = new Intent(this, PrinterActivity.class);
            intent.putExtra("idProizvodjaca", proizvodjac.getId());
            intent.putExtra("nazivProizvodjaca", proizvodjac.getNaziv());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // —— Spinner filter A–Z ——
        Spinner spinner = findViewById(R.id.filterSpinnerProizvodjac);
        List<String> letters = new ArrayList<>();
        letters.add("Svi");
        for (char c = 'A'; c <= 'Z'; c++) {
            letters.add(String.valueOf(c));
        }
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                letters
        );
        spinner.setAdapter(spinAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String sel = letters.get(pos);
                if (sel.equals("Svi")) {
                    adapter.filterList(sviProizvodjaci);
                } else {
                    List<Proizvodjac> filtrirani = new ArrayList<>();
                    for (Proizvodjac p : sviProizvodjaci) {
                        if (p.getNaziv().toUpperCase().startsWith(sel)) {
                            filtrirani.add(p);
                        }
                    }
                    adapter.filterList(filtrirani);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        // ————————————————

        setupSearch(searchView);
        fetchProizvodjaciFromApi();
    }

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
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

    private void fetchProizvodjaciFromApi() {
        AuthApi api = ApiClient.getAuthApi();
        Call<List<Proizvodjac>> call = api.getProizvodjaci();
        call.enqueue(new Callback<List<Proizvodjac>>() {
            @Override
            public void onResponse(Call<List<Proizvodjac>> call, Response<List<Proizvodjac>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sviProizvodjaci.clear();
                    for (Proizvodjac p : response.body()) {
                        String base64 = p.getLogoBase64();
                        Bitmap bmp = null;
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                            bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        }
                        p.setLogoBitmap(bmp);
                        sviProizvodjaci.add(p);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ProizvodaciActivity.this,
                            "Greška pri dohvaćanju proizvođača", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Proizvodjac>> call, Throwable t) {
                Toast.makeText(ProizvodaciActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
