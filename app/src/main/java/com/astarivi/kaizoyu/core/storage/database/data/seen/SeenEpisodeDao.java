package com.astarivi.kaizoyu.core.storage.database.data.seen;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface SeenEpisodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SeenEpisode episode);
    @Insert
    long[] insertAll(SeenEpisode... episode);
    @Update
    void update(SeenEpisode episode);
    @Update
    void updateAll(SeenEpisode... episode);
    @Delete
    void delete(SeenEpisode episode);
    @Query("SELECT * FROM seen_episode")
    List<SeenEpisode> getAll();
    @Query("SELECT * FROM seen_episode WHERE id=:id")
    SeenEpisode get(int id);
}
