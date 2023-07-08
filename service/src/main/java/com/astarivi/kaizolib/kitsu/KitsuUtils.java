package com.astarivi.kaizolib.kitsu;

import com.astarivi.kaizolib.common.util.StringPair;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public class KitsuUtils {

    public static @NotNull HttpUrl buildTrendingAnimeUri() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("kitsu.io")
                .addPathSegments("api/edge/trending/anime")
                .build();
    }

    public static @NotNull HttpUrl buildIdUri(int id) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("kitsu.io")
                .addPathSegments("api/edge/anime")
                .addPathSegment(Integer.toString(id))
                .build();
    }

    public static @NotNull HttpUrl buildEpisodesUri(int animeId, int limit, int offset) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("kitsu.io")
                .addPathSegments("api/edge/episodes")
                .addQueryParameter("filter[media_id]", Integer.toString(animeId))
                .addQueryParameter("page[limit]", Integer.toString(limit))
                .addQueryParameter("page[offset]", Integer.toString(offset))
                .addQueryParameter("sort", "number")
                .build();
    }

    public static @NotNull HttpUrl buildEpisodeUri(int animeId, int episode) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("kitsu.io")
                .addPathSegments("api/edge/episodes")
                .addQueryParameter("filter[media_id]", Integer.toString(animeId))
                .addQueryParameter("filter[number]", Integer.toString(episode))
                .build();
    }

    public static @NotNull HttpUrl buildSearchUri(int limit, int offset, String animeTitle, int seasonYear, Seasons season,
                                                  Status status, List<StringPair> customParameters) {
        HttpUrl.Builder queryUrl = new HttpUrl.Builder();
        queryUrl.scheme("https").host("kitsu.io").addPathSegments("api/edge/anime");

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
