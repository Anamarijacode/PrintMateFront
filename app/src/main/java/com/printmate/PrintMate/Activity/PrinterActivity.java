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

import com.printmate.PrintMate.Adapter.PrinterAdapter;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.Printer;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrinterActivity extends AppCompatActivity {
    private final List<Printer> sviPrinteri = new ArrayList<>();
    private PrinterAdapter adapter;

    // State for combined filters
    private String currentFilterLetter = "Svi";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_printer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        int idProizvodjaca = getIntent().getIntExtra("idProizvodjaca", -1);
        if (idProizvodjaca < 0) {
            Toast.makeText(this, "Neispravan proizvođač", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.recyclerViewPrinter);
        adapter = new PrinterAdapter(this, new ArrayList<>(), printer -> {
            Intent i = new Intent(PrinterActivity.this, SpojNaPrinterActivity.class);
            i.putExtra("idPrinter", printer.getIdModela());
            i.putExtra("nazivPrinter", printer.getNaziv());
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Setup SearchView
        SearchView searchView = findViewById(R.id.searchViewPrinter);
        setupSearch(searchView);

        // —— Spinner filter A–Z ——
        Spinner spinner = findViewById(R.id.filterSpinnerPrinter);
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
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                currentFilterLetter = letters.get(pos);
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        // ————————————————

        fetchPrinters(idProizvodjaca);
    }

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.trim();
                applyFilters();
                return true;
            }
        });
    }

    /** Applies both the letter prefix and search query filters */
    private void applyFilters() {
        List<Printer> filtered = new ArrayList<>();
        String q = currentSearchQuery.toLowerCase();
        boolean allLetters = currentFilterLetter.equals("Svi");

        for (Printer p : sviPrinteri) {
            String name = p.getNaziv();
            boolean matchesLetter = allLetters || name.toUpperCase().startsWith(currentFilterLetter);
            boolean matchesQuery = q.isEmpty() || name.toLowerCase().contains(q);

            if (matchesLetter && matchesQuery) {
                filtered.add(p);
            }
        }
        adapter.filterList(filtered);
    }

    private void fetchPrinters(int proizvodjacId) {
        AuthApi api = ApiClient.getAuthApi();
        Call<List<Printer>> call = api.getModelPrintera(proizvodjacId);
        call.enqueue(new Callback<List<Printer>>() {
            @Override
            public void onResponse(Call<List<Printer>> call, Response<List<Printer>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PrinterActivity.this,
                            "Greška pri dohvaćanju printera", Toast.LENGTH_SHORT).show();
                    return;
                }
                sviPrinteri.clear();
                for (Printer p : response.body()) {
                    if (p.getProizvodjacId() == proizvodjacId) {
                        String b64 = p.getSlikaPrinteraBase64();
                        if (b64 != null && !b64.isEmpty()) {
                            byte[] data = Base64.decode(b64, Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            p.setLogoBitmap(bmp);
                        }
                        sviPrinteri.add(p);
                    }
                }
                // Re-apply filters so search & spinner both take effect
                applyFilters();
            }

            @Override
            public void onFailure(Call<List<Printer>> call, Throwable t) {
                if (isFinishing() ||
                        (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                    return;
                }
                runOnUiThread(() -> Toast.makeText(PrinterActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
