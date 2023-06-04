package com.astarivi.kaizoyu.core.theme;

import android.content.Context;

import com.astarivi.kaizoyu.MainActivity;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;


public enum Theme {
    HIGH_CONTRAST(0, R.style.HighContrast, R.string.theme_high_contrast, R.string.theme_high_contrast_description),
    DYNAMIC_COLORS(1, R.style.AppTheme, R.string.theme_dynamic_colors, R.string.theme_dynamic_colors_description),
    LOW_CONTRAST(2, R.style.AppTheme, R.string.theme_low_contrast, R.string.theme_low_contrast_description);

    private final int id;
    private final int appTheme;
    private final int title;
    private final int description;

    Theme(int i, int theme, int t, int d) {
        id = i;
        appTheme = theme;
        title = t;
        description = d;
    }

    public int getId() {
        return id;
    }

    public int getTheme() {
        return appTheme;
    }

    public @NotNull String getTitle(@NotNull Context context) {
        return context.getResources().getString(title);
    }

    public @NotNull String getDescription(@NotNull Context context) {
        return context.getResources().getString(description);
    }

    public static @NotNull Theme getCurrentTheme() {
        if (MainActivity.weakActivity == null) return HIGH_CONTRAST;

        int currentTheme = Integer.parseInt(
                Data.getProperties(
                        Data.CONFIGURATION.APP
                ).getProperty(
                        "app_theme",
                        "0"
                )
        );

        return Theme.values()[currentTheme];
    }

    public synchronized static void setTheme(@NotNull Theme themeToApply, @NotNull Context context) {
        //noinspection ResultOfMethodCallIgnored
        new File(context.getFilesDir(), "config/disabledcolor.bool").delete();

        Data.getProperties(
                Data.CONFIGURATION.APP
        ).setProperty(
                "app_theme",
                String.valueOf(
                        themeToApply.getId()
                )
        );

        Data.saveProperties(Data.CONFIGURATION.APP);

        if (themeToApply != DYNAMIC_COLORS) {
            try {
                //noinspection ResultOfMethodCallIgnored
                new File(context.getFilesDir(), "config/disabledcolor.bool").createNewFile();
            } catch (IOException e) {
                Logger.error("Couldn't write disabledcolor.bool file due to an IOException.");
            }
        }
    }
}
