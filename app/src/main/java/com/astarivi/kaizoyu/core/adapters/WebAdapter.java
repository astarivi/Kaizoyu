package com.astarivi.kaizoyu.core.adapters;

import com.astarivi.kaizolib.common.util.ResponseToString;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;


// TODO: Replace with HttpMethods class from :service module
public class WebAdapter {
    public static @Nullable String getJSON(HttpUrl url) {
        Request.Builder getRequestBuilder = new Request.Builder();

        getRequestBuilder.url(url);
        getRequestBuilder.addHeader("Accept","application/json");
        getRequestBuilder.addHeader("Content-Type","application/vnd.api+json");

        Response response;

        try {
            response = Data.getUserHttpClient().executeRequest(
                    getRequestBuilder.build()
            );
        } catch (IOException e) {
            return null;
        }

        int responseCode = response.code();
        final String responseContent = ResponseToString.read(response);
        if (responseContent == null) {
            return null;
        }

        switch(responseCode) {
            case 304:
            case 200:
                return responseContent;
            default:
                Logger.error("Couldn't connect to " + url);
                break;
        }

        return null;
    }
}
