package com.astarivi.kaizoyu.core.storage.database.data.favorite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;


@Dao
public interface FavoriteAnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FavoriteAnime anime);
    @Insert
    long[] insertAll(FavoriteAnime... anime);
    @Update
    void update(FavoriteAnime anime);
    @Update
    void updateAll(FavoriteAnime... anime);
    @Delete
    void delete(FavoriteAnime anime);
    @Query("DELETE FROM favorite_anime WHERE id=:id")
    void delete(int id);
    @Query("SELECT * FROM favorite_anime ORDER BY date DESC")
    List<FavoriteAnime> getAll();
    @Query("SELECT * FROM favorite_anime ORDER BY date DESC LIMIT :limit")
    List<FavoriteAnime> getPartial(int limit);
    @Query("SELECT * FROM favorite_anime WHERE id=:id")
    FavoriteAnime get(long id);
    @Transaction
    @Query("SELECT * FROM favorite_anime ORDER BY date DESC")
    List<FavoriteAnimeWithSeenAnime> getRelation();
    @Transaction
    @Query("SELECT * FROM favorite_anime WHERE id=:id")
    FavoriteAnimeWithSeenAnime getRelation(int id);
    @Transaction
    @Query("SELECT * FROM favorite_anime ORDER BY date DESC LIMIT :limit")
    List<FavoriteAnimeWithSeenAnime> getRelationPartial(int limit);
}
