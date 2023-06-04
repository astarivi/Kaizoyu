package com.astarivi.kaizoyu.core.storage.database.data.favorite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(
        tableName = "favorite_anime"
)
public class FavoriteAnime {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    public long date;

    public FavoriteAnime() {
    }

    public FavoriteAnime(long creationTimestamp) {
        this.date = creationTimestamp;
    }
}
