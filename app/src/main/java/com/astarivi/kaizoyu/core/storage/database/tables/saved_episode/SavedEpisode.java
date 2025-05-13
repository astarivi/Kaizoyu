package com.astarivi.kaizoyu.core.storage.database.tables.saved_episode;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.astarivi.kaizoyu.core.models.episode.LocalEpisode;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;

import org.jetbrains.annotations.Contract;


@Entity(
        tableName = "saved_episode",
        foreignKeys = @ForeignKey(
                entity = SavedAnime.class,
                parentColumns = "kitsuId",
                childColumns = "animeKitsuId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(
                        value = "animeKitsuId"
                )
        }
)
public class SavedEpisode {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    @Embedded
    public LocalEpisode episode;
    public long updateDate;

    public SavedEpisode() {
    }

    @Contract(pure = true)
    public SavedEpisode(LocalEpisode episode, long updateDate) {
        this.id = episode.dbId;
        this.episode = episode;
        this.updateDate = updateDate;
    }
}
