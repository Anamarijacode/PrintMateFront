package com.printmate.PrintMate.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.printmate.PrintMate.Klijenti.OctoPrintController;
import com.printmate.PrintMate.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private OctoPrintController printer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // pretpostavljamo da layout već postoji

        printer = new OctoPrintController();

        Button startButton = findViewById(R.id.btnStart);
        Button pauseButton = findViewById(R.id.btnPause);
        Button resumeButton = findViewById(R.id.btnResume);
        Button cancelButton = findViewById(R.id.btnCancel);

        startButton.setOnClickListener(v -> printer.startPrint(callback("Start")));
        pauseButton.setOnClickListener(v -> printer.pausePrint(callback("Pause")));
        resumeButton.setOnClickListener(v -> printer.resumePrint(callback("Resume")));
        cancelButton.setOnClickListener(v -> printer.cancelPrint(callback("Cancel")));
    }

    private Callback callback(String actionName) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("OCTO", "Greška u mreži: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Mrežna greška: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "Nema odgovora";
                Log.d("OCTO", "Status: " + response.code() + ", Odgovor: " + body);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, actionName + " uspješan!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Greška " + response.code() + ": " + body, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
    }

}
