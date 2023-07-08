package com.astarivi.kaizolib.common.util;

import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;


public class ResponseToString {

    public static @Nullable String read(@NotNull Response response){
        try {
            ResponseBody body = response.body();

            if (body == null) return null;

            String bodyAsString = body.string();
            body.close();

            return bodyAsString;
        } catch (IOException e) {
            Logger.warn("The CloseableHttpResponse went through to the host and back,"
                        +"but it couldn't be read correctly, or didn't close. ", e);
            return null;
        }
    }
}
