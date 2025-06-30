// RegisterActivity.java
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
import androidx.core.graphics.Insets;
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
import com.printmate.PrintMate.Modeli.RegisterRequest;
import com.printmate.PrintMate.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etLozinka, etLozinkaPonovna, etIme, etPrezime;
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
                                    showError("Google registracija", "Nema ID tokena od Google-a.");
                                }
                            } catch (ApiException e) {
                                Log.e(TAG, "Google registracija neuspjela: " + e.getStatusCode(), e);
                                showError("Google registracija", "Status: " + e.getStatusCode());
                            }
                        } else {
                            showError("Google registracija", "Otkazano.");
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1) Ako već postoji token, preskoči
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

        // 2) Init view-ove
        etEmail          = findViewById(R.id.etemailRegistrirajSe);
        etLozinka        = findViewById(R.id.etlozinkaRegistrirajSe);
        etLozinkaPonovna = findViewById(R.id.etlozinkaRegistrirajSePonovna);
        etIme            = findViewById(R.id.etImeRegistracija);
        etPrezime        = findViewById(R.id.etPrezimeRegistracija);

        // 3) Show/Hide lozinki
        setupPasswordToggle(etLozinka);
        setupPasswordToggle(etLozinkaPonovna);

        // 4) Registracija
        findViewById(R.id.btnRegistrirajSe)
                .setOnClickListener(v -> attemptRegister());

        // 5) Google Sign-In
        findViewById(R.id.constraintLayoutRegistrirajSe)
                .setOnClickListener(v -> startGoogleSignIn());
    }

    private void attemptRegister() {
        String email     = etEmail.getText().toString().trim();
        String password  = etLozinka.getText().toString().trim();
        String password2 = etLozinkaPonovna.getText().toString().trim();
        String firstName = etIme.getText().toString().trim();
        String lastName  = etPrezime.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Unesite valjani email.");
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
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

        RegisterRequest req = new RegisterRequest();
        req.email     = email;
        req.password  = password;
        req.firstName = firstName;
        req.lastName  = lastName;

        AuthApi api = ApiClient.getAuthApi();
        api.register(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToPrefs(response.body().token, email, firstName + " " + lastName);
                    showSuccess("Registracija uspješna", "Dobrodošli, " + firstName + "!");
                } else {
                    showError("Registracija neuspjela", "Provjerite unos ili email već postoji.");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showError("Greška pri povezivanju", t.getMessage());
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
                    showSuccess("Google registracija", "Dobrodošli, " + account.getDisplayName() + "!");
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
