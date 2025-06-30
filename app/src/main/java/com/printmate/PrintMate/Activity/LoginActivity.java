// LoginActivity.java
package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.andreseko.SweetAlert.SweetAlertDialog;
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

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
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
                                    showError("Google prijava", "Nema ID tokena od Google-a.");
                                }
                            } catch (ApiException e) {
                                Log.e(TAG, "Google prijava neuspjela: " + e.getStatusCode(), e);
                                showError("Google prijava", "Status: " + e.getStatusCode());
                            }
                        } else {
                            showError("Google prijava", "Otkazano.");
                        }
                    }
            );

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

        etEmail   = findViewById(R.id.etemail);
        etLozinka = findViewById(R.id.etlozinka);

        // show/hide lozinku
        setupPasswordToggle(etLozinka);

        findViewById(R.id.btnPrijavise).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.constraintLayout).setOnClickListener(v -> startGoogleSignIn());
        findViewById(R.id.tvLinkRegistracija).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etLozinka.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Unesite valjani email.");
            etEmail.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            etLozinka.setError("Lozinka mora imati barem 6 znakova.");
            etLozinka.requestFocus();
            return;
        }

        LoginRequest req = new LoginRequest();
        req.email    = email;
        req.password = pass;

        AuthApi api = ApiClient.getAuthApi();
        api.login(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToPrefs(response.body().token, email, null);
                    showSuccess("Prijava uspješna", "Dobrodošli natrag!");
                } else {
                    showError("Prijava neuspjela", "Provjerite podatke ili mrežu.");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showError("Greška pri prijavi", t.getMessage());
            }
        });
    }

    private void startGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
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
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToPrefs(
                            response.body().token,
                            account.getEmail(),
                            account.getDisplayName()
                    );
                    showSuccess("Google prijava uspješna",
                            "Dobrodošli, " + account.getDisplayName() + "!");
                } else {
                    showError("Google prijava odbijena", "Server odbio zahtjev.");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showError("Greška pri Google loginu", t.getMessage());
            }
        });
    }

    private void saveAuthToPrefs(String token, String email, String fullName) {
        JWT jwt = new JWT(token);
        String userId = jwt.getClaim(
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"
        ).asString();

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("jwt_token", token)
                .putString("user_email", email)
                .putString("user_name", fullName)
                .putString("user_id", userId)
                .apply();
    }

    private void setupPasswordToggle(EditText et) {
        // Po defaultu neka bude maskirano
        et.setTransformationMethod(PasswordTransformationMethod.getInstance());
        et.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                ContextCompat.getDrawable(this, R.drawable.ic_visibility_off),
                null
        );

        et.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int dw = et.getCompoundDrawables()[2].getBounds().width();
                // klik na drawable-end?
                if (event.getRawX() >= et.getRight() - dw - et.getPaddingEnd()) {
                    if (et.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // trenutno maskirano → odmaskiraj
                        et.setTransformationMethod(null);
                        et.setCompoundDrawablesWithIntrinsicBounds(
                                null, null,
                                ContextCompat.getDrawable(this, R.drawable.ic_visibility),
                                null
                        );
                    } else {
                        // trenutno vidljivo → maskiraj
                        et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        et.setCompoundDrawablesWithIntrinsicBounds(
                                null, null,
                                ContextCompat.getDrawable(this, R.drawable.ic_visibility_off),
                                null
                        );
                    }
                    // vrati kursor na kraj teksta
                    et.setSelection(et.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
    private void showError(String title, String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .show();
    }

    private void showSuccess(String title, String msg) {
        // 1) napravi i prikaži Sweetalert
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText("OK");
        dialog.show();

        // 2) nakon 2 sekunde zatvori dijalog i navigiraj
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismissWithAnimation();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }, 2000);
    }
}
