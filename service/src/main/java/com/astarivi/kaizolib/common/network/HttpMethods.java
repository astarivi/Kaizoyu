package com.astarivi.kaizolib.common.network;

import com.astarivi.kaizolib.common.util.ResponseToString;
import com.astarivi.kaizolib.common.util.StringPair;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;


public class HttpMethods {

    // Expects body to contain data.
    public static @NotNull String get(UserHttpClient client, HttpUrl url, @Nullable List<StringPair> headers) throws
            NetworkConnectionException,
            NoResponseException
    {
        Request.Builder getRequestBuilder = new Request.Builder();
        getRequestBuilder.url(url);

        if (headers != null && !headers.isEmpty()) {
            for (StringPair header : headers) {
                getRequestBuilder.addHeader(header.getName(), header.getValue());
            }
        }

        Response response;
        try {
            response = client.executeRequest(
                    getRequestBuilder.build()
            );
        } catch (IOException e) {
            Logger.debug("Couldn't reach {}", url);
            throw new NetworkConnectionException("Couldn't reach remote destination");
        }

        int responseCode = response.code();
        final String responseContent = ResponseToString.read(response);
        if (responseContent == null) {
            Logger.debug("Remote had no response for {}", url);
            throw new NoResponseException("Remote destination had no response to this request.");
        }

        switch(responseCode) {
            case 304:
            case 200:
                return responseContent;
            default:
                Logger.error("Remote response code was incorrect, it was {}. (Only 200 and 304 allowed)", responseCode);
                throw new NoResponseException("Remote destination response code wasn't 200 (or 304).");
        }
    }
}
