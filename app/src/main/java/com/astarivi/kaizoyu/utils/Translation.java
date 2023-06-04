package com.astarivi.kaizoyu.utils;

import android.content.Context;
import android.content.res.Resources;

import com.astarivi.kaizoyu.MainActivity;
import com.astarivi.kaizoyu.R;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;


public class Translation {

    public static @NotNull String getSubTypeTranslation(@NotNull String subtype, @NotNull Context context) {
        switch(subtype) {
            case "ONA":
                return context.getResources().getString(R.string.type_ona);
            case "OVA":
                return context.getResources().getString(R.string.type_ova);
            case "TV":
                return context.getResources().getString(R.string.type_tv);
            case "movie":
                return context.getResources().getString(R.string.type_movie);
            case "special":
                return context.getResources().getString(R.string.type_special);
            case "music":
            default:
                return context.getResources().getString(R.string.type_music);
        }
    }

    public static String getNightThemeTranslation(int option, Context context) {
        switch(option) {
            case 0:
                return context.getResources().getString(R.string.night_theme_default);
            case 1:
                return context.getResources().getString(R.string.night_theme_day);
            default:
                return context.getResources().getString(R.string.night_theme_night);
        }
    }

    public static String getLocalizedDow(DayOfWeek dow, Context context) {
        switch(dow) {
            case MONDAY:
                return context.getResources().getString(R.string.monday);
            case TUESDAY:
                return context.getResources().getString(R.string.tuesday);
            case WEDNESDAY:
                return context.getResources().getString(R.string.wednesday);
            case THURSDAY:
                return context.getResources().getString(R.string.thursday);
            case FRIDAY:
                return context.getResources().getString(R.string.friday);
            case SATURDAY:
                return context.getResources().getString(R.string.saturday);
            default:
                return context.getResources().getString(R.string.sunday);
        }

    }

    // QoL function to be used in safe functions
    public static Resources getResources() {
        return MainActivity.getInstance().getResources();
    }
}
