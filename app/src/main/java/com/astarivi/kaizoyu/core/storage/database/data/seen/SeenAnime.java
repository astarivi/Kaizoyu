package com.astarivi.kaizoyu.core.storage.database.data.seen;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.storage.database.data.embedded.EmbeddedAnime;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnime;

import java.util.Date;


@Entity(
        tableName = "seen_anime",
        indices = {
                @Index(
                        value = "kitsuId",
                        unique = true
                ),
                @Index(
                        value = "favoriteId",
                        unique = true
                )
        },
        foreignKeys = @ForeignKey(
                entity = FavoriteAnime.class,
                parentColumns = "id",
                childColumns = "favoriteId",
                onDelete = ForeignKey.SET_NULL
        )
)
public class SeenAnime {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    @Embedded
    public EmbeddedAnime anime;
    public long date;
    // Relations
    public Integer favoriteId;

    public SeenAnime() {
    }

    public SeenAnime(EmbeddedAnime anime, long creationTimestamp) {
        this.anime = anime;
        this.date = creationTimestamp;
    }

    public LocalAnime toLocalAnime(ModelType.LocalAnime type) {
        return new LocalAnime(
                anime.toKitsuAnime(),
                id,
                new Date(date),
                type
        );
    }

    public boolean isRelated() {
        return favoriteId != null;
    }
}
