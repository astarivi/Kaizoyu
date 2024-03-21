package com.astarivi.kaizolib.common.network;

import com.astarivi.kaizolib.common.util.StringPair;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Request;


public class CommonHeaders {
    public static List<StringPair> JSON_HEADERS = Collections.unmodifiableList(
            new ArrayList<>(
                    Arrays.asList(
                            new StringPair("Accept", "application/json"),
                            new StringPair("Content-Type", "application/vnd.api+json")
                    )
            )
    );
    public static List<StringPair> KITSU_HEADERS = Collections.unmodifiableList(
            new ArrayList<>(
                    Arrays.asList(
                            new StringPair("Accept", "application/vnd.api+json"),
                            new StringPair("Content-Type", "application/vnd.api+json")
                    )
            )
    );

    public static void addTo(@NotNull Request.Builder requestBuilder, @NotNull List<StringPair> headers) {
        for (StringPair header : headers) {
            requestBuilder.addHeader(header.getName(), header.getValue());
        }
    }
}
