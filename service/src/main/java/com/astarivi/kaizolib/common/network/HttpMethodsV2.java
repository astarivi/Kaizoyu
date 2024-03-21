package com.astarivi.kaizolib.common.network;

import com.astarivi.kaizolib.common.exception.NoResponseException;
import com.astarivi.kaizolib.common.util.ResponseToString;

import org.tinylog.Logger;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;


public class HttpMethodsV2 {
    public static String executeRequest(Request request) throws IOException, NoResponseException {
        Response response;
        try {
            response = UserHttpClient.getInstance().executeRequest(
                    request
            );
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
