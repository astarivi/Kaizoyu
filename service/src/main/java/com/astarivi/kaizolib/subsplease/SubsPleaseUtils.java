package com.astarivi.kaizolib.subsplease;

import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

public class SubsPleaseUtils {
    public static @NotNull HttpUrl buildEmissionUrl(String timezone) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("subsplease.org")
                .addPathSegment("api/")
                .addQueryParameter("f", "schedule")
                .addQueryParameter("tz", timezone)
                .build();
    }

    public static @NotNull HttpUrl buildTodayUrl(String timezone) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("subsplease.org")
                .addPathSegment("api/")
                .addQueryParameter("f", "schedule")
                .addQueryParameter("h", "true")
                .addQueryParameter("tz", timezone)
                .build();
    }
}
