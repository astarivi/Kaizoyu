package com.astarivi.kaizoyu.utils;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityManagerCompat;

import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.core.annotations.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBase;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Locale;
import java.util.Random;


public class Utils {
    @ThreadedOnly
    public static void makeToastRegardless(Context context, @StringRes int resId, int duration) {
        try {
            runOnUiThread(() -> Toast.makeText(
                    context,
                    resId,
                    duration
            ).show());
        } catch(Exception ignored) {
        }
    }

    public static boolean copyToClipboard(Context context, String label, String content) {
        try {
            ClipData clip = ClipData.newPlainText(label, content);
            ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clip);
        } catch(Exception ignored) {
            // Shit happens ig
            return false;
        }

        return true;
    }

    // Only accepts ISO-8601 date strings ("2023-03-10")
    @NotNull
    public static String getDateAsQuarters(@NotNull String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        int quarter = date.get(IsoFields.QUARTER_OF_YEAR);
        return String.format(Locale.getDefault(), "%d Q%d", date.getYear(), quarter);
    }

    // Very flawed, but ey, this is all I got
    public static boolean isIPv6Capable() {
        try{
            InetAddress address = InetAddress.getByName("2001:4860:4860::8888");
            return address.isReachable(10000);
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
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

    public static void generateIrcUsername(ExtendedProperties appProperties) {
        if (!appProperties.getFilename().equals("Kaizoyu.properties")) return;

        String ircName = appProperties.getProperty("ircName");

        // Name already exists
        if (ircName != null && !ircName.equals("null")){
            return;
        }

        String randomChars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();
        while (randomString.length() < 7) {
            int index = (int) (random.nextFloat() * randomChars.length());
            randomString.append(randomChars.charAt(index));
        }
        appProperties.setProperty("ircName", "KaiZ" + randomString);
        appProperties.save();
    }

    public static void clearCache() {
        if (KaizoyuApplication.application == null) return;

        File[] files = KaizoyuApplication.getApplication().getCacheDir().listFiles();
        if(files == null) {
            return;
        }

        for(File file : files) {
            if (!file.delete()) file.deleteOnExit();
        }
    }

    // Use with legacy devices. How legacy? Android 6
    public static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static boolean isDeviceLowSpec(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);

        return ActivityManagerCompat.isLowRamDevice(activityManager) || memInfo.totalMem < 1000000000L;
    }
}
