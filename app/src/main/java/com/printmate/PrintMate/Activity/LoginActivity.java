package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.auth0.android.jwt.JWT;
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
import com.printmate.PrintMate.Modeli.LoginRequest;
import com.printmate.PrintMate.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etLozinka;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String existingToken = prefs.getString("jwt_token", null);
        if (existingToken != null && !existingToken.isEmpty()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).top, 0, 0);
            return insets;
        });

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etEmail = findViewById(R.id.etemail);
        etLozinka = findViewById(R.id.etlozinka);

        findViewById(R.id.btnPrijavise).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.constraintLayout).setOnClickListener(v -> startGoogleSignIn());
        findViewById(R.id.tvLinkRegistracija).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String idToken = account.getIdToken();
                            if (idToken != null) sendIdTokenToBackend(idToken, account);
                            else showToast("Google ID token je null.");
                        } catch (ApiException e) {
                            Log.e(TAG, "Google prijava nije uspjela: " + e.getStatusCode(), e);
                            showToast("Google prijava nije uspjela.");
                        }
                    } else showToast("Google prijava otkazana.");
                }
        );
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etLozinka.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Unesite valjani email.");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etLozinka.setError("Lozinka je obavezna.");
            etLozinka.requestFocus();
            return;
        }

        LoginRequest request = new LoginRequest();
        request.email = email;
        request.password = password;

        ApiClient.getAuthApi().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToPrefs(response.body().token, email, "");
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else showToast("Neuspješna prijava.");
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showToast("Greška pri povezivanju: " + t.getMessage());
            }
        });
    }

    private void startGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
    }

    private void sendIdTokenToBackend(String idToken, GoogleSignInAccount account) {
        GoogleLoginRequest req = new GoogleLoginRequest();
        req.idToken = idToken;

        ApiClient.getAuthApi().googleLogin(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToPrefs(response.body().token, account.getEmail(), account.getDisplayName());
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else showToast("Google prijava odbijena od strane servera.");
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showToast("Greška pri Google loginu: " + t.getMessage());
            }
        });
    }

    private void saveAuthToPrefs(String token, String email, String fullName) {
        JWT jwt = new JWT(token);
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit()
                .putString("jwt_token", token)
                .putString("user_email", email)
                .putString("user_name", fullName)
                .putString("user_id", jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier").asString())
                .apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}