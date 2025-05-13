package com.astarivi.kaizoyu.core.storage.database.tables.id_overlays;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface IdOverlaysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(IdOverlays IdOverlays);
    @Insert
    long[] insertAll(IdOverlays... anime);
    @Insert
    long[] insertAll(List<IdOverlays> anime);
    @Update
    void update(IdOverlays IdOverlays);
    @Update
    void updateAll(IdOverlays... IdOverlays);
    @Update
    void updateAll(List<IdOverlays> IdOverlays);
    @Delete
    void delete(IdOverlays IdOverlays);
    @Query("DELETE FROM id_overlays")
    void deleteAll();
    @Query("DELETE FROM id_overlays WHERE id=:id")
    void delete(int id);
    @Query("SELECT * FROM id_overlays WHERE id=:id")
    IdOverlays get(long id);
    @Query("SELECT * FROM id_overlays WHERE kitsuId=:id")
    IdOverlays getFromKitsu(long id);
    @Query("SELECT * FROM id_overlays WHERE aniId=:id")
    IdOverlays getFromAni(long id);
    @Query("SELECT * FROM id_overlays WHERE malId=:id")
    IdOverlays getFromMAL(long id);
}
