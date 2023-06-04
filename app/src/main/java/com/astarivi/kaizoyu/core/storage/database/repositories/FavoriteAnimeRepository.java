package com.astarivi.kaizoyu.core.storage.database.repositories;

import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnime;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;


public class FavoriteAnimeRepository {
    private final FavoriteAnimeDao animeDao;

    public FavoriteAnimeRepository(@NotNull AppDatabase database) {
        animeDao = database.favoriteAnimeDao();
    }

    public FavoriteAnimeDao getAnimeDao() {
        return animeDao;
    }

    public void createFromRelated(@NotNull SeenAnime seenAnime, long timestamp) {
        if (seenAnime.id == 0) return;

        seenAnime.favoriteId = (int) animeDao.insert(
                new FavoriteAnime(
                        timestamp
                )
        );

        Data.getRepositories()
                .getSeenAnimeRepository()
                .getAnimeDao()
                .update(seenAnime);

        Data.getTemporarySwitches().setPendingFavoritesRefresh(true);
    }

    public void deleteFromRelated(@NotNull SeenAnime seenAnime) {
        if (!seenAnime.isFavorite()) return;

        animeDao.delete(seenAnime.favoriteId);

        seenAnime.favoriteId = null;

        Data.getTemporarySwitches().setPendingFavoritesRefresh(true);
    }
}
