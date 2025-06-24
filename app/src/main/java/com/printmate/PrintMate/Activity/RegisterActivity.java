package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.AuthResponse;
import com.printmate.PrintMate.Modeli.GoogleLoginRequest;
import com.printmate.PrintMate.Modeli.RegisterRequest;
import com.printmate.PrintMate.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etLozinka, etLozinkaPonovna;
    private EditText etIme, etPrezime;
    private Spinner  spinnerGender;
    private EditText etDateOfBirth;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1) If already logged in, skip straight to Home
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String existingToken = prefs.getString("jwt_token", null);
        if (existingToken != null && !existingToken.isEmpty()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 2) Initialize views
        etEmail          = findViewById(R.id.etemailRegistrirajSe);
        etLozinka        = findViewById(R.id.etlozinkaRegistrirajSe);
        etLozinkaPonovna = findViewById(R.id.etlozinkaRegistrirajSePonovna);
        etIme            = findViewById(R.id.etImeRegistracija);
        etPrezime        = findViewById(R.id.etPrezimeRegistracija);



        // 3) Register button
        findViewById(R.id.btnRegistrirajSe)
                .setOnClickListener(v -> attemptRegister());

        // 4) Google Sign‐In button
        findViewById(R.id.constraintLayoutRegistrirajSe)
                .setOnClickListener(v -> startGoogleSignIn());

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String idToken = account.getIdToken();
                            if (idToken != null) {
                                sendGoogleTokenToBackend(idToken, account);
                            } else {
                                Toast.makeText(this,
                                        "Nema ID tokena od Google-a.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (ApiException e) {
                            Log.e(TAG,
                                    "Google registracija/prijava neuspjela: " + e.getStatusCode(),
                                    e);
                            Toast.makeText(this,
                                    "Google prijava nije uspjela: " + e.getStatusCode(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this,
                                "Google prijava otkazana.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void attemptRegister() {
        String email      = etEmail.getText().toString().trim();
        String password   = etLozinka.getText().toString().trim();
        String password2  = etLozinkaPonovna.getText().toString().trim();
        String firstName  = etIme.getText().toString().trim();
        String lastName   = etPrezime.getText().toString().trim();
        // Validation
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Unesite valjani email.");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etLozinka.setError("Lozinka mora imati barem 6 znakova.");
            etLozinka.requestFocus();
            return;
        }
        if (!password.equals(password2)) {
            etLozinkaPonovna.setError("Lozinke se ne poklapaju.");
            etLozinkaPonovna.requestFocus();
            return;
        }
        if (firstName.isEmpty()) {
            etIme.setError("Ime je obavezno.");
            etIme.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            etPrezime.setError("Prezime je obavezno.");
            etPrezime.requestFocus();
            return;
        }


        // Build register request
        RegisterRequest req = new RegisterRequest();
        req.email     = email;
        req.password  = password;
        req.firstName = firstName;
        req.lastName  = lastName;
        // If your API supports gender and DOB, add:
        // req.gender = gender;
        // req.dateOfBirth = dateOfBirth;

        AuthApi api = ApiClient.getAuthApi();
        api.register(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    Call<AuthResponse> call,
                    Response<AuthResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    String jwt = response.body().token;
                    Log.d(TAG, "Registracija uspješna. Token=" + jwt);
                    saveAuthToPrefs(jwt, email, firstName + " " + lastName);
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Neuspješna registracija. Provjerite unos ili email već postoji.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Greška pri povezivanju: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
    }

    private void sendGoogleTokenToBackend(String idToken, GoogleSignInAccount account) {
        GoogleLoginRequest req = new GoogleLoginRequest();
        req.idToken = idToken;

        AuthApi api = ApiClient.getAuthApi();
        api.googleLogin(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    Call<AuthResponse> call,
                    Response<AuthResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    String jwt = response.body().token;
                    Log.d(TAG, "Google registracija/prijava uspješna. Token=" + jwt);
                    saveAuthToPrefs(jwt,
                            account.getEmail(),
                            account.getDisplayName());
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Google prijava odbijena od strane servera.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Greška pri Google loginu: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveAuthToPrefs(String token, String email, String fullName) {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("jwt_token",  token)
                .putString("user_email", email)
                .putString("user_name",  fullName)
                .apply();
    }
}
