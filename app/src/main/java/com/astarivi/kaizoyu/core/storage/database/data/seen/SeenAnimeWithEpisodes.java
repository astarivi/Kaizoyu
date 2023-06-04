package com.astarivi.kaizoyu.core.storage.database.data.seen;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;


public class SeenAnimeWithEpisodes {
    @Embedded public SeenAnime anime;
    @Relation(
            parentColumn = "id",
            entityColumn = "animeId"
    )
    public List<SeenEpisode> episodes;
}
