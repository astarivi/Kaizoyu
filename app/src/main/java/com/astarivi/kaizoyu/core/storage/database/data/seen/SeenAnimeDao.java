package com.astarivi.kaizoyu.core.storage.database.data.seen;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;


@Dao
public interface SeenAnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SeenAnime anime);
    @Insert
    long[] insertAll(SeenAnime... anime);
    @Update
    void update(SeenAnime anime);
    @Update
    void updateAll(SeenAnime... anime);
    @Delete
    void delete(SeenAnime anime);
    @Query("DELETE FROM seen_anime WHERE id=:id")
    void delete(int id);
    @Query("SELECT id FROM seen_anime WHERE kitsuId=:id")
    long animeExists(int id);
    @Query("SELECT * FROM seen_anime WHERE kitsuId=:id")
    SeenAnime getFromKitsuId(int id);
    @Query("SELECT * FROM seen_anime ORDER BY date DESC")
    List<SeenAnime> getAll();
    @Query("SELECT * FROM seen_anime ORDER BY date DESC LIMIT :limit")
    List<SeenAnime> getPartial(int limit);
    @Query("SELECT * FROM seen_anime WHERE id=:id")
    SeenAnime get(long id);
    @Transaction
    @Query("SELECT * FROM seen_anime ORDER BY date DESC")
    List<SeenAnimeWithEpisodes> getRelation();
    @Transaction
    @Query("SELECT * FROM seen_anime WHERE id=:id")
    SeenAnimeWithEpisodes getRelation(int id);
    @Transaction
    @Query("SELECT * FROM seen_anime WHERE kitsuId=:id")
    SeenAnimeWithEpisodes getRelationFromKitsuId(int id);
}