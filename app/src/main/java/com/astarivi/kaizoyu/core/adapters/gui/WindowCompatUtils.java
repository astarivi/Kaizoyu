package com.astarivi.kaizoyu.core.adapters.gui;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;


public class WindowCompatUtils {
    public static void setOnApplyWindowInsetsListener(View v, OnApplyWindowInsetsListener listener) {
        // For any caller before Android 11, add requestApplyInsets to mimic Android 11 behavior
        // There could be a better way, but this doesn't seem to impact performance that much
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            v.addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                v1.requestApplyInsets()
            );
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                v,
                listener
        );
    }

    public static void setWindowFullScreen(final Window window){
        WindowCompat.setDecorFitsSystemWindows(window, false);
        // After Android 10, on Android 11, Insets behave in a much more predictable way, and
        // this flag makes more sense then. Using it before Android 11, will cause issues.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        window.setStatusBarColor(
                Color.TRANSPARENT
        );
        window.setNavigationBarColor(
                Color.TRANSPARENT
        );
    }
}
