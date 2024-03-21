package com.astarivi.kaizolib.anilist.model;

import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AniListAnime {
    public long id;
    public Long idMal;
    public String description;
    public Titles title;
    public Integer averageScore;
    public FuzzyDate startDate;
    @JsonProperty("format")
    public String subtype;
    public String status;
    public CoverImage coverImage;
    public String bannerImage;
    public String siteUrl;
    public Integer episodes;
    public Integer duration;
    public List<String> genres;
    public Trailer trailer;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Titles {
        public String romaji;
        public String english;
        @JsonProperty("native")
        public String japanese;
        public String userPreferred;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FuzzyDate {
        public Integer year;
        public Integer month;
        public Integer day;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CoverImage {
        public String extraLarge;
        public String large;
        public String medium;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trailer {
        public String id;
        public String site;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Response {
        public Data data;

        private static class Data {
            @JsonProperty("Media")
            public AniListAnime Media;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ResponseMany {
        public DataMany data;

        private static class DataMany {
            public Page Page;
        }

        private static class Page {
            public List<AniListAnime> media;
        }
    }

    public static AniListAnime deserializeOne(String serialized) throws ParsingError {
        try {
            return JsonMapper.deserializeGeneric(serialized, Response.class).data.Media;
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }

    public static List<AniListAnime> deserializeMany(String serialized) throws ParsingError {
        try {
            return JsonMapper.deserializeGeneric(serialized, ResponseMany.class).data.Page.media;
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }
}
