package com.astarivi.kaizoyu.core.storage.database.tables.saved_anime;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.astarivi.kaizoyu.core.models.anime.LocalAnime;

import org.jetbrains.annotations.Contract;


@Entity(
        tableName = "saved_anime",
        indices = {
                @Index(
                        value = "kitsuId",
                        unique = true
                ),
                @Index(
                        value = "list"
                )
        }
)
public class SavedAnime {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    @Embedded
    public LocalAnime anime;
    public long updateDate;
    public int list;

    public SavedAnime() {
    }

    @Ignore
    @Contract(pure = true)
    public SavedAnime(@NonNull LocalAnime anime, long updateDate) {
        this.id = anime.getDbId();
        this.anime = anime;
        this.list = anime.getLocalList().getValue();
        this.updateDate = updateDate;
    }
}
