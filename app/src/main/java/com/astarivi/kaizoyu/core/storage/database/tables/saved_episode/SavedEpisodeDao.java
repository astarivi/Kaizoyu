package com.astarivi.kaizoyu.core.storage.database.tables.saved_episode;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SavedEpisodeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(SavedEpisode episode);
    @Insert
    long[] insertAll(SavedEpisode... episode);
    @Update
    void update(SavedEpisode episode);
    @Update
    void updateAll(SavedEpisode... episode);
    @Delete
    void delete(SavedEpisode episode);
    @Query("SELECT * FROM saved_episode")
    List<SavedEpisode> getAll();
    @Query("SELECT * FROM saved_episode WHERE id=:id")
    SavedEpisode get(int id);
    @Query("SELECT * FROM saved_episode WHERE kitsuId=:id")
    SavedEpisode getByOwnKitsuId(long id);
    @Query("SELECT * FROM saved_episode WHERE animeKitsuId=:animeKitsuId")
    SavedEpisode getEpisodeByAnimeKitsuId(long animeKitsuId);
    @Query("SELECT * FROM saved_episode WHERE kitsuId=:ownKitsuId")
    SavedEpisode getEpisodeByOwnKitsuId(long ownKitsuId);
    @Query("SELECT * FROM saved_episode WHERE animeKitsuId=:animeKitsuId AND number=:episodeNumber")
    SavedEpisode getEpisodeWith(int animeKitsuId, int episodeNumber);
    @Query("DELETE FROM saved_episode WHERE kitsuId=:id")
    void deleteByOwnKitsuId(long id);

    @Query("SELECT EXISTS(SELECT 1 FROM saved_episode WHERE kitsuId=:id)")
    Boolean existsByKitsuId(long id);
}