package com.astarivi.kaizoyu.video.utils;

import android.os.Build;
import android.os.Bundle;

import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.Result;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BundleUtils {
    @SuppressWarnings("deprecation")
    public static @Nullable Result getResultFromBundle(@NotNull Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable("result", Result.class);
        }
        return bundle.getParcelable("result");
    }

    @SuppressWarnings("deprecation")
    public static @Nullable Episode getEpisodeFromBundle(@NotNull Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable("episode", Episode.class);
        }
        return bundle.getParcelable("episode");
    }
}
