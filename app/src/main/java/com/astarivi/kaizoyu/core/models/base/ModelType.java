package com.astarivi.kaizoyu.core.models.base;

public class ModelType {
    public enum Anime {
        BASE,
        SEASONAL,
        LOCAL
    }

    public enum Episode {
        BASE,
        LOCAL
    }

    public enum LocalAnime {
        SEEN,
        FAVORITE
    }

    public enum LocalEpisode {
        FAVORITE
    }
}