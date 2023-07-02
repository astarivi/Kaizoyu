package com.astarivi.kaizolib.kitsu;

import com.astarivi.kaizolib.common.util.StringPair;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class KitsuSearchParams {
    private final List<StringPair> customParameters;
    private int limit = 20;
    private int offset = 0;
    private String animeTitle = null;
    private int seasonYear = 0;
    private KitsuUtils.Seasons seasons = null;
    private KitsuUtils.Status status = null;


    public KitsuSearchParams(){
        customParameters = new ArrayList<>();
    }

    public KitsuSearchParams setCustomParameter(@NotNull String key, @NotNull String value) {
        customParameters.add(
                new StringPair(key, value)
        );

        return this;
    }

    public KitsuSearchParams setTitle(String animeTitle) {
        animeTitle = animeTitle.replaceAll("\\[.*?]","")
                .replace("_", " ")
                .trim();

        this.animeTitle = animeTitle;
        return this;
    }

    public KitsuSearchParams setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public KitsuSearchParams setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public KitsuSearchParams setSeasonYear(int seasonYear) {
        this.seasonYear = seasonYear;
        return this;
    }

    public KitsuSearchParams setSeason(KitsuUtils.Seasons seasons) {
        this.seasons = seasons;
        return this;
    }

    public KitsuSearchParams setStatus(KitsuUtils.Status status) {
        this.status = status;
        return this;
    }

    public @NotNull HttpUrl buildURI() {
        return KitsuUtils.buildSearchUri(
                this.limit,
                this.offset,
                this.animeTitle,
                this.seasonYear,
                this.seasons,
                this.status,
                this.customParameters
        );
    }
}
