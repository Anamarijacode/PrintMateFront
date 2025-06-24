package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private List<Printer> sviPrinteri = new ArrayList<>();
    private PrinterAdapter  adapter;

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
        adapter = new PrinterAdapter(this, sviPrinteri, printer -> {
            Intent i = new Intent(PrinterActivity.this, SpojNaPrinterActivity.class);
            i.putExtra("idPrinter", printer.getIdModela());
            i.putExtra("nazivPrinter", printer.getNaziv());
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        fetchPrinters(idProizvodjaca);
    }

    private void fetchPrinters(int proizvodjacId) {
        AuthApi api = ApiClient.getAuthApi();
        api.getModelPrintera(proizvodjacId).enqueue(new Callback<List<Printer>>() {
            @Override public void onResponse(Call<List<Printer>> c, Response<List<Printer>> res) {
                if (!res.isSuccessful() || res.body()==null) {
                    Toast.makeText(PrinterActivity.this,
                            "Greška pri dohvaćanju printera", Toast.LENGTH_SHORT).show();
                    return;
                }
                sviPrinteri.clear();
                for (Printer p : res.body()) {
                    if (p.getProizvodjacId()==proizvodjacId) {
                        String b64 = p.getSlikaPrinteraBase64();
                        if (b64!=null && !b64.isEmpty()) {
                            byte[] data = Base64.decode(b64, Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            p.setLogoBitmap(bmp);
                        }
                        sviPrinteri.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<Printer>> c, Throwable t) {
                Toast.makeText(PrinterActivity.this,
                        "Network error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
