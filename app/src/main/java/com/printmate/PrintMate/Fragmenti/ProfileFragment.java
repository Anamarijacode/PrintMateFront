package com.printmate.PrintMate.Fragmenti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.printmate.PrintMate.Activity.LoginActivity;
import com.printmate.PrintMate.R;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AlertDialog;

public class ProfileFragment extends Fragment {

    private TextView tvAvatar;
    private TextView tvName, tvEmail, tvPrintCount, tvMinutes, tvMaterial;
    private AppCompatButton btnSettings, btnDeleteProfile, btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Views
        tvAvatar       = view.findViewById(R.id.tvAvatar);
        tvName         = view.findViewById(R.id.tvName);
        tvEmail        = view.findViewById(R.id.tvEmail);
        tvPrintCount   = view.findViewById(R.id.tvPrintCount);
        tvMinutes      = view.findViewById(R.id.tvMinutes);
        tvMaterial     = view.findViewById(R.id.tvMaterial);

        btnLogout        = view.findViewById(R.id.btnLogout);

        // Load user data (e.g., from SharedPreferences)
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
        String fullName = prefs.getString("user_name", "Korisnik");
        String email    = prefs.getString("user_email", "email@example.com");
        String prints   = prefs.getString("print_count", "0");
        String minutes  = prefs.getString("print_minutes", "0");
        String material = prefs.getString("preferred_material", "PLA");

        // Set data
        tvName.setText(fullName);
        tvEmail.setText(email);
        tvPrintCount.setText(prints);
        tvMinutes.setText(minutes + " m");
        tvMaterial.setText(material);

        // Set avatar initial (first letter of name)
        if (!fullName.isEmpty()) {
            String initial = fullName.substring(0, 1).toUpperCase();
            tvAvatar.setText(initial);
        }


        // Logout: clear prefs and back to login
        btnLogout.setOnClickListener(v -> {
            // 1. Clear your own tokens / user fields
            prefs.edit().remove("jwt_token")
                    .remove("user_email")
                    .remove("user_name")
                    .apply();

            // 2. Also sign out from Google so the account picker re-appears
            GoogleSignInClient googleClient = GoogleSignIn.getClient(
                    requireContext(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
            );
            googleClient.signOut();

            // 3. And finally navigate home:
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

    }
}
