package com.astarivi.kaizoyu.video.utils;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.models.base.EpisodeBasicInfo;
import com.astarivi.kaizoyu.core.storage.database.repo.SavedShowRepo;
import com.astarivi.kaizoyu.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.Getter;


@Getter
public class AnimeEpisodeManager {
    private final AnimeBasicInfo anime;
    private final EpisodeBasicInfo episode;

    public AnimeEpisodeManager(AnimeBasicInfo anime, EpisodeBasicInfo episode) {
        this.anime = anime;
        this.episode = episode;
    }

    public AnimeEpisodeManager(@NonNull Bundle bundle) {
        final String type = bundle.getString("type");
        final String episodeTypeStr = bundle.getString("episode_type");

        if ((type == null || type.isEmpty()) || (episodeTypeStr == null || episodeTypeStr.isEmpty())) {
            throw new IllegalArgumentException("The type String in bundle cannot be empty");
        }

        this.anime = Utils.getAnimeFromBundle(
                bundle,
                AnimeBasicInfo.AnimeType.valueOf(type)
        );

        this.episode = BundleUtils.getEpisodeFromBundle(
                bundle,
                EpisodeBasicInfo.EpisodeType.valueOf(episodeTypeStr)
        );

        if (anime == null || episode == null) {
            throw new IllegalArgumentException("No anime and/or episode were given");
        }
    }

    public String getAnimeTitle() {
        return anime.getPreferredTitle();
    }

    public String getEpisodeTitle(@NotNull Context context) {
        return String.format(
                Locale.ENGLISH,
                "%s %d %s",
                context.getString(R.string.episode),
                episode.getNumber(),
                ""
        );
    }

    public void saveProgress(int playTime, int totalLength) {
        if (totalLength > 0 && episode.getLength() <= 0) {
            episode.setLength((int) TimeUnit.MILLISECONDS.toMinutes(totalLength));
        }

        SavedShowRepo.saveEpisodeAsync(anime, episode, playTime, null);
    }
}