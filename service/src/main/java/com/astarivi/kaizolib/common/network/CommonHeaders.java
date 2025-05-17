package com.astarivi.kaizolib.common.network;

import com.astarivi.kaizolib.common.util.StringPair;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Request;


public class CommonHeaders {
    public static List<StringPair> JSON_HEADERS = List.of(
            new StringPair("Accept", "application/json"),
            new StringPair("Content-Type", "application/vnd.api+json")
    );
    public static List<StringPair> KITSU_HEADERS = List.of(
            new StringPair("Accept", "application/vnd.api+json"),
            new StringPair("Content-Type", "application/vnd.api+json")
    );
    public static List<StringPair> XML_HEADERS = List.of(
            new StringPair("Accept", "text/xml;charset=UTF-8"),
            new StringPair("Content-Type", "text/xml;charset=UTF-8")
    );
    public static List<StringPair> TEXT_HEADERS = List.of(
            new StringPair("Accept", "text/html; charset=UTF-8"),
            new StringPair("Content-Type", "text/html; charset=UTF-8")
    );

    public static void addTo(@NotNull Request.Builder requestBuilder, @NotNull List<StringPair> headers) {
        for (StringPair header : headers) {
            requestBuilder.addHeader(header.getName(), header.getValue());
        }
    }
}
