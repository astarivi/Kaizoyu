package com.astarivi.kaizoyu.core.storage.database.repositories;

import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.base.AnimeBase;
import com.astarivi.kaizoyu.core.models.local.LocalEpisode;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeWithEpisodes;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisode;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisodeDao;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.Date;


public class SeenAnimeRepository {
    private final SeenAnimeDao seenAnimeDao;
    private final SeenEpisodeDao seenEpisodeDao;

    public SeenAnimeRepository(@NotNull AppDatabase database) {
        seenAnimeDao = database.seenAnimeDao();
        seenEpisodeDao = database.seenEpisodeDao();
    }

    public SeenAnimeDao getAnimeDao() {
        return seenAnimeDao;
    }

    public SeenEpisodeDao getEpisodeDao() {
        return seenEpisodeDao;
    }

    public void saveSeenEpisodeAsync(AnimeBase anime, Episode episode, int currentPlayerTime) {
        this.saveSeenEpisodeAsync(currentPlayerTime, anime, episode, null);
    }

    // Callback runs inside an "INSTANT" thread.
    public void saveSeenEpisodeAsync(AnimeBase anime, Episode episode, int currentPlayerTime, Runnable callback) {
        this.saveSeenEpisodeAsync(currentPlayerTime, anime, episode, callback);
    }

    private void saveSeenEpisodeAsync(int currentPlayerTime, AnimeBase anime, Episode episode, Runnable callback) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            if (KaizoyuApplication.application == null) return;

            long timestamp = System.currentTimeMillis();
            final int animeId = Integer.parseInt(anime.getKitsuAnime().id);
            boolean shouldCheckExisting = true;

            final SeenAnimeRepository seenAnimeRepository = Data.getRepositories().getSeenAnimeRepository();

            SeenAnimeDao seenAnimeDao = seenAnimeRepository.getAnimeDao();
            SeenEpisodeDao seenEpisodeDao = seenAnimeRepository.getEpisodeDao();

            SeenAnime parentAnime = seenAnimeDao.getFromKitsuId(
                    animeId
            );

            if (parentAnime == null) {
                parentAnime = new SeenAnime(
                        anime.toEmbeddedDatabaseObject(),
                        timestamp
                );

                parentAnime.id = (int) seenAnimeDao.insert(
                        parentAnime
                );

                shouldCheckExisting = false;
            }

            final boolean isAutoFavorite = Boolean.parseBoolean(
                    Data.getProperties(Data.CONFIGURATION.APP)
                            .getProperty("auto_favorite", "false")
            );

            // Auto-Favorite
            if (isAutoFavorite && !parentAnime.isFavorite()) {
                Data.getRepositories()
                        .getFavoriteAnimeRepository()
                        .createFromRelated(parentAnime, timestamp);
            }

            // If the episode already exists, lets update it instead.
            if (shouldCheckExisting) {
                SeenAnimeWithEpisodes seenAnimeWithEpisodes = seenAnimeDao.getRelationFromKitsuId(
                        animeId
                );

                for (SeenEpisode seenEpisode : seenAnimeWithEpisodes.episodes) {
                    if (seenEpisode.episode.episodeNumber == episode.getKitsuEpisode().attributes.number) {
                        // Only store times that are after the original stored time:
                        // If user wants to re-watch some part of the episode
                        // CURRENTLY DISABLED
//                        if (currentPlayerTime < seenEpisode.episode.currentPosition) {
//                            return;
//                        }

                        seenEpisode.episode.currentPosition = currentPlayerTime;
                        seenEpisodeDao.update(seenEpisode);

                        Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);

                        if (callback != null) Threading.submitTask(Threading.TASK.INSTANT, callback);
                        return;
                    }
                }
            }

            // Episode doesn't exist, lets create it
            LocalEpisode localEpisode = new LocalEpisode(
                    episode.getKitsuEpisode(),
                    animeId,
                    currentPlayerTime,
                    new Date()
            );

            SeenEpisode seenEpisode = new SeenEpisode(
                    localEpisode.toEmbeddedDatabaseObject(),
                    System.currentTimeMillis()
            );

            seenAnimeRepository.createRelation(parentAnime, seenEpisode);

            Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);

            if (callback != null) Threading.submitTask(Threading.TASK.INSTANT, callback);
        });
    }

    public void deleteSeenEpisodeAsync(Anime anime, Episode episode) {
        deleteSeenEpisodeAsync(null, anime, episode);
    }

    public void deleteSeenEpisodeAsync(Anime anime, Episode episode, Runnable callback) {
        deleteSeenEpisodeAsync(callback, anime, episode);
    }

    private void deleteSeenEpisodeAsync(Runnable runnable, Anime anime, Episode episode) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            if (KaizoyuApplication.application == null) return;

            final int animeId = Integer.parseInt(anime.getKitsuAnime().id);

            SeenAnimeWithEpisodes seenAnimeWithEpisodes = seenAnimeDao.getRelationFromKitsuId(
                    animeId
            );

            if (seenAnimeWithEpisodes == null) return;

            for (SeenEpisode seenEpisode : seenAnimeWithEpisodes.episodes) {
                if (seenEpisode.episode.episodeNumber == episode.getKitsuEpisode().attributes.number) {
                    seenEpisodeDao.delete(seenEpisode);
                }
            }

            Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);
            if (runnable != null) Threading.submitTask(Threading.TASK.INSTANT, runnable);
        });
    }

    public void createRelation(@NotNull SeenAnime parent, @NotNull SeenEpisode children) {
        if (parent.id == 0) return;

        children.animeId = parent.id;

        seenEpisodeDao.insert(
                children
        );
    }
}
