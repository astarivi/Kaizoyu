package com.astarivi.kaizolib.kitsuv2.model;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuEpisode {
    public long id;
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
        private final long id;
        private final KitsuEpisodeAttributes attributes;

        public KitsuEpisodeBuilder(long id) {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SearchResults {
        public List<KitsuEpisode> data;
    }

    public static List<KitsuEpisode> deserializeSearch(String serialized) throws ParsingError {
        try {
            return JsonMapper.deserializeGeneric(serialized, KitsuEpisode.SearchResults.class).data;
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }
}
