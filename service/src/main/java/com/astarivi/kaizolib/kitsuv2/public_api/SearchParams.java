package com.astarivi.kaizolib.kitsuv2.public_api;

import com.astarivi.kaizolib.common.util.StringPair;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;


public class SearchParams {
    private final List<StringPair> customParameters;
    private int limit = 20;
    private int offset = 0;
    private String animeTitle = null;
    private int seasonYear = 0;
    private SearchParams.Seasons season = null;
    private SearchParams.Status status = null;

    public SearchParams(){
        customParameters = new ArrayList<>();
    }

    public SearchParams setCustomParameter(@NotNull String key, @NotNull String value) {
        customParameters.add(
                new StringPair(key, value)
        );

        return this;
    }

    public SearchParams setTitle(String animeTitle) {
        animeTitle = animeTitle.replaceAll("\\[.*?]","")
                .replace("_", " ")
                .trim();

        this.animeTitle = animeTitle;
        return this;
    }

    public SearchParams setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public SearchParams setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public SearchParams setSeasonYear(int seasonYear) {
        this.seasonYear = seasonYear;
        return this;
    }

    public SearchParams setSeason(SearchParams.Seasons seasons) {
        this.season = seasons;
        return this;
    }

    public SearchParams setStatus(SearchParams.Status status) {
        this.status = status;
        return this;
    }

    public @NotNull HttpUrl buildURI() {
        HttpUrl.Builder queryUrl = new HttpUrl.Builder();
        queryUrl.scheme("https").host("kitsu.app").addPathSegments("api/edge/anime");

        queryUrl.addQueryParameter("page[limit]", Integer.toString(limit));
        queryUrl.addQueryParameter("page[offset]", Integer.toString(offset));

        if (animeTitle != null) queryUrl.addQueryParameter("filter[text]", animeTitle);

        if (seasonYear != 0) queryUrl.addQueryParameter("filter[seasonYear]", Integer.toString(seasonYear));

        if (season != null) queryUrl.addQueryParameter("filter[season]", season.getString());

        if (status != null) queryUrl.addQueryParameter("filter[status]", status.getString());

        if (!customParameters.isEmpty()) {
            for (StringPair parameter : customParameters) {
                queryUrl.addQueryParameter(parameter.getName(), parameter.getValue());
            }
        }

        return queryUrl.build();
    }

    public enum Seasons {
        WINTER("winter"),
        SPRING("spring"),
        SUMMER("summer"),
        FALL("fall");

        private final String season;

        Seasons(String season) {
            this.season = season;
        }

        public String getString() {
            return this.season;
        }
    }

    public enum Status {
        CURRENT("current"),
        FINISHED("finished"),
        TBA("tba"),
        UNRELEASED("unreleased"),
        UPCOMING("upcoming");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        public String getString() {
            return this.status;
        }
    }
}
