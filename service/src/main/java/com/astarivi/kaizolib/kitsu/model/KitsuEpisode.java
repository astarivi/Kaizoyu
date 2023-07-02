package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuEpisode {
    public String id;
    public String type;
    public KitsuEpisodeAttributes attributes;

    public KitsuEpisode() {

    }

    @JsonIgnore
    public KitsuEpisode(KitsuEpisodeBuilder builder) {
        this.id = builder.id;
        this.attributes = builder.attributes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuEpisodeAttributes {
        public String createdAt;
        public String updatedAt;
        public String synopsis;
        public String description;
        public KitsuEpisodeTitles titles;
        public String canonicalTitle;
        public Integer seasonNumber;
        public Integer number;
        public Integer relativeNumber;
        public String airdate;
        public Integer length;
        public KitsuEpisodeThumbnail thumbnail;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuEpisodeTitles {
        public String en;
        public String en_jp;
        public String en_us;
        public String ja_jp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuEpisodeThumbnail {
        public String original;
    }

    public static class KitsuEpisodeBuilder {
        private final String id;
        private final KitsuEpisodeAttributes attributes;

        public KitsuEpisodeBuilder(String id) {
            this.id = id;
            attributes = new KitsuEpisodeAttributes();
            attributes.titles = new KitsuEpisodeTitles();
        }

        public KitsuEpisodeBuilder setEpisodeNumber(int number) {
            attributes.number = number;
            return this;
        }

        public KitsuEpisodeBuilder setEpisodeSeasonNumber(int season) {
            attributes.seasonNumber = season;
            return this;
        }

        public KitsuEpisodeBuilder setRelativeNumber(int relativeNumber) {
            attributes.relativeNumber = relativeNumber;
            return this;
        }

        public @NotNull KitsuEpisode build() {
            return new KitsuEpisode(this);
        }
    }
}
