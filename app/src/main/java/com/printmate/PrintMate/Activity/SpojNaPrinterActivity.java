package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.andreseko.SweetAlert.SweetAlertDialog;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.R;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpojNaPrinterActivity extends AppCompatActivity {
    private EditText etApiKey, etSharedConn;
    private Button   btnSpojiSe, btnVratiSe;
    private int      modelPrintera;
    private String   nazivPrinter;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spoj_na_printer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        modelPrintera = getIntent().getIntExtra("idPrinter", -1);
        nazivPrinter  = getIntent().getStringExtra("nazivPrinter");
        etApiKey      = findViewById(R.id.unesitAPi);
        etSharedConn  = findViewById(R.id.sharedConnection);
        btnSpojiSe    = findViewById(R.id.spojiSe);
        btnVratiSe    = findViewById(R.id.vratiSe);

        btnVratiSe.setOnClickListener(v -> finish());
        TextView tvKako = findViewById(R.id.kakoDobitiSharedConnection);
        tvKako.setOnClickListener(v -> {
            Intent i = new Intent(SpojNaPrinterActivity.this, OctoEverwereActivity.class);
            startActivity(i);
        });

        TextView kakoPronaciAPI = findViewById(R.id.kakoPronaciAPI);
        kakoPronaciAPI.setOnClickListener(v -> {
            Intent i = new Intent(SpojNaPrinterActivity.this, Octoprint_api_Activity.class);
            startActivity(i);
        });
        btnSpojiSe.setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            String url = etSharedConn.getText().toString().trim();
            if (key.isEmpty() || url.isEmpty()) {
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Nedostaje podatak")
                        .setContentText("Morate unijeti i URL i API ključ.")
                        .setConfirmText("OK")
                        .show();
                return;
            }

            new OctoPrintController(url, key).checkConnection(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> {
                        // 1) Instanciraj dijalog
                        SweetAlertDialog dlg = new SweetAlertDialog(
                                SpojNaPrinterActivity.this,
                                SweetAlertDialog.ERROR_TYPE
                        );
                        // 2) Pozovi svaki setter zasebno
                        dlg.setTitleText("Offline");
                        dlg.setContentText("Ne mogu se spojiti na printer.");
                        dlg.setConfirmText("OK");
                        dlg.setCancelable(true);
                        // 3) Pokaži dijalog
                        dlg.show();
                        // 4) Dozvoli dismiss dodirom izvan
                        dlg.setCanceledOnTouchOutside(true);
                    });
                }


                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        onFailure(call, null);
                        return;
                    }
                    runOnUiThread(() -> {
                        pDialog = new SweetAlertDialog(
                                SpojNaPrinterActivity.this,
                                SweetAlertDialog.PROGRESS_TYPE);
                        pDialog.setTitleText("Spremam printer...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                        Log.d("SpojNaPrinter", "Connection OK, spremam printer…");
                        savePrinter(url, key);
                    });
                }
            });
        });
    }

    private void savePrinter(String url, String key) {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", null);
        if (currentUserId == null) {
            runOnUiThread(() -> new SweetAlertDialog(
                    SpojNaPrinterActivity.this,
                    SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Greška")
                    .setContentText("Nije pronađen UserId.")
                    .setConfirmText("OK")
                    .show());
            return;
        }

        PrinterHome ph = new PrinterHome();
        ph.setNaziv(nazivPrinter);
        ph.setBaseUrl(url);
        ph.setApiKey(key);
        ph.setOnline(true);
        ph.setModelPrintera(modelPrintera);
        ph.setUserId(currentUserId);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                message -> Log.d("OkHttp", message)
        );
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        // Ako Retrofit autogenerira vlastiti OkHttpClient, prilagodi ApiClient da koristi gornji client

        AuthApi api = ApiClient.getAuthApi();
        Call<PrinterHome> call = api.addPrinter(ph);
        call.enqueue(new Callback<PrinterHome>() {
            @Override
            public void onResponse(Call<PrinterHome> call, Response<PrinterHome> res) {
                runOnUiThread(() -> {
                    if (res.isSuccessful()) {
                        // promijeni u SUCCESS
                        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.setTitleText("Printer spremljen")
                                .setContentText("Uspješno spremljeno.")
                                .setConfirmText("OK");
                        // nakon 2 sekunde automatski prelazimo na MainActivity
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent i = new Intent(
                                    SpojNaPrinterActivity.this,
                                    HomeActivity.class
                            );
                            i.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_NEW_TASK
                            );
                            startActivity(i);
                            finish();
                        }, 2000);
                    } else {
                        pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        pDialog.setTitleText("Greška pri spremanju")
                                .setContentText("HTTP " + res.code())
                                .setConfirmText("OK");
                    }
                });
            }

            @Override
            public void onFailure(Call<PrinterHome> call, Throwable t) {
                runOnUiThread(() -> {
                    pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    pDialog.setTitleText("Network Error")
                            .setContentText(t.getMessage())
                            .setConfirmText("OK");
                });
            }
        });
    }
}
