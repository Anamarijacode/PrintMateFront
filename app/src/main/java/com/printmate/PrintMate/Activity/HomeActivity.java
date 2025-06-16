package com.printmate.PrintMate.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.printmate.PrintMate.R;
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation;

import java.util.Arrays;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class HomeActivity extends AppCompatActivity {

    private static final int HOME_ITEM      = R.id.homeFragment;
    private static final int MATERIALS_ITEM = R.id.materialsFragment;
    private static final int ARCHIVE_ITEM   = R.id.archiveFragment;
    private static final int PROFILE_ITEM   = R.id.profileFragment;

    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        View curved = findViewById(R.id.curvedNav);
        ViewCompat.setOnApplyWindowInsetsListener(curved, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Postavi samo bottom padding, ostavi ostale netaknute
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    sys.bottom
            );

            return insets;
        });


if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initNavHost();
        setUpBottomNavigation();
    }


    private void initNavHost() {
        FragmentManager fm = getSupportFragmentManager();
        NavHostFragment navHost = (NavHostFragment)
                fm.findFragmentById(R.id.nav_host_fragment);
        if (navHost == null) {
            throw new IllegalStateException("NavHostFragment nije pronađen u layoutu!");
        }
        navController = navHost.getNavController();
    }

    private void setUpBottomNavigation() {
        // Priprema stavki
        List<CurvedBottomNavigation.Model> items = Arrays.asList(
                new CurvedBottomNavigation.Model(HOME_ITEM,      "Početna",   R.drawable.ic_home),
                new CurvedBottomNavigation.Model(MATERIALS_ITEM, "Materijali",R.drawable.ic_grid),
                new CurvedBottomNavigation.Model(ARCHIVE_ITEM,   "Arhiva",    R.drawable.ic_archive),
                new CurvedBottomNavigation.Model(PROFILE_ITEM,   "Profil",    R.drawable.ic_user)
        );

        // Dohvat zakrivljene navigacije
        CurvedBottomNavigation bottomNav = findViewById(R.id.curvedNav);

        // Dodavanje stavki
        for (CurvedBottomNavigation.Model m : items) {
            bottomNav.add(m);
        }

        // Klik‐listener za navigaciju
        bottomNav.setOnClickMenuListener(new Function1<CurvedBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(CurvedBottomNavigation.Model model) {
                navController.navigate(model.getId());
                return Unit.INSTANCE;
            }
        });

        // Defaultni prikaz Početne (bez animacije)
        bottomNav.show(HOME_ITEM, false);

        // Sinkronizacija s NavControllerom
        bottomNav.setupNavController(navController);
    }

    @Override
    public void onBackPressed() {
        int current = navController.getCurrentDestination().getId();
        if (current == HOME_ITEM) {
            super.onBackPressed();
        } else {
            navController.popBackStack(HOME_ITEM, false);
        }
    }
}
