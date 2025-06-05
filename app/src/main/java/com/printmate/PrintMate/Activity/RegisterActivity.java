package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etLozinka, etLozinkaPonovna;
    private EditText etIme, etPrezime;
    private Spinner spinnerGender;
    private EditText etDateOfBirth;

    // Za Google Sign-In
    private static final int RC_SIGN_IN = 9002;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicijaliziraj View komponente
        etEmail            = findViewById(R.id.etemailRegistrirajSe);
        etLozinka          = findViewById(R.id.etlozinkaRegistrirajSe);
        etLozinkaPonovna   = findViewById(R.id.etlozinkaRegistrirajSePonovna);
        etIme              = findViewById(R.id.etImeRegistracija);
        etPrezime          = findViewById(R.id.etPrezimeRegistracija);
        // Gumb “Registriraj se”
        findViewById(R.id.btnRegistrirajSe).setOnClickListener(v -> attemptRegister());

        // Gumb “Registriraj se s Google”
        findViewById(R.id.constraintLayoutRegistrirajSe).setOnClickListener(v -> startGoogleSignIn());
    }

    // ----- 1. KORAK: Standardna registracija (email + lozinka + ostalo) -----
    private void attemptRegister() {
        String email     = etEmail.getText().toString().trim();
        String password  = etLozinka.getText().toString().trim();
        String password2 = etLozinkaPonovna.getText().toString().trim();
        String firstName = etIme.getText().toString().trim();
        String lastName  = etPrezime.getText().toString().trim();
        String dob       = etDateOfBirth.getText().toString().trim();          // npr. “1990-10-05”
        String gender    = spinnerGender.getSelectedItem().toString();         // npr. “M” ili “F” ili “Other”

        // Validacija
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
        if (TextUtils.isEmpty(dob)) {
            etDateOfBirth.setError("Datum rođenja je obavezan.");
            etDateOfBirth.requestFocus();
            return;
        }
        // TODO: Dodati provjeru formata datuma ako treba, ili koristiti DatePicker

        // Sastavi DTO za registraciju
        RegisterRequest request = new RegisterRequest();
        request.email       = email;
        request.password    = password;
        request.firstName   = firstName;
        request.lastName    = lastName;
        request.dateOfBirth = dob;          // Backend očekuje string “yyyy-MM-dd”
        request.genderValue = gender;       // Provjeri da li backend želi “M”/“F” ili “Male”/“Female”

        AuthApi authApi = ApiClient.getAuthApi();
        Call<AuthResponse> call = authApi.register(request);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().token;
                    saveTokenToPrefs(token);

                    Toast.makeText(RegisterActivity.this, "Registracija uspješna!", Toast.LENGTH_SHORT).show();
                    // Nakon registracije, nastavi na glavni
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
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

    private void saveTokenToPrefs(String token) {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        prefs.edit().putString("jwt_token", token).apply();
    }

    // ----- 2. KORAK: Google Sign-In registracija/prijava -----
    private void startGoogleSignIn() {
        // Konfiguriraj GoogleSignInOptions: zatraži ID token
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Ovdje moraš postaviti svoj web client ID u strings.xml:
                // <string name="default_web_client_id">TVOJ_WEB_CLIENT_ID</string>
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                if (idToken != null) {
                    // Pošalji ID token na backend (isti endpoint kao za login)
                    sendIdTokenToBackend(idToken);
                } else {
                    Toast.makeText(this, "Nismo dobili ID token od Googlea.", Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google prijava neuspješna: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendIdTokenToBackend(String idToken) {
        Log.d("GoogleLoginFlow", "Počinjem slanje ID tokena na backend: " +
                idToken.substring(0, Math.min(idToken.length(), 10)) + "…");

        GoogleLoginRequest request = new GoogleLoginRequest();
        request.idToken = idToken;

        AuthApi authApi = ApiClient.getAuthApi();
        Call<AuthResponse> call = authApi.googleLogin(request);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("GoogleLoginFlow", "Google prijava uspješna, dobiven JWT.");
                    saveTokenToPrefs(response.body().token);
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d("GoogleLoginFlow", "Google prijava nije odobrena od servera. Kod: " + response.code());
                    Toast.makeText(RegisterActivity.this, "Google prijava odbijena.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.d("GoogleLoginFlow", "Greška pri Google loginu: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Greška pri Google loginu: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
