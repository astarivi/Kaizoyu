package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;


public class AssistedResultSearcher {
    @ThreadedOnly
    public static @Nullable SearchEnhancer getSearchEnhancer(long kitsuId) {
        if (kitsuId == -1) return null;

        Request.Builder getRequestBuilder = new Request.Builder();

        getRequestBuilder.url(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("kaizoyu.ddns.net")
                        .addPathSegments("enhancer/api/v1/kitsu_search")
                        .addPathSegment(String.valueOf(kitsuId))
                        .build()
        );

        CommonHeaders.addTo(getRequestBuilder, CommonHeaders.JSON_HEADERS);

        String body;

        try {
            body = HttpMethodsV2.executeRequest(getRequestBuilder.build());
        } catch (IOException e) {
            Logger.error("UpdateManager.getLatestReleases error at executing request");
            Logger.error(e);
            return null;
        }

        if (body == null) return null;

        return SearchEnhancer.fromJson(body);
    }
}
