package com.astarivi.kaizolib.kitsuv2.public_api;

import com.astarivi.kaizolib.kitsuv2.common.KitsuCommon;
import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;

import org.jetbrains.annotations.NotNull;

import okhttp3.HttpUrl;

public class Methods extends KitsuCommon {
    protected static @NotNull String idRequest(long id) throws KitsuException {
        return executeGet(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("kitsu.io")
                        .addPathSegments("api/edge/anime")
                        .addPathSegment(Long.toString(id))
                        .build()
        );
    }

    protected static @NotNull String episodesRequest(long animeId, int limit, int offset) throws KitsuException{
        return executeGet(
                new HttpUrl.Builder()
                    .scheme("https")
                    .host("kitsu.io")
                    .addPathSegments("api/edge/episodes")
                    .addQueryParameter("filter[media_id]", Long.toString(animeId))
                    .addQueryParameter("page[limit]", Integer.toString(limit))
                    .addQueryParameter("page[offset]", Integer.toString(offset))
                    .addQueryParameter("sort", "number")
                    .build()
        );
    }
}
