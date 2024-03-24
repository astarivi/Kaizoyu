package com.astarivi.kaizoyu.core.storage.database.data.seen;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.astarivi.kaizoyu.core.models.local.LocalEpisode;
import com.astarivi.kaizoyu.core.storage.database.data.embedded.EmbeddedEpisode;

import java.util.Date;


@Entity(
        tableName = "seen_episode",
        foreignKeys = @ForeignKey(
                entity = SeenAnime.class,
                parentColumns = "id",
                childColumns = "animeId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(
                value = "animeId"
        )
)
public class SeenEpisode {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    @Embedded public EmbeddedEpisode episode;
    public int animeId;
    public long date;
    public boolean notified = false;

    public SeenEpisode() {
    }

    public SeenEpisode(EmbeddedEpisode episode, long creationTimestamp) {
        this.episode = episode;
        date = creationTimestamp;
    }

    public LocalEpisode toLocalEpisode() {
        return new LocalEpisode(
                episode.parentKitsuId,
                episode.episodeNumber,
                episode.length,
                episode.currentPosition,
                new Date(date)
        );
    }
}
