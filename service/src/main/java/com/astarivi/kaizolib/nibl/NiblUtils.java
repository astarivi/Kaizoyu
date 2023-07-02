package com.astarivi.kaizolib.nibl;

import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;


public class NiblUtils {
    public static @NotNull HttpUrl buildLatestAnimeUri(int limit) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.nibl.co.uk")
                .addPathSegments("nibl/latest")
                .addQueryParameter("size", Integer.toString(limit))
                .build();
    }

    public static @NotNull HttpUrl buildSearchUri(int limit, String anime) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.nibl.co.uk")
                .addPathSegments("nibl/search")
                .addQueryParameter("query", anime)
                .addQueryParameter("size", Integer.toString(limit))
                .build();
    }

    public static @NotNull HttpUrl buildEpisodeSearchUri(int limit, String anime, int episode) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.nibl.co.uk")
                .addPathSegments("nibl/search")
                .addQueryParameter("query", anime)
                .addQueryParameter("episodeNumber", Integer.toString(episode))
                .addQueryParameter("size", Integer.toString(limit))
                .build();
    }

    public static @NotNull HttpUrl buildBotsUri() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.nibl.co.uk")
                .addPathSegments("nibl/bots")
                .build();
    }
}
