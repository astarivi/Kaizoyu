package com.astarivi.kaizolib.anilist.model;

import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Locale;


@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class AniListAnime {
    public Long id;
    public Long idMal;
    public String description;
    public Titles title;
    public Integer averageScore;
    public FuzzyDate startDate;
    @JsonProperty("format")
    public String subtype;
    public String type;
    public String status;
    public CoverImage coverImage;
    public String bannerImage;
    public String siteUrl;
    public Integer episodes;
    public Integer duration;
    public List<String> genres;
    public Trailer trailer;

    public AniListAnime() {

    }

    @JsonProperty("description")
    public void setDescription(String value) {
        if (value == null) {
            description = null;
            return;
        }

        description = Jsoup.parse(value).text();
    }

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

        @JsonIgnore
        public @Nullable String getDateAsQuarters() {
            if (year == null || month == null) return null;

            LocalDate date = LocalDate.of(year, month, 1);
            int quarter = date.get(IsoFields.QUARTER_OF_YEAR);
            return String.format(Locale.getDefault(), "%d Q%d", date.getYear(), quarter);
        }

        @JsonIgnore
        public @Nullable String getDate() {
            if (year == null) {
                return null;
            }

            StringBuilder dateString = new StringBuilder();

            if (day != null) {
                dateString.append(String.format(Locale.UK, "%02d", day)).append("/");
            }

            if (month != null) {
                dateString.append(String.format(Locale.UK, "%02d", month)).append("/");
            }

            dateString.append(year);

            return dateString.toString();
        }
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

    public static AniListAnime deserializeOne(String serialized) throws ParsingError {
        try {
            return JsonMapper.deserializeGeneric(serialized, Response.class).data.Media;
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }

    public static AniListAnime withDefaults(long id) {
        AniListAnime anime = new AniListAnime();

        anime.id = id;
        anime.title = new Titles();
        anime.coverImage = new CoverImage();
        anime.trailer = new Trailer();
        anime.startDate = new AniListAnime.FuzzyDate();

        return anime;
    }
}
