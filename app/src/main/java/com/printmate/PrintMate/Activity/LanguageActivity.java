package com.printmate.PrintMate.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.printmate.PrintMate.R;

import java.util.Arrays;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {
    private static final String PREFS = "app_prefs";
    private static final String KEY_LANG = "selected_language";

    private Spinner spinner;
    private final String[] languageCodes = {
            "choose", "en", "hr", "bs", "sr", "sk", "sl", "hu"
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS, MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, Locale.getDefault().getLanguage());

        Resources res = newBase.getResources();
        Configuration config = res.getConfiguration();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());

        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        String[] names = getResources().getStringArray(R.array.languages);
        spinner = findViewById(R.id.spinnerJezik);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_list, names);
        adapter.setDropDownViewResource(R.layout.spinner_dopdown_item);
        spinner.setAdapter(adapter);

        String current = getCurrentLocale().getLanguage();
        int sel = Arrays.asList(languageCodes).indexOf(current);
        spinner.setSelection(sel >= 0 ? sel : 0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (first) { first = false; return; }
                String code = languageCodes[pos];
                if (!code.equals(getCurrentLocale().getLanguage())) {
                    getSharedPreferences(PREFS, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_LANG, code)
                            .apply();

                    setLocale(code);
                    recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btnZapocnimo).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }


    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources res = getResources();
        Configuration config = res.getConfiguration();

        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }


    private Locale getCurrentLocale() {
        Configuration cfg = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return cfg.getLocales().get(0);
        } else {
            return cfg.locale;
        }
    }
}
