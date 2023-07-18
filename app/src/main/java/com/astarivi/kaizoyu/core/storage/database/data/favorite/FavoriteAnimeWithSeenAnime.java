package com.astarivi.kaizoyu.core.storage.database.data.favorite;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnime;


public class FavoriteAnimeWithSeenAnime {
    @Embedded
    public FavoriteAnime favoriteAnime;
    @Relation(
            parentColumn = "id",
            entityColumn = "favoriteId"
    )
    public SeenAnime seenAnime;

    public LocalAnime toLocalAnime() {
        return seenAnime.toLocalAnime(
                ModelType.LocalAnime.getLocalAnime(favoriteAnime.type)
        );
    }
}
