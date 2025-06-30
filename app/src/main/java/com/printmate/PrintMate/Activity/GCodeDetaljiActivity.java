package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.AppCompatButton;

import com.andreseko.SweetAlert.SweetAlertDialog;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.R;

import java.io.IOException;

import retrofit2.Response;             // Retrofit
import retrofit2.Call;
import retrofit2.Callback;

public class GCodeDetaljiActivity extends AppCompatActivity {
    private TextView tvTitle, tvContent;
    private ProgressBar progressBar;
    private AuthApi api;
    private int gcodeId;
    private String baseUrl, apiKey;
    private SweetAlertDialog pdPrint;
    private AppCompatButton btnToggle;
    private boolean isPrinting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gcode_detalji);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tvTitle = findViewById(R.id.nazivGkoda);
        tvContent = findViewById(R.id.tvContent);
        progressBar = findViewById(R.id.progressBar);
        tvContent.setMovementMethod(new ScrollingMovementMethod());
        btnToggle = findViewById(R.id.btnToggle);

        Intent intent = getIntent();
        baseUrl = intent.getStringExtra("BASE_URL");
        apiKey = intent.getStringExtra("API_KEY");
        gcodeId = intent.getIntExtra("GKOD_ID", -1);
        String name = intent.getStringExtra("GKOD_NAME");
        if (gcodeId < 0) {
            Toast.makeText(this, "Invalid G-code ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (name != null && !name.isEmpty()) tvTitle.setText(name);

        api = ApiClient.getAuthApi();
        progressBar.setVisibility(View.VISIBLE);
        tvContent.setText("Učitavanje…");
        loadGcodeText(gcodeId);

        findViewById(R.id.backIcon).setOnClickListener(v -> finish());
        findViewById(R.id.btnGaleri).setOnClickListener(v -> {
            Intent i = new Intent(this, GaleryActivity.class);
            i.putExtra("GKOD_ID", gcodeId);
            startActivity(i);
        });

        btnToggle.setOnClickListener(v -> {
            if (!isPrinting) startPrintOnOcto(gcodeId);
            else cancelPrintOnOcto();
        });
    }

    private void setToggleState(boolean printing) {
        isPrinting = printing;
        if (printing) {
            btnToggle.setText("Zaustavi");
            btnToggle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.rounded_pause_24, 0, 0, 0);
        } else {
            btnToggle.setText(getString(R.string.ispis));
            btnToggle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_play_arrow_24, 0, 0, 0);
        }
    }

    private void loadGcodeText(int id) {
        api.getGcodeText(id)
                .enqueue(new retrofit2.Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            tvContent.setText(response.body());
                        } else {
                            tvContent.setText("Greška pri učitavanju.");
                            Toast.makeText(GCodeDetaljiActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        tvContent.setText("Nema internetske veze.");
                        Toast.makeText(GCodeDetaljiActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startPrintOnOcto(int gcodeId) {
        setToggleState(true);
        pdPrint = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Printing...")
                .setCancelText("Stop")
                .setCancelClickListener(d -> cancelPrintOnOcto());
        pdPrint.setCancelable(false);
        pdPrint.show();

        new OctoPrintController(baseUrl, apiKey, ApiClient.BASE_URL)
                .startPrint(new okhttp3.Callback() {
                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                        runOnUiThread(() -> {
                            pdPrint.dismissWithAnimation();
                            if (response.isSuccessful()) {
                                Toast.makeText(GCodeDetaljiActivity.this,
                                        "Print started",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                setToggleState(false);
                                Toast.makeText(GCodeDetaljiActivity.this,
                                        "OctoPrint vratio: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        runOnUiThread(() -> {
                            pdPrint.dismissWithAnimation();
                            setToggleState(false);
                            Toast.makeText(GCodeDetaljiActivity.this,
                                    "Neuspjeh: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void cancelPrintOnOcto() {
        new OctoPrintController(baseUrl, apiKey, ApiClient.BASE_URL)
                .cancelPrint(new okhttp3.Callback() {
                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                        runOnUiThread(() -> {
                            setToggleState(false);
                            Toast.makeText(GCodeDetaljiActivity.this,
                                    response.isSuccessful()
                                            ? "Print zaustavljen"
                                            : "OctoPrint vratio: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(GCodeDetaljiActivity.this,
                                        "Neuspjeh stopping: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }
}
