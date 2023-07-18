package com.astarivi.kaizoyu.core.storage.database.data.favorite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.astarivi.kaizoyu.core.models.base.ModelType;


@Entity(
        tableName = "favorite_anime"
)
public class FavoriteAnime {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    public long date;
    public int type = 1;

    public FavoriteAnime() {
    }

    public FavoriteAnime(long creationTimestamp, ModelType.LocalAnime favoriteType) {
        date = creationTimestamp;
        type = favoriteType.getValue();

        if (type == 0) throw new IllegalArgumentException("Cannot save favorite of type 'SEEN'");
    }
}
