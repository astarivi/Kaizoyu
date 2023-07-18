package com.astarivi.kaizoyu.core.storage.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnime;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.search.SearchHistory;
import com.astarivi.kaizoyu.core.storage.database.data.search.SearchHistoryDao;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisode;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisodeDao;


@Database(
        entities = {
                FavoriteAnime.class,
                SeenAnime.class,
                SeenEpisode.class,
                SearchHistory.class
        },
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteAnimeDao favoriteAnimeDao();
    public abstract SearchHistoryDao searchHistoryDao();
    public abstract SeenAnimeDao seenAnimeDao();
    public abstract SeenEpisodeDao seenEpisodeDao();
}
