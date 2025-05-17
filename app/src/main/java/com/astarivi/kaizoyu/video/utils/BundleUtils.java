package com.astarivi.kaizoyu.video.utils;

import android.os.Build;
import android.os.Bundle;

import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.models.base.EpisodeBasicInfo;
import com.astarivi.kaizoyu.core.models.episode.LocalEpisode;
import com.astarivi.kaizoyu.core.models.episode.RemoteEpisode;

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
    public static @Nullable EpisodeBasicInfo getEpisodeFromBundle(@NotNull Bundle bundle, @NotNull EpisodeBasicInfo.EpisodeType type) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable("episode");
        }

        return switch (type) {
            case REMOTE -> bundle.getParcelable("episode", RemoteEpisode.class);
            default -> bundle.getParcelable("episode", LocalEpisode.class);
        };
    }

    public enum PictureInPictureAction {
        PAUSE_OR_RESUME,
        REWIND_TEN,
        FORWARD_TEN
    }
}
