package com.astarivi.kaizoyu.core.storage.database.tables.saved_anime;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisode;

import java.util.List;


public class SavedAnimeWithEpisodes {
    @Embedded
    public SavedAnime anime;
    @Relation(
            parentColumn = "kitsuId",
            entityColumn = "animeKitsuId"
    )
    public List<SavedEpisode> episodes;
}
