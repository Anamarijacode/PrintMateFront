package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TuneActivity extends AppCompatActivity {
    private OctoPrintController octoPrintController;

    private SeekBar seekSpeed, seekNozzle, seekBed, seekFan, seekZoffset;
    private TextView txtSpeed, txtNozzle, txtBed, txtFan, txtZoffset;

    // Trenutne vrijednosti s printera
    private int currentNozzleTemp = -1;
    private int currentBedTemp = -1;
    private int currentFan = -1;
    private final int currentFeedrate = -1;
    private float currentZoffset = 0;
    private boolean isPrinting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        ImageView logo = findViewById(R.id.logoOdabir);

        boolean isDark = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDark) {
            logo.setImageResource(R.drawable.logoandiconwhite);
        } else {
            logo.setImageResource(R.drawable.logoandiconviolet);
        }

        // Dohvati podatke iz Intenta
        Intent intent = getIntent();
        String baseUrl = intent.getStringExtra("base_url");
        String apiKey = intent.getStringExtra("api_key");
        octoPrintController = new OctoPrintController(baseUrl, apiKey);

        // UI elementi
        seekSpeed = findViewById(R.id.seek_speed);
        seekNozzle = findViewById(R.id.seek_nozzle);
        seekBed = findViewById(R.id.seek_bed);
        seekFan = findViewById(R.id.seek_fan);
        seekZoffset = findViewById(R.id.seek_zoffset);

        txtSpeed = findViewById(R.id.txt_speed_value);
        txtNozzle = findViewById(R.id.txt_nozzle_value);
        txtBed = findViewById(R.id.txt_bed_value);
        txtFan = findViewById(R.id.txt_fan_value);
        txtZoffset = findViewById(R.id.txt_zoffset_value);

        ImageView back = findViewById(R.id.backIcon);
        back.setOnClickListener(v -> finish());

        // -- SLIDER LISTENERI --

        // FEEDRATE
        seekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtSpeed.setText(progress + "% (trenutno: " +
                        (currentFeedrate >= 0 ? currentFeedrate : "--") + "%)");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPrinting) {
                    octoPrintController.setFeedrate(seekBar.getProgress(), emptyCallback());
                }
            }
        });

        // FAN
        seekFan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtFan.setText(progress + "% (trenutno: " +
                        (currentFan >= 0 ? currentFan : "--") + "%)");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPrinting) {
                    int value = (int)(seekBar.getProgress() * 2.55f); // 0-100% u 0-255
                    octoPrintController.setFanSpeed(value, emptyCallback());
                }
            }
        });

        // NOZZLE TEMP
        seekNozzle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtNozzle.setText(progress + "°C (trenutno: " +
                        (currentNozzleTemp >= 0 ? currentNozzleTemp : "--") + "°C)");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                int temp = seekBar.getProgress();
                octoPrintController.setNozzleTemp(temp, emptyCallback());
            }
        });

        // BED TEMP
        seekBed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtBed.setText(progress + "°C (trenutno: " +
                        (currentBedTemp >= 0 ? currentBedTemp : "--") + "°C)");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                int temp = seekBar.getProgress();
                octoPrintController.setBedTemp(temp, emptyCallback());
            }
        });

        // Z-OFFSET
        seekZoffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float z = (progress - 30) / 10.0f;
                txtZoffset.setText(String.format("%.2f", z) + " (trenutno: " +
                        String.format("%.2f", currentZoffset) + ")");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                float z = (seekBar.getProgress() - 30) / 10.0f;
                octoPrintController.setZOffset(z, emptyCallback());
            }
        });

        // Podesi slider limite i početne vrijednosti
        fetchPrinterSettingsAndFillSliders();

        // Dohvati trenutne vrijednosti s printera
        fetchCurrentPrinterState();

        // Provjeri status printanja za enable slidera
        checkIfPrintingAndEnableSliders();
    }

    private Callback emptyCallback() {
        return new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) {}
        };
    }

    private void fetchCurrentPrinterState() {
        octoPrintController.getPrinterState(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    JSONObject tempObj = obj.getJSONObject("temperature");

                    currentNozzleTemp = (int) tempObj.getJSONObject("tool0").getDouble("actual");
                    currentBedTemp = (int) tempObj.getJSONObject("bed").getDouble("actual");

                    // Fan i feedrate - često nisu dostupni direktno, ostavi "--" ako nije iz API-a
                    currentFan = tempObj.has("fan") ? tempObj.getInt("fan") : -1;

                    // Z-offset - često je u custom fieldu ili pluginu, simuliraj s 0 ili dohvatiti iz posebnog endpointa ako imaš!
                    currentZoffset = tempObj.has("zoffset") ? (float) tempObj.getDouble("zoffset") : 0f;

                    runOnUiThread(this::updateAllViews);
                } catch (Exception ignored) {}
            }
            private void updateAllViews() {
                txtNozzle.setText(seekNozzle.getProgress() + "°C (trenutno: " +
                        (currentNozzleTemp >= 0 ? currentNozzleTemp : "--") + "°C)");
                txtBed.setText(seekBed.getProgress() + "°C (trenutno: " +
                        (currentBedTemp >= 0 ? currentBedTemp : "--") + "°C)");
                txtFan.setText(seekFan.getProgress() + "% (trenutno: " +
                        (currentFan >= 0 ? currentFan : "--") + "%)");
                txtSpeed.setText(seekSpeed.getProgress() + "% (trenutno: " +
                        (currentFeedrate >= 0 ? currentFeedrate : "--") + "%)");
                txtZoffset.setText(String.format("%.2f", ((float)seekZoffset.getProgress() - 30)/10.0f)
                        + " (trenutno: " + String.format("%.2f", currentZoffset) + ")");
            }
        });
    }

    private void fetchPrinterSettingsAndFillSliders() {
        octoPrintController.getPrinterSettings(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(TuneActivity.this::setDefaultSliderLimits);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(TuneActivity.this::setDefaultSliderLimits);
                    return;
                }
                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);

                    int maxNozzle = 300, maxBed = 120, maxFan = 100, maxSpeed = 150;

                    // ...kao prije, možeš ostaviti default ili parsirati detaljnije po želji

                    int finalMaxNozzle = maxNozzle;
                    int finalMaxBed = maxBed;
                    int finalMaxFan = maxFan;
                    int finalMaxSpeed = maxSpeed;
                    runOnUiThread(() -> {
                        seekNozzle.setMax(finalMaxNozzle);
                        seekBed.setMax(finalMaxBed);
                        seekFan.setMax(finalMaxFan);
                        seekSpeed.setMax(finalMaxSpeed);
                        seekNozzle.setProgress(finalMaxNozzle / 2);
                        seekBed.setProgress(finalMaxBed / 2);
                        seekFan.setProgress(finalMaxFan / 2);
                        seekSpeed.setProgress(finalMaxSpeed / 2);
                        seekZoffset.setProgress(30);
                    });
                } catch (Exception e) {
                    runOnUiThread(TuneActivity.this::setDefaultSliderLimits);
                }
            }
        });
    }

    private void setDefaultSliderLimits() {
        seekNozzle.setMax(300);
        seekBed.setMax(120);
        seekFan.setMax(100);
        seekSpeed.setMax(150);
        seekNozzle.setProgress(150);
        seekBed.setProgress(60);
        seekFan.setProgress(50);
        seekSpeed.setProgress(75);
        seekZoffset.setProgress(30);
    }

    private void checkIfPrintingAndEnableSliders() {
        octoPrintController.getPrinterState(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    seekSpeed.setEnabled(false);
                    seekFan.setEnabled(false);
                });
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        seekSpeed.setEnabled(false);
                        seekFan.setEnabled(false);
                    });
                    return;
                }
                try {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    String state = "";
                    if (obj.has("state")) {
                        JSONObject stateObj = obj.getJSONObject("state");
                        state = stateObj.optString("text", "");
                    }
                    isPrinting = state.equalsIgnoreCase("Printing") || state.equalsIgnoreCase("Paused");
                    runOnUiThread(() -> {
                        seekSpeed.setEnabled(isPrinting);
                        seekFan.setEnabled(isPrinting);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        seekSpeed.setEnabled(false);
                        seekFan.setEnabled(false);
                    });
                }
            }
        });
    }
}
