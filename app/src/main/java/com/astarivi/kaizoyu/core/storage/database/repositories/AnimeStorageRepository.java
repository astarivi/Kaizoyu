package com.astarivi.kaizoyu.core.storage.database.repositories;

import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnime;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeWithSeenAnime;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import lombok.Getter;


@Getter
public class AnimeStorageRepository {
    private final FavoriteAnimeDao favoriteAnimeDao;

    public AnimeStorageRepository(@NotNull AppDatabase database) {
        favoriteAnimeDao = database.favoriteAnimeDao();
    }

    @ThreadedOnly
    public ModelType.LocalAnime getLocalType(Anime anime) {
        // Get the LocalAnime type of this Anime, if available
        FavoriteAnimeWithSeenAnime favoriteAnimeWithSeenAnime = getFavoriteAnimeWithSeenAnime(anime);

        if (favoriteAnimeWithSeenAnime == null) return null;

        return ModelType.LocalAnime.getLocalAnime(favoriteAnimeWithSeenAnime.favoriteAnime.type);
    }

    @ThreadedOnly
    public @Nullable LocalAnime get(Anime anime) {
        // Get the LocalAnime equivalent of this Anime, if available
        FavoriteAnimeWithSeenAnime favoriteAnimeWithSeenAnime = getFavoriteAnimeWithSeenAnime(anime);

        if (favoriteAnimeWithSeenAnime == null) return null;

        return favoriteAnimeWithSeenAnime.toLocalAnime();
    }

    @ThreadedOnly
    public @Nullable FavoriteAnimeWithSeenAnime getFavoriteAnimeWithSeenAnime(Anime anime) {
        SeenAnime seenAnime = Data.getRepositories().getSeenAnimeRepository().get(anime);

        if (seenAnime == null || !seenAnime.isRelated()) return null;

        return favoriteAnimeDao.getRelation(seenAnime.favoriteId);
    }

    public void asyncCreateOrUpdate(Anime anime, ModelType.LocalAnime type, Callback callback) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            try {
                createOrUpdate(anime, type);
            } catch(Exception e) {
                Logger.error("Error saving anime with ID {} to database", anime.getAniListAnime().id);
                Logger.error(e);
                AnalyticsClient.onError(
                        "database_save_anime",
                        "There was an error while saving anime to the database",
                        e
                );
                Threading.runOnUiThread(callback::onFailure);
                return;
            }

            Threading.runOnUiThread(callback::onFinished);
        });
    }

    @ThreadedOnly
    public void createOrUpdate(Anime anime, ModelType.LocalAnime type) {
        if (type == ModelType.LocalAnime.SEEN)
            throw new IllegalArgumentException("LocalAnime.SEEN is not a valid type to save as.");

        long creationDate = System.currentTimeMillis();

        SeenAnimeRepository seenAnimeRepo = Data.getRepositories().getSeenAnimeRepository();

        // Check if seenAnime already exists.
        SeenAnime seenAnime = seenAnimeRepo.getOrCreate(anime);

        if (seenAnime.id == 0)
            throw new IllegalStateException("ID of saved SeenAnime was 0. Is there something wrong with the database?");

        if (seenAnime.isRelated()) {
            favoriteAnimeDao.delete(seenAnime.favoriteId);
        }

        seenAnime.favoriteId = (int) favoriteAnimeDao.insert(
                new FavoriteAnime(
                        creationDate,
                        type
                )
        );

        seenAnimeRepo.update(seenAnime);

        Data.getTemporarySwitches().setPendingFavoritesRefresh(true);
    }

    public void asyncDelete(Anime anime, Callback callback) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            try {
                delete(anime);
            } catch(Exception e) {
                Logger.error("Error removing anime with ID {} from database", anime.getAniListAnime().id);
                Logger.error(e);
                AnalyticsClient.onError(
                        "database_save_anime",
                        "There was an error while deleting anime from the database",
                        e
                );
                Threading.runOnUiThread(callback::onFailure);
                return;
            }

            Threading.runOnUiThread(callback::onFinished);
        });
    }

    @ThreadedOnly
    public void delete(Anime anime) {
        SeenAnimeRepository seenAnimeRepo = Data.getRepositories().getSeenAnimeRepository();

        // Check if seenAnime already exists.
        SeenAnime seenAnime = seenAnimeRepo.get(anime);

        // Nothing to do, this anime doesn't exist in storage.
        if (seenAnime == null) return;

        if (seenAnime.isRelated()) {
            favoriteAnimeDao.delete(seenAnime.favoriteId);
        }

        seenAnimeRepo.delete(seenAnime);

        Data.getTemporarySwitches().setPendingFavoritesRefresh(true);
    }

    public interface Callback {
        void onFinished();
        void onFailure();
    }
}
