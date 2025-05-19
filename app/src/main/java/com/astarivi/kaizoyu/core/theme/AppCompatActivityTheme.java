package com.astarivi.kaizoyu.core.theme;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


public abstract class AppCompatActivityTheme extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Theme theme = Theme.getCurrentTheme();

        setTheme(
                theme.getTheme()
        );

        if (theme == Theme.HIGH_CONTRAST) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == Theme.KITSUNE_TAKEOVER) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
    }
}
