package com.astarivi.kaizoyu.utils;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBase;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Locale;


public class Utils {

    /*
    * Many fansubs and ripsubs groups use shortened titles.
    * Searching for "Vinland Saga Season 2" yields no results,
    * while "Vinland Saga S2" does. This method is meant to
    * shorten the title with the goal of making it recognizable
    * to Nibl.
    */
    public static String getNiblReadyTitle(String title) {
        for (int i = 1; i < 9; i++) {
            String intString = Integer.toString(i);

            title = title.replace("Season " + intString, "S" + intString);
        }

        return title;
    }

    // Only accepts ISO-8601 date strings ("2023-03-10")
    @NotNull
    public static String getDateAsQuarters(@NotNull String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        int quarter = date.get(IsoFields.QUARTER_OF_YEAR);
        return String.format(Locale.getDefault(), "%d Q%d", date.getYear(), quarter);
    }

    public static boolean isIPv6Capable() {
        try{
            InetAddress address = InetAddress.getByName("2001:4860:4860::8888");
            return address.isReachable(10000);
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    // Shamelessly stolen function
    public static double similarity(@NotNull String s1, @NotNull String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    @SuppressWarnings("deprecation")
    public static @Nullable AnimeBase getAnimeFromBundle(@NotNull Bundle bundle, @NotNull ModelType.Anime type) {
        switch(type) {
            case BASE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return bundle.getParcelable("anime", Anime.class);
                }
                return bundle.getParcelable("anime");
            case SEASONAL:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return bundle.getParcelable("anime", SeasonalAnime.class);
                }
                return bundle.getParcelable("anime");
            default:
            case LOCAL:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return bundle.getParcelable("anime", LocalAnime.class);
                }
                return bundle.getParcelable("anime");
        }
    }

    @NonNull
    @Contract("_ -> new")
    public static Shader getBrandingTextShader(float textSize) {
        return new LinearGradient(
                0,
                0,
                0,
                textSize,
                new int[] {
                        Color.parseColor("#363d80"),
                        Color.parseColor("#9240aa")
                },
                null,
                Shader.TileMode.CLAMP
        );
    }

    @NonNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static int[] paginateNumber(int page, int total, int perPage) {
        int start = (page - 1) * perPage + 1;
        return new int[]{start, Math.min(start + perPage - 1, total)};
    }
}
