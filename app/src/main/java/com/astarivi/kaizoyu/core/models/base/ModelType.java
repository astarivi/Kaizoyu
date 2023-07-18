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
        SEEN(0),
        FAVORITE(1),
        WATCHED(2),
        PENDING(3);
        private final int value;

        LocalAnime(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static LocalAnime getLocalAnime(int value) {
            return LocalAnime.values()[value];
        }
    }

    public enum LocalEpisode {
        FAVORITE
    }
}