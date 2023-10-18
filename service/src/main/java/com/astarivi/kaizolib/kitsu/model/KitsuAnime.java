package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuAnime {
    public String id;
    public String type;
    public KitsuAnimeAttributes attributes;
    public KitsuRelationships relationships;

    public KitsuAnime() {

    }

    @JsonIgnore
    private KitsuAnime(@NotNull KitsuAnimeBuilder builder) {
        id = builder.id;
        attributes = builder.kitsuAnimeAttributes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuAnimeAttributes {
        public String createdAt;
        public String updatedAt;
        public String slug;
        public String synopsis;
        public String description;
        // Nullable
        public Integer coverImageTopOffset;
        public KitsuAnimeTitles titles;
        public String canonicalTitle;
        public List<String> abbreviatedTitles;
        public String averageRating;
        public long userCount;
        public long favoritesCount;
        public String startDate;
        public String endDate;
        public String nextRelease;
        public long popularityRank;
        public long ratingRank;
        public String ageRating;
        public String ageRatingGuide;
        public String subtype;
        public String status;
        public String tba;
        public KitsuAnimeImages posterImage;
        public KitsuAnimeImages coverImage;
        public int episodeCount;
        public int episodeLength;
        // Just in case One Piece ever overflows an int...
        public long totalLength;
        public String youtubeVideoId;
        public String showType;
        public String nsfw;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuAnimeTitles {
        public String en;
        public String en_jp;
        public String en_us;
        public String ja_jp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuAnimeImages {
        public String tiny;
        public String small;
        public String medium;
        public String large;
        public String original;
        public KitsuAnimeMeta meta;
    }

    public static class KitsuAnimeMeta {
        public KitsuAnimeDimensions dimensions;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KitsuAnimeDimensions {
            public KitsuAnimeDimension tiny;
            public KitsuAnimeDimension small;
            public KitsuAnimeDimension medium;
            public KitsuAnimeDimension large;
        }

        public static class KitsuAnimeDimension {
            public int width;
            public int height;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuRelationships {
        public KitsuRelationship genres;
        public KitsuRelationship categories;
        public KitsuRelationship castings;
        public KitsuRelationship reviews;
        public KitsuRelationship mediaRelationships;
        public KitsuRelationship characters;
        public KitsuRelationship staff;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KitsuRelationship {
            public KitsuRelation links;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KitsuRelation {
            public String self;
            public String related;
        }
    }

    public static class KitsuAnimeBuilder {
        private final String id;
        private final KitsuAnimeAttributes kitsuAnimeAttributes = new KitsuAnimeAttributes();

        public KitsuAnimeBuilder(@NotNull String kitsuId) {
            id = kitsuId;
            kitsuAnimeAttributes.titles = new KitsuAnimeTitles();
            kitsuAnimeAttributes.coverImage = new KitsuAnimeImages();
            kitsuAnimeAttributes.posterImage = new KitsuAnimeImages();
            kitsuAnimeAttributes.userCount = 0;
            kitsuAnimeAttributes.favoritesCount = 0;
            kitsuAnimeAttributes.popularityRank = 0;
            kitsuAnimeAttributes.ratingRank = 0;
            kitsuAnimeAttributes.episodeCount = 0;
            kitsuAnimeAttributes.episodeLength = 0;
            kitsuAnimeAttributes.totalLength = 0;
        }

        public KitsuAnimeBuilder setSubtype(String subtype) {
            kitsuAnimeAttributes.subtype = subtype;
            return this;
        }

        public KitsuAnimeBuilder setSynopsis(String synopsis) {
            kitsuAnimeAttributes.synopsis = synopsis;
            return this;
        }

        public KitsuAnimeBuilder setTitles(String titleJp, String titleEn, String titleEnJp) {
            kitsuAnimeAttributes.titles.ja_jp = titleJp;
            kitsuAnimeAttributes.titles.en = titleEn;
            kitsuAnimeAttributes.titles.en_jp = titleEnJp;
            return this;
        }

        public KitsuAnimeBuilder setCoverImage(String url) {
            kitsuAnimeAttributes.coverImage.tiny = url;
            return this;
        }

        public KitsuAnimeBuilder setPosterImage(String url) {
            kitsuAnimeAttributes.posterImage.tiny = url;
            return this;
        }

        public KitsuAnime build() {
            return new KitsuAnime(this);
        }
    }
}
