package com.astarivi.kaizoyu.core.storage.database.repositories;

import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeWithEpisodes;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisode;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisodeDao;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import lombok.Getter;


@Getter
public class SeenAnimeRepository {
    private final SeenAnimeDao seenAnimeDao;
    private final SeenEpisodeDao seenEpisodeDao;

    public SeenAnimeRepository(@NotNull AppDatabase database) {
        seenAnimeDao = database.seenAnimeDao();
        seenEpisodeDao = database.seenEpisodeDao();
    }

    public void saveSeenEpisodeAsync(Anime anime, Episode episode, int currentPlayerTime) {
        Threading.submitTask(Threading.TASK.DATABASE, () ->
                this.saveSeenEpisode(currentPlayerTime, anime, episode)
        );
    }

    // Callback runs inside an "INSTANT" thread.
    public void saveSeenEpisodeAsync(Anime anime, Episode episode, int currentPlayerTime, Runnable callback) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            try {
                this.saveSeenEpisode(currentPlayerTime, anime, episode);
            } catch(Exception e) {
                Logger.error("Error while saving seen episode async");
                Logger.error(e);
            }
            if (callback != null) Threading.submitTask(Threading.TASK.INSTANT, callback);
        });
    }

    @ThreadedOnly
    private void saveSeenEpisode(int currentPlayerTime, Anime anime, Episode episode) {
        final boolean isAutoFavorite = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("auto_favorite", false);
        final boolean isAutoMove = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("auto_move", false);

        SeenAnime parentAnime;
        if (isAutoFavorite) {
            parentAnime = getOrCreate(anime);
        } else {
            parentAnime = get(anime);
        }

        if (parentAnime == null) {
            return;
        }

        // Auto-Favorite
        if (
                (isAutoFavorite && !parentAnime.isRelated()) ||
                (isAutoMove && parentAnime.isRelated())
        ) {
            Data.getRepositories()
                    .getAnimeStorageRepository()
                    .createOrUpdate(anime, ModelType.LocalAnime.FAVORITE);
        }

        // If the episode already exists, lets update it instead.
        SeenAnimeWithEpisodes seenAnimeWithEpisodes = seenAnimeDao.getRelation(parentAnime.id);

        for (SeenEpisode seenEpisode : seenAnimeWithEpisodes.episodes) {
            if (seenEpisode.episode.episodeNumber == episode.getKitsuEpisode().attributes.number) {
                seenEpisode.episode.currentPosition = currentPlayerTime;
                seenEpisodeDao.update(seenEpisode);

                Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);
                return;
            }
        }

        SeenEpisode seenEpisode = new SeenEpisode(
                episode.toEmbeddedDatabaseObject(currentPlayerTime),
                System.currentTimeMillis()
        );

        createRelation(parentAnime, seenEpisode);

        Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);
    }

    public void deleteSeenEpisodeAsync(Anime anime, Episode episode, Runnable callback) {
        deleteSeenEpisodeAsync(callback, anime, episode);
    }

    private void deleteSeenEpisodeAsync(Runnable runnable, Anime anime, Episode episode) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
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

    public @Nullable SeenAnime get(Anime anime) {
        return seenAnimeDao.getFromKitsuId(
                Integer.parseInt(anime.getKitsuAnime().id)
        );
    }

    public @NotNull SeenAnime create(Anime anime) {
        SeenAnime seenAnime = new SeenAnime(
                anime.toEmbeddedDatabaseObject(),
                System.currentTimeMillis()
        );

        seenAnime.id = (int) seenAnimeDao.insert(
                seenAnime
        );

        return seenAnime;
    }

    public @NotNull SeenAnime getOrCreate(Anime anime) {
        SeenAnime seenAnime = get(anime);

        // seenAnime doesn't exist, let's create it.
        if (seenAnime == null) {
            seenAnime = create(anime);
        }

        return seenAnime;
    }

    public void update(SeenAnime seenAnime) {
        seenAnimeDao.update(seenAnime);
    }

    public void delete(SeenAnime seenAnime) {
        if (seenAnime.id == 0)
            throw new IllegalArgumentException("SeenAnime Id cannot be 0, can't delete if it doesn't exist");

        SeenAnimeWithEpisodes seenAnimeWithEpisodes = seenAnimeDao.getRelation(seenAnime.id);

        if (seenAnimeWithEpisodes == null) {
            seenAnimeDao.delete(seenAnime);
            seenAnime.id = 0;
            return;
        }

        for (SeenEpisode seenEpisode : seenAnimeWithEpisodes.episodes) {
            seenEpisodeDao.delete(seenEpisode);
        }

        seenAnimeDao.delete(seenAnime);
        seenAnime.id = 0;
    }
}
