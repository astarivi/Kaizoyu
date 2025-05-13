package com.astarivi.kaizoyu.core.storage.database;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.astarivi.kaizoyu.core.storage.database.tables.id_overlays.IdOverlays;
import com.astarivi.kaizoyu.core.storage.database.tables.id_overlays.IdOverlaysDao;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisode;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisodeDao;
import com.astarivi.kaizoyu.core.storage.database.tables.search_history.SearchHistory;
import com.astarivi.kaizoyu.core.storage.database.tables.search_history.SearchHistoryDao;


@Database(
        version = 4,
        entities = {
                IdOverlays.class,
                SavedAnime.class,
                SavedEpisode.class,
                SearchHistory.class
        },
        autoMigrations = {
                @AutoMigration(from = 3, to = 4)
        }
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract IdOverlaysDao idOverlaysDao();
    public abstract SearchHistoryDao searchHistoryDao();
    public abstract SavedEpisodeDao savedEpisodeDao();
    public abstract SavedAnimeDao savedAnimeDao();
}
