package com.astarivi.kaizolib.kitsuv2.common;

import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;
import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;

public abstract class KitsuCommon {
    protected static Request.@NotNull Builder getBaseBuilder(HttpUrl url) {
        Request.Builder builder = new Request.Builder().url(url);

        CommonHeaders.addTo(builder, CommonHeaders.KITSU_HEADERS);

        return builder;
    }

    protected static String executeGet(HttpUrl url) throws KitsuException {
        return execute(getBaseBuilder(url).get().build());
    }

    protected static String execute(Request request) throws KitsuException {
        try {
            return HttpMethodsV2.executeRequest(
                    request
            );
        } catch (IOException e) {
            throw new KitsuException(e);
        }
    }
}
