package com.astarivi.kaizoyu.video.utils;

import android.content.Context;
import android.os.Bundle;

import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class AnimeEpisodeManager {
    private final Anime anime;
    private final Episode episode;

    public AnimeEpisodeManager(Anime anime, Episode episode) {
        this.anime = anime;
        this.episode = episode;
    }

    public String getAnimeTitle() {
        return anime.getDisplayTitle();
    }

    public String getEpisodeTitle(@NotNull Context context) {
        return String.format(
                Locale.ENGLISH,
                "%s %d %s",
                context.getString(R.string.episode),
                episode.getKitsuEpisode().attributes.number,
                episode.getKitsuEpisode().attributes.canonicalTitle != null ? episode.getKitsuEpisode().attributes.canonicalTitle : ""
        );
    }

    public void saveProgress(int playTime, int totalLength) {
        KitsuEpisode.KitsuEpisodeAttributes attributes = episode.getKitsuEpisode().attributes;
        if (totalLength > 0 && (attributes.length == null || attributes.length <= 0)) {
            attributes.length = (int) TimeUnit.MILLISECONDS.toMinutes(totalLength);
        }

        Data.getRepositories()
                .getSeenAnimeRepository()
                .saveSeenEpisodeAsync(anime, episode, playTime);
    }

    public static class Builder {
        private final Bundle bundle;

        public Builder(@NotNull Bundle from) {
            bundle = from;
        }

        public @NotNull AnimeEpisodeManager build() throws IllegalArgumentException {
            final String type = bundle.getString("type");

            if (type == null || type.equals("")) {
                throw new IllegalArgumentException("The type bundle cannot be empty");
            }

            ModelType.Anime animeType;

            animeType = ModelType.Anime.valueOf(type);

            Episode episode = BundleUtils.getEpisodeFromBundle(bundle);
            // Guaranteed to cast
            Anime anime = (Anime) Utils.getAnimeFromBundle(bundle, animeType);

            if (anime == null || episode == null) {
                throw new IllegalArgumentException("No anime and episode were given");
            }

            return new AnimeEpisodeManager(anime, episode);
        }
    }
}