package com.printmate.PrintMate.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andreseko.SweetAlert.SweetAlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Modeli.SlikaModelaDto;
import com.printmate.PrintMate.Modeli.UploadResponse;
import com.printmate.PrintMate.Adapter.ImageAdapter;
import com.printmate.PrintMate.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GaleryActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA       = 1001;
    private static final int REQUEST_GALLERY      = 1002;
    private static final int PERMISSION_REQUEST   = 2000;

    private RecyclerView rvGallery;
    private FloatingActionButton fabAddImage;
    private ImageAdapter adapter;
    private int gKodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galery);

        // Dohvati ID G-koda
        gKodId = getIntent().getIntExtra("GKOD_ID", 0);

        // RecyclerView
        rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ImageAdapter();
        rvGallery.setAdapter(adapter);

        // FAB
        fabAddImage = findViewById(R.id.fabAddImage);
        fabAddImage.setOnClickListener(v -> showImageSourceDialog());

        fetchImages();
        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    private void fetchImages() {
        ApiClient.getAuthApi()
                .getByGCode(gKodId)
                .enqueue(new Callback<List<SlikaModelaDto>>() {
                    @Override
                    public void onResponse(Call<List<SlikaModelaDto>> call, Response<List<SlikaModelaDto>> res) {
                        if (res.isSuccessful() && res.body() != null) {
                            adapter.setImages(res.body());
                        } else {
                            new SweetAlertDialog(GaleryActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Greška")
                                    .setContentText("Greška pri učitavanju slika")
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SlikaModelaDto>> call, Throwable t) {
                        new SweetAlertDialog(GaleryActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Network Error")
                                .setContentText(t.getMessage())
                                .show();
                    }
                });
    }

    private void showImageSourceDialog() {
        String[] options = {"Kamera", "Galerija"};
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Odaberite izvor slike")
                .setConfirmText(options[0])
                .setCancelText(options[1])
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    checkCameraPermission();
                })
                .setCancelClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    openGallery();
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.CAMERA },
                    PERMISSION_REQUEST
            );
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Upozorenje")
                        .setContentText("Dozvola za kameru odbijena")
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Bitmap bmp = null;
            if (requestCode == REQUEST_CAMERA) {
                bmp = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_GALLERY) {
                Uri uri = data.getData();
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bmp != null) {
                uploadImage(bmp);
            }
        }
    }

    private void uploadImage(Bitmap bmp) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] bytes = bos.toByteArray();

        RequestBody reqFile = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", "photo.jpg", reqFile);
        RequestBody idBody = RequestBody.create(
                String.valueOf(gKodId),
                MediaType.parse("text/plain")
        );

        ApiClient.getAuthApi()
                .upload(part, idBody)
                .enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        if (response.isSuccessful()) {
                            new SweetAlertDialog(GaleryActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Uspjeh")
                                    .setContentText("Slika dodana!")
                                    .show();
                            fetchImages();
                        } else {
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "nema tijela greške";
                                Log.e("UPLOAD", "HTTP " + response.code() + ": " + err);
                                new SweetAlertDialog(GaleryActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Upload failed")
                                        .setContentText("HTTP " + response.code() + ": " + err)
                                        .show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                new SweetAlertDialog(GaleryActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Upload failed")
                                        .setContentText("Cannot read error body")
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        Log.e("UPLOAD", "network failure", t);
                        new SweetAlertDialog(GaleryActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Network Error")
                                .setContentText(t.getMessage())
                                .show();
                    }
                });
    }
}
