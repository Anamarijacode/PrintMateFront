package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.Modeli.PrinterHome;
import com.printmate.PrintMate.R;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import taimoor.sultani.sweetalert2.Sweetalert;

public class SpojNaPrinterActivity extends AppCompatActivity {
    private EditText etApiKey, etSharedConn;
    private Button btnSpojiSe, btnVratiSe;
    private int modelPrintera;
    private String nazivPrinter;
    private Sweetalert pDialog;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        modelPrintera = getIntent().getIntExtra("idPrinter", -1);
        nazivPrinter  = getIntent().getStringExtra("nazivPrinter");
        etApiKey      = findViewById(R.id.unesitAPi);
        etSharedConn  = findViewById(R.id.sharedConnection);
        btnSpojiSe    = findViewById(R.id.spojiSe);
        btnVratiSe    = findViewById(R.id.vratiSe);

        btnVratiSe.setOnClickListener(v -> finish());

        btnSpojiSe.setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            String url = etSharedConn.getText().toString().trim();
            if (key.isEmpty() || url.isEmpty()) {
                new Sweetalert(this, Sweetalert.WARNING_TYPE)
                        .setTitleText("Nedostaje podatak")
                        .setContentText("Morate unijeti i URL i API ključ.")
                        .setConfirmText("OK")
                        .show();
                return;
            }

            new OctoPrintController(url, key).checkConnection(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> new Sweetalert(
                            SpojNaPrinterActivity.this,
                            Sweetalert.ERROR_TYPE)
                            .setTitleText("Offline")
                            .setContentText("Ne mogu se spojiti na printer.")
                            .setConfirmText("OK")
                            .show());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        onFailure(call, null);
                        return;
                    }
                    runOnUiThread(() -> {
                        pDialog = new Sweetalert(
                                SpojNaPrinterActivity.this,
                                Sweetalert.PROGRESS_TYPE);
                        pDialog.setTitleText("Spremam printer...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                        Log.d("SpojNaPrinter", "Connection OK, spremam printer...");
                        savePrinter(url, key);
                    });
                }
            });
        });
    }

    private void savePrinter(String url, String key) {
        Log.d("SpojNaPrinter", "savePrinter() url=" + url + " key=" + key);
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", null);
        if (currentUserId == null) {
            Log.e("SpojNaPrinter", "Nemam UserId u prefs!");
            runOnUiThread(() -> new Sweetalert(
                    SpojNaPrinterActivity.this,
                    Sweetalert.ERROR_TYPE)
                    .setTitleText("Greška")
                    .setContentText("Nije pronađen UserId.")
                    .setConfirmText("OK")
                    .show());
            return;
        }

        // Priprema objekta za API poziv
        PrinterHome ph = new PrinterHome();
        ph.setNaziv(nazivPrinter);
        ph.setBaseUrl(url);
        ph.setApiKey(key);
        ph.setOnline(true);
        ph.setModelPrintera(10);  // Samo ID šalješ
        ph.setUserId(currentUserId);

        // Logiranje requesta
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d("OkHttp", message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        AuthApi api = ApiClient.getAuthApi();
        Call<PrinterHome> call = api.addPrinter(ph);
        call.enqueue(new Callback<PrinterHome>() {
            @Override
            public void onResponse(Call<PrinterHome> call, Response<PrinterHome> res) {
                Log.d("SpojNaPrinter", "API onResponse code=" + res.code());
                runOnUiThread(() -> {
                    if (res.isSuccessful()) {
                        pDialog.changeAlertType(Sweetalert.SUCCESS_TYPE);
                        pDialog.setTitleText("Printer spremljen")
                                .setContentText("Uspješno spremljeno u bazu.")
                                .setConfirmText("OK")
                                .setConfirmClickListener(d -> {
                                    d.dismissWithAnimation();
                                    finish();
                                });
                    } else {
                        String err;
                        try {
                            err = res.errorBody() != null
                                    ? res.errorBody().string()
                                    : "Nepoznata greška";
                        } catch (IOException e) {
                            err = "Greška pri čitanju: " + e.getMessage();
                        }
                        Log.e("SpojNaPrinter", "API error " + res.code() + ": " + err);
                        pDialog.changeAlertType(Sweetalert.ERROR_TYPE);
                        pDialog.setTitleText("Greška pri spremanju")
                                .setContentText("HTTP " + res.code() + ": " + err)
                                .setConfirmText("OK");
                    }
                });
            }

            @Override
            public void onFailure(Call<PrinterHome> call, Throwable t) {
                Log.e("SpojNaPrinter", "Network error", t);
                runOnUiThread(() -> {
                    pDialog.changeAlertType(Sweetalert.ERROR_TYPE);
                    pDialog.setTitleText("Network error")
                            .setContentText(t.getMessage())
                            .setConfirmText("OK");
                });
            }
        });
    }
}
