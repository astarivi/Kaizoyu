package com.astarivi.kaizoyu.core.storage.database.tables.saved_anime;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;


@Dao
public interface SavedAnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SavedAnime anime);
    @Insert
    long[] insertAll(SavedAnime... anime);
    @Update
    void update(SavedAnime anime);
    @Update
    void updateAll(SavedAnime... anime);
    @Delete
    void delete(SavedAnime anime);
    @Query("DELETE FROM saved_anime WHERE id=:id")
    void delete(int id);
    @Query("DELETE FROM saved_anime WHERE kitsuId=:id")
    void deleteByKitsuId(long id);
    @Query("SELECT id FROM saved_anime WHERE kitsuId=:id")
    long animeExists(long id);
    @Query("SELECT * FROM saved_anime WHERE kitsuId=:id")
    SavedAnime getByKitsuId(long id);
    @Query("SELECT * FROM saved_anime ORDER BY updateDate DESC")
    List<SavedAnime> getAll();

    @Query("SELECT * FROM saved_anime WHERE list=:list ORDER BY updateDate DESC")
    List<SavedAnime> getAllByType(int list);
    @Query("SELECT * FROM saved_anime ORDER BY updateDate DESC LIMIT :limit")
    List<SavedAnime> getPartial(int limit);
    @Query("SELECT * FROM saved_anime WHERE id=:id")
    SavedAnime get(long id);
    @Transaction
    @Query("SELECT * FROM saved_anime ORDER BY updateDate DESC")
    List<SavedAnimeWithEpisodes> getRelation();
    @Transaction
    @Query("SELECT * FROM saved_anime WHERE id=:id")
    SavedAnimeWithEpisodes getRelation(int id);
    @Transaction
    @Query("SELECT * FROM saved_anime WHERE kitsuId=:id")
    SavedAnimeWithEpisodes getRelationFromKitsuId(long id);
    @Transaction
    @Query("SELECT * FROM saved_anime WHERE list=:list")
    List<SavedAnimeWithEpisodes> getRelationFromType(int list);
}
