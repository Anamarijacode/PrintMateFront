package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
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
import com.printmate.PrintMate.Fragmenti.DetaljiFragment;
import com.printmate.PrintMate.Klijenti.ApiClient;
import com.printmate.PrintMate.Klijenti.AuthApi;
import com.printmate.PrintMate.Modeli.AuthResponse;
import com.printmate.PrintMate.Modeli.GoogleLoginRequest;
import com.printmate.PrintMate.Modeli.LoginRequest;
import com.printmate.PrintMate.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etLozinka;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etEmail = findViewById(R.id.etemail);
        etLozinka = findViewById(R.id.etlozinka);
        findViewById(R.id.btnPrijavise).setOnClickListener(v ->
        {
            Intent intent = new Intent(LoginActivity.this, ProizvodaciActivity.class);
            startActivity(intent);
        });

        // Google-gumb (koristi ID "constraintLayout" kao u XML-u)
        //findViewById(R.id.constraintLayout).setOnClickListener(v -> startGoogleSignIn());

        // Link na registraciju
        findViewById(R.id.tvLinkRegistracija).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etLozinka.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email je obavezan.");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Unesite valjani email.");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etLozinka.setError("Lozinka je obavezna.");
            etLozinka.requestFocus();
            return;
        }
        // TODO: you could enforce minimum length, etc.

        // Create the request object:
        LoginRequest request = new LoginRequest();
        request.email = email;
        request.password = password;

        AuthApi authApi = ApiClient.getAuthApi();
        Call<AuthResponse> call = authApi.login(request);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().token;
                    saveTokenToPrefs(token);

                    // Navigate to next Activity (e.g., MainActivity):
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // (optionally) putExtra user email or token if needed
                    startActivity(intent);
                    finish();
                } else {
                    // Maybe parse error body for a message, or just show generic:
                    Toast.makeText(LoginActivity.this, "Neuspješna prijava. Provjerite podatke.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Greška pri povezivanju: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveTokenToPrefs(String token) {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        prefs.edit().putString("jwt_token", token).apply();
    }

    //--------------------------------------------------
    // GOOGLE SIGN-IN (optional)
    //
    // If you want to allow “Login with Google,” you need to:
    // 1. Configure GoogleSignInOptions to request the ID token.
    // 2. Launch the GoogleSignInClient intent.
    // 3. In onActivityResult, get the ID token, and call your backend’s "google-login" endpoint.
    //
    // Below is a sketch. You must:
    // - Put your web client ID from the Google Cloud Console into R.string.default_web_client_id.
    // - Enable Google Sign-In in your Firebase/Google console, if necessary.
    //--------------------------------------------------

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    private void startGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
                    sendIdTokenToBackend(idToken);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google prijava neuspješna: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendIdTokenToBackend(String idToken) {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.idToken = idToken;

        AuthApi authApi = ApiClient.getAuthApi();
        Call<AuthResponse> call = authApi.googleLogin(request);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveTokenToPrefs(response.body().token);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Google prijava odbijena od strane servera.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Greška pri Google loginu: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
