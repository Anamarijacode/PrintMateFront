package com.printmate.PrintMate.Activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.StatistikaDomain;
import com.printmate.PrintMate.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsActivity extends AppCompatActivity {

    private LineChart chartMaterial, chartTime;
    private int gcodeId;
    private AuthApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        api = ApiClient.getAuthApi();
        gcodeId = getIntent().getIntExtra("GCODE_ID", 0);

        chartMaterial = findViewById(R.id.chartMaterial);
        chartTime     = findViewById(R.id.chartTime);

        Button btnPdf   = findViewById(R.id.btnExportPdf);
        Button btnExcel = findViewById(R.id.btnExportExcel);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Statistika za GCode #" + gcodeId);

        btnPdf.setOnClickListener(v -> exportPdf());
        btnExcel.setOnClickListener(v -> exportExcel());

        loadStatistics();
    }

    private void loadStatistics() {
        Call<List<StatistikaDomain>> call = api.getStatistikaByGCode(gcodeId);
        call.enqueue(new Callback<List<StatistikaDomain>>() {
            @Override
            public void onResponse(@NonNull Call<List<StatistikaDomain>> call,
                                   @NonNull Response<List<StatistikaDomain>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(StatsActivity.this,
                            "Greška pri dohvatu podataka",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                List<StatistikaDomain> statsList = res.body();
                setupMaterialChart(statsList);
                setupTimeChart(statsList);
            }

            @Override
            public void onFailure(@NonNull Call<List<StatistikaDomain>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(StatsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupMaterialChart(List<StatistikaDomain> statsList) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < statsList.size(); i++) {
            double used = statsList.get(i).getUkupnaPotrosnjaMaterijala();
            entries.add(new Entry(i, (float) used));
        }
        drawChart(chartMaterial, entries, "Materijal (g)");
    }

    private void setupTimeChart(List<StatistikaDomain> statsList) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < statsList.size(); i++) {
            long seconds = getDurationSeconds(statsList.get(i));
            entries.add(new Entry(i, seconds / 60f));  // show in minutes
        }
        drawChart(chartTime, entries, "Vrijeme (min)");
    }

    /** Safely extract seconds from Duration (no crash on API < 26) */
    private long getDurationSeconds(StatistikaDomain s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return s.getUkupnoVrijemeRada().getSeconds();
        } else {
            // fallback: parse toString() which is "PT#H#M#S"
            String str = s.getUkupnoVrijemeRada().toString()
                    .replace("PT","")
                    .replace("H",":").replace("M",":").replace("S","");
            String[] parts = str.split(":");
            long secs = 0;
            if (parts.length==3)
                secs = Integer.parseInt(parts[0])* 3600L
                        + Integer.parseInt(parts[1])* 60L
                        + Integer.parseInt(parts[2]);
            return secs;
        }
    }

    private void drawChart(LineChart chart, List<Entry> entries, String label) {
        LineDataSet ds = new LineDataSet(entries, label);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setDrawCircles(false);
        chart.setData(new LineData(ds));
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }

    private void exportPdf() {
        api.generatePdfByGCode(gcodeId).enqueue(new Callback<byte[]>() {
            @Override
            public void onResponse(@NonNull Call<byte[]> call,
                                   @NonNull Response<byte[]> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(StatsActivity.this,
                            "Greška pri preuzimanju PDF-a",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                saveToFile(res.body(), "statistika_" + gcodeId + ".pdf");
            }
            @Override
            public void onFailure(@NonNull Call<byte[]> call,
                                  @NonNull Throwable t) {
                Toast.makeText(StatsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void exportExcel() {
        api.generateCijenaPrintaExcel(gcodeId).enqueue(new Callback<byte[]>() {
            @Override
            public void onResponse(@NonNull Call<byte[]> call,
                                   @NonNull Response<byte[]> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(StatsActivity.this,
                            "Greška pri preuzimanju Excela",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                saveToFile(res.body(), "statistika_" + gcodeId + ".xlsx");
            }
            @Override
            public void onFailure(@NonNull Call<byte[]> call,
                                  @NonNull Throwable t) {
                Toast.makeText(StatsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveToFile(byte[] bytes, String filename) {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) dir = getFilesDir();
        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            Toast.makeText(this,
                    "Spremljeno: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this,
                    "Greška kod spremanja: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
