package com.astarivi.kaizoyu.core.storage.database.tables.search_history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SearchHistory searchHistory);
    @Insert
    long[] insertAll(SearchHistory... anime);
    @Insert
    long[] insertAll(List<SearchHistory> anime);
    @Update
    void update(SearchHistory searchHistory);
    @Update
    void updateAll(SearchHistory... searchHistory);
    @Update
    void updateAll(List<SearchHistory> searchHistory);
    @Delete
    void delete(SearchHistory searchHistory);
    @Query("DELETE FROM search_history")
    void deleteAll();
    @Query("DELETE FROM search_history WHERE id=:id")
    void delete(int id);
    @Query("SELECT * FROM search_history ORDER BY date DESC")
    List<SearchHistory> getAll();
}
