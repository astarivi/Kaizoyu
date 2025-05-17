package com.astarivi.kaizolib.common.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.IOException;


public class UserHttpClient {
    private static volatile UserHttpClient _instance = null;
    private final OkHttpClient httpClient;

    private UserHttpClient(){
        httpClient = new OkHttpClient();
    }

    private UserHttpClient(OkHttpClient httpClient){
        this.httpClient = httpClient;
    }

    public Response executeRequest(Request request) throws IOException {
        return httpClient.newCall(request).execute();
    }

    public OkHttpClient getHttpClient(){
        return httpClient;
    }

    public static @NotNull UserHttpClient getInstance() {
        if (_instance == null) {
            synchronized (UserHttpClient.class) {
                if (_instance == null) _instance = new UserHttpClient();
            }
        }
        return _instance;
    }

    public void close() {
        try {
            httpClient.dispatcher().executorService().shutdownNow();
            httpClient.connectionPool().evictAll();
            if (httpClient.cache() != null) httpClient.cache().close();
        } catch (IOException io) {
            Logger.warn("The HttpClient couldn't close. ", io);
        }
    }
}
