package com.astarivi.kaizoyu.core.theme;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public abstract class AppCompatActivityTheme extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(
                Theme.getCurrentTheme().getTheme()
        );

        super.onCreate(savedInstanceState);
    }
}
