package com.astarivi.kaizoyu.utils;

import android.content.Context;

import com.astarivi.kaizoyu.R;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;


public class Translation {
    public static @NotNull String getSubTypeTranslation(@NotNull String subtype, @NotNull Context context) {
        return switch (subtype) {
            case "ONA" -> context.getResources().getString(R.string.type_ona);
            case "OVA" -> context.getResources().getString(R.string.type_ova);
            case "TV" -> context.getResources().getString(R.string.type_tv);
            case "MOVIE" -> context.getResources().getString(R.string.type_movie);
            case "SPECIAL" -> context.getResources().getString(R.string.type_special);
            default -> context.getResources().getString(R.string.type_music);
        };
    }

    public static String getNightThemeTranslation(int option, Context context) {
        return switch (option) {
            case 0 -> context.getResources().getString(R.string.night_theme_default);
            case 1 -> context.getResources().getString(R.string.night_theme_day);
            default -> context.getResources().getString(R.string.night_theme_night);
        };
    }

    public static String getLocalizedDow(DayOfWeek dow, Context context) {
        return switch (dow) {
            case MONDAY -> context.getResources().getString(R.string.monday);
            case TUESDAY -> context.getResources().getString(R.string.tuesday);
            case WEDNESDAY -> context.getResources().getString(R.string.wednesday);
            case THURSDAY -> context.getResources().getString(R.string.thursday);
            case FRIDAY -> context.getResources().getString(R.string.friday);
            case SATURDAY -> context.getResources().getString(R.string.saturday);
            default -> context.getResources().getString(R.string.sunday);
        };
    }
}
