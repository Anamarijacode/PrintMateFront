// GKodActivity.java
package com.printmate.PrintMate.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andreseko.SweetAlert.SweetAlertDialog;
import com.google.gson.Gson;
import com.printmate.PrintMate.Adapter.GkodAdapter;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.Gkod;
import com.printmate.PrintMate.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GKodActivity extends AppCompatActivity {

    private static final int PICK_GKOD_REQUEST = 1234;
    private AuthApi apiService;
    private String userId;
    private GkodAdapter adapter;
    private String baseUrl, apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gkod);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        baseUrl = getIntent().getStringExtra("base_url");
        apiKey  = getIntent().getStringExtra("api_key");
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        userId = prefs.getString("user_id", "");

        apiService = ApiClient.getAuthApi();
        RecyclerView rv = findViewById(R.id.gkodRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GkodAdapter(new ArrayList<>(),   baseUrl,
                apiKey,g -> {

        });
        rv.setAdapter(adapter);
        findViewById(R.id.fabAddGkod).setOnClickListener(v -> showAddGkodDialog());

        fetchGkodList();
    }

    // u GKodActivity.java
    private void fetchGkodList() {
        apiService.getGkodList().enqueue(new Callback<List<Gkod>>() {
            @Override
            public void onResponse(Call<List<Gkod>> call, Response<List<Gkod>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Gkod> lista = response.body();
                    Log.d("GKodActivity", "Prvi GKOD iz JSON-a: " + new Gson().toJson(lista.get(0)));
                    adapter.updateData(lista);
                } else {
                    adapter.updateData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Gkod>> call, Throwable t) {
                adapter.updateData(new ArrayList<>());
            }
        });
    }


    private void showAddGkodDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_add_gkod, null);
        EditText et = dv.findViewById(R.id.editTextGkodName);
        new AlertDialog.Builder(this)
                .setTitle("Dodaj G-KOD")
                .setView(dv)
                .setPositiveButton("Dalje", (dlg, w) -> {
                    String naziv = et.getText().toString().trim();
                    if (naziv.isEmpty()) {
                        showError("Naziv je obavezan!");
                    } else {
                        pickGkodFile(naziv);
                    }
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void pickGkodFile(String naziv) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, PICK_GKOD_REQUEST);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_GKOD_REQUEST && res == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String name = getFileName(uri);
            if (!name.toLowerCase().endsWith(".gcode")) {
                showError("Samo .gcode datoteke");
                return;
            }
            uploadGkod(uri, name);
        }
    }

    private void uploadGkod(Uri uri, String naziv) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String uid = prefs.getString("user_id", "");
        if (uid == null || uid.isEmpty()) {
            showError("Prijavite se ponovo!");
            return;
        }
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int r;
            while ((r = is.read(data)) != -1) buf.write(data, 0, r);
            is.close();

            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file", naziv,
                    RequestBody.create(MediaType.parse("application/octet-stream"), buf.toByteArray())
            );
            RequestBody nazBody = RequestBody.create(MediaType.parse("text/plain"), naziv);
            RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), uid);

            SweetAlertDialog pd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText("Učitavanje...")
                    .setContentText("Dodavanje G-KODA");
            pd.setCancelable(false);
            pd.show();

            apiService.uploadGkod(filePart, nazBody, idBody).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                    pd.dismissWithAnimation();
                    if (r.isSuccessful()) {
                        new SweetAlertDialog(GKodActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Uspjeh")
                                .setContentText("G-KOD dodan!").show();
                        fetchGkodList();
                    } else {
                        new SweetAlertDialog(GKodActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Greška")
                                .setContentText("Code: " + r.code()).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> c, Throwable t) {
                    pd.dismissWithAnimation();
                    showError("Greška: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Greška")
                .setContentText(msg)
                .show();
    }

    private String getFileName(Uri uri) {
        String res = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (i >= 0) res = c.getString(i);
                }
            }
        }
        if (res == null && uri.getPath() != null) {
            String path = uri.getPath();
            int cut = path.lastIndexOf('/');
            res = (cut != -1 ? path.substring(cut + 1) : path);
        }
        return res != null ? res : "gcode.gcode";
    }
}