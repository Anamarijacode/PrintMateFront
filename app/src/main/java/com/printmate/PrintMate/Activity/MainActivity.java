package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andreseko.SweetAlert.SweetAlertDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private int printerId;
    private String printerName, baseUrl, apiKey, imageBase64, backendApiUrl;

    private TextView naslovView, statusOnlineView, tempNozzleView, tempBedView, fanSpeedView, zOffsetView;
    private ImageView printerImageView;
    private LineChart chart;

    private final List<Entry> toolEntries = new ArrayList<>();
    private final List<Entry> bedEntries = new ArrayList<>();
    private float targetTool = 200, targetBed = 60;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private OctoPrintController octoPrintController;

    private AuthApi authApi;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchTemperatures();
            handler.postDelayed(this, 1500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        Bundle args = getIntent().getExtras();
        if (args != null) {
            printerId    = args.getInt("printer_id", -1);
            printerName  = args.getString("printer_name", "");
            baseUrl      = args.getString("base_url", "");
            apiKey       = args.getString("api_key", "");
            imageBase64  = args.getString("printer_image_base64", "");
        }

        // Inicijalizacija Retrofit AuthApi instance
        authApi = ApiClient.getAuthApi();

        naslovView       = findViewById(R.id.textNaslovDetaljiAndroid);
        statusOnlineView = findViewById(R.id.statusOnline);
        tempNozzleView   = findViewById(R.id.temp_nozzle);
        tempBedView      = findViewById(R.id.temp_bed);
        fanSpeedView     = findViewById(R.id.fan_speed);
        zOffsetView      = findViewById(R.id.z_offset);
        printerImageView = findViewById(R.id.printerImage);
        chart            = findViewById(R.id.temperature_chart);

        if (printerName != null) naslovView.setText(printerName);

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                printerImageView.setImageBitmap(bmp);
            } catch (Exception e) {
                printerImageView.setImageResource(R.drawable.baseline_no_photography_24);
            }
        } else {
            printerImageView.setImageResource(R.drawable.baseline_no_photography_24);
        }

        setupChart();
        handler.post(refreshRunnable);

        // Back icon
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());

        // Inicijalizacija OctoPrintController
        octoPrintController = new OctoPrintController(baseUrl, apiKey);


        findViewById(R.id.btn_tune).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TuneActivity.class);
            intent.putExtra("printer_id", printerId);
            intent.putExtra("base_url", baseUrl);
            intent.putExtra("api_key", apiKey);
            startActivity(intent);
        });
        findViewById(R.id.btn_card_gcode).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GKodActivity.class);
            intent.putExtra("printer_id", printerId);
            intent.putExtra("base_url", baseUrl);
            intent.putExtra("api_key", apiKey);
            startActivity(intent);
        });


        // Brisanje printera (kanta za smeće)
        // Zamijeni btn_card_settings u btn_delete ako imaš drugu id!
        findViewById(R.id.btn_card_settings).setOnClickListener(v -> showDeleteConfirmation());

        ImageView logo = findViewById(R.id.logoOdabir);
        boolean isDark = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDark) {
            logo.setImageResource(R.drawable.logoandiconwhite);
        } else {
            logo.setImageResource(R.drawable.logoandiconviolet);
        }
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(true);
        chart.setBorderWidth(1f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.printMateWhite));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(300f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.input_secondary));

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(13f);
        legend.setDrawInside(false);
        legend.setYOffset(8f);
        legend.setTextColor(ContextCompat.getColor(this, R.color.input_secondary));

        updateLimitLines();
    }

    private void updateLimitLines() {
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();

        int toolLineColor = ContextCompat.getColor(this, R.color.tool_line_color);
        int bedLineColor = ContextCompat.getColor(this, R.color.bed_line_color);

        LimitLine targetToolLine = new LimitLine(targetTool, "Target T: " + targetTool + "°C");
        targetToolLine.setLineColor(toolLineColor);
        targetToolLine.setLineWidth(1.5f);
        targetToolLine.enableDashedLine(8, 8, 0);
        targetToolLine.setTextColor(toolLineColor);

        LimitLine targetBedLine = new LimitLine(targetBed, "Target Bed: " + targetBed + "°C");
        targetBedLine.setLineColor(bedLineColor);
        targetBedLine.setLineWidth(1.5f);
        targetBedLine.enableDashedLine(8, 8, 0);
        targetBedLine.setTextColor(bedLineColor);

        leftAxis.addLimitLine(targetToolLine);
        leftAxis.addLimitLine(targetBedLine);
    }

    private void fetchTemperatures() {
        if (baseUrl == null || apiKey == null) return;
        String url = baseUrl + "/api/printer";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Api-Key", apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OctoPrint", "API fetch failed", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String res = response.body().string();
                    JSONObject obj = new JSONObject(res);

                    final float actualTool = (float) obj.getJSONObject("temperature").getJSONObject("tool0").getDouble("actual");
                    final float targetToolApi = (float) obj.getJSONObject("temperature").getJSONObject("tool0").getDouble("target");
                    final float actualBed = (float) obj.getJSONObject("temperature").getJSONObject("bed").getDouble("actual");
                    final float targetBedApi = (float) obj.getJSONObject("temperature").getJSONObject("bed").getDouble("target");

                    final int fanSpeed = obj.has("fan_speed") ? obj.getInt("fan_speed") : 0;
                    final double zOffset = obj.has("z_offset") ? obj.getDouble("z_offset") : -0.60;

                    runOnUiThread(() -> {
                        float x = toolEntries.size();

                        toolEntries.add(new Entry(x, actualTool));
                        bedEntries.add(new Entry(x, actualBed));
                        if (toolEntries.size() > 1800) {
                            toolEntries.remove(0);
                            bedEntries.remove(0);
                        }

                        targetTool = targetToolApi;
                        targetBed = targetBedApi;
                        updateChart();

                        tempNozzleView.setText(String.format(Locale.US, "Temp dizne: %.1f °C", actualTool));
                        tempBedView.setText(String.format(Locale.US, "Temp podloge: %.1f °C", actualBed));
                        fanSpeedView.setText(String.format(Locale.US, "Brzina ventilatora: %d", fanSpeed));
                        zOffsetView.setText(String.format(Locale.US, "Z-offset: %.2f", zOffset));
                        statusOnlineView.setText("Online");
                    });
                } catch (Exception e) {
                    Log.e("OctoPrint", "Parse error", e);
                }
            }
        });
    }

    private void updateChart() {
        updateLimitLines();

        int toolColor = ContextCompat.getColor(this, R.color.tool_line_color);
        int bedColor = ContextCompat.getColor(this, R.color.bed_line_color);

        LineDataSet toolSet = new LineDataSet(toolEntries, "Actual T");
        toolSet.setColor(toolColor);
        toolSet.setLineWidth(2f);
        toolSet.setDrawCircles(false);
        toolSet.setDrawValues(false);
        toolSet.setValueTextColor(toolColor);

        LineDataSet bedSet = new LineDataSet(bedEntries, "Actual Bed");
        bedSet.setColor(bedColor);
        bedSet.setLineWidth(2f);
        bedSet.setDrawCircles(false);
        bedSet.setDrawValues(false);
        bedSet.setValueTextColor(bedColor);

        LineData data = new LineData(toolSet, bedSet);
        chart.setData(data);
        chart.invalidate();
    }

    // ====== PRINT KONTROLE ======

    private void tryPausePrint() {
        checkIfPrintingOrShow(() -> octoPrintController.pausePrint(defaultCallback("Ispis pauziran!")));
    }

    private void tryStopPrint() {
        checkIfPrintingOrShow(() -> octoPrintController.cancelPrint(defaultCallback("Ispis zaustavljen!")));
    }

    private void checkIfPrintingOrShow(Runnable onTrue) {
        Request req = new Request.Builder()
                .url(baseUrl + "/api/job")
                .addHeader("X-Api-Key", apiKey)
                .build();
        httpClient.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> showSweetAlert("Greška", "Nije moguće provjeriti status pisača!", SweetAlertDialog.ERROR_TYPE));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showSweetAlert("Greška", "Ne mogu dohvatiti status pisača.", SweetAlertDialog.ERROR_TYPE));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    String state = obj.optString("state", "");
                    if (state.equalsIgnoreCase("Printing") || state.equalsIgnoreCase("Paused")) {
                        runOnUiThread(onTrue);
                    } else {
                        runOnUiThread(() -> showSweetAlert("Info", "Nema pokrenutog ispisa!", SweetAlertDialog.WARNING_TYPE));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> showSweetAlert("Greška", "Dogodila se greška pri provjeri statusa.", SweetAlertDialog.ERROR_TYPE));
                }
            }
        });
    }

    private void showSweetAlert(String title, String msg, int type) {
        new SweetAlertDialog(this, type)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText("OK")
                .show();
    }

    private Callback defaultCallback(String msg) {
        return new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> showSweetAlert("Greška", "Naredba nije izvršena!", SweetAlertDialog.ERROR_TYPE));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> showSweetAlert("Uspjeh", msg, SweetAlertDialog.SUCCESS_TYPE));
            }
        };
    }

    // ---------- BRISANJE PRINTERA PREKO RETROFITA ----------


    private void showDeleteConfirmation() {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this file!")
                .setConfirmText("Yes, delete it!")
                .setCancelText("Cancel");

        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
                deletePrinterRetrofit();
            }
        });

        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }





    private void deletePrinterRetrofit() {
        Log.d("RETROFIT", "DELETE id: " + printerId);
        Log.d("RETROFIT", "BASE: " + backendApiUrl);
        authApi.deletePrinter(printerId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                Log.d("RETROFIT", "CODE: " + response.code());
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Printer izbrisan")
                                .setMessage("Printer je uspješno obrisan.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    } else {
                        showSweetAlert("Greška", "Brisanje nije uspjelo! (code " + response.code() + ")", 3);
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Log.d("RETROFIT", "Error: " + t.getMessage());
                runOnUiThread(() -> showSweetAlert("Greška", "Mrežna greška: " + t.getMessage(), 3));
            }
        });
    }


}
