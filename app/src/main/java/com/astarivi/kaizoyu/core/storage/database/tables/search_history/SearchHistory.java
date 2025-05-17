package com.astarivi.kaizoyu.core.storage.database.tables.search_history;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(
        tableName = "search_history"
)
public class SearchHistory {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    public String searchTerm;
    public long date;

    public SearchHistory() {
    }

    public SearchHistory(String searchTerm, long creationTimestamp) {
        this.searchTerm = searchTerm;
        this.date = creationTimestamp;
    }
}
