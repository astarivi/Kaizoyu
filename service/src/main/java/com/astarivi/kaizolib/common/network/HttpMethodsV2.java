package com.astarivi.kaizolib.common.network;

import com.astarivi.kaizolib.common.exception.NoResponseException;
import com.astarivi.kaizolib.common.exception.UnexpectedStatusCodeException;
import com.astarivi.kaizolib.common.util.ResponseToString;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpMethodsV2 {
    @NotNull
    public static String executeRequestWith(@NotNull Request request, @NotNull OkHttpClient client) throws IOException {
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            Logger.debug("Couldn't reach {}", request.url());
            throw e;
        }

        int responseCode = response.code();
        final String responseContent = ResponseToString.read(response);
        if (responseContent == null) {
            Logger.debug("Remote had no response for {}", request.url());
            throw new NoResponseException("Remote destination had no response to this request.");
        }

        return switch (responseCode) {
            case 304, 200 -> responseContent;
            default -> {
                Logger.error("Remote response code was incorrect, it was {}. (Only 200 and 304 allowed)", responseCode);
                throw new UnexpectedStatusCodeException(responseCode);
            }
        };
    }

    @NotNull
    public static String executeRequest(@NotNull Request request) throws IOException {
        return executeRequestWith(request, UserHttpClient.getInstance().getHttpClient());
    }
}
