package com.astarivi.kaizolib.kitsuv2.private_api;

import com.astarivi.kaizolib.common.util.StringPair;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;

public class LibraryParams implements Cloneable {
    private final List<StringPair> customParameters;
    private final long kitsuUserId;
    private final KitsuList kitsuList;
    private int pageSize = 20;
    private int pageNumber = 0;

    public LibraryParams(long kitsuUserId, KitsuList list) {
        customParameters = new ArrayList<>();
        kitsuList = list;
        this.kitsuUserId = kitsuUserId;
    }

    protected LibraryParams(long kitsuUserId, KitsuList list, List<StringPair> wParameters) {
        customParameters = wParameters;
        kitsuList = list;
        this.kitsuUserId = kitsuUserId;
    }

    public LibraryParams setCustomParameter(@NotNull String key, @NotNull String value) {
        customParameters.add(
                new StringPair(key, value)
        );

        return this;
    }

    public LibraryParams setPageSize(int limit) {
        this.pageSize = limit;
        return this;
    }

    public LibraryParams setPageNumber(int page) {
        this.pageNumber = page;
        return this;
    }

    public @NotNull HttpUrl buildURI() {
        HttpUrl.Builder queryUrl = new HttpUrl.Builder();
        queryUrl.scheme("https").host("kitsu.app").addPathSegments("api/edge/library-entries");

        // Default queries
        queryUrl.addQueryParameter("filter[kind]", "anime");
        queryUrl.addQueryParameter("filter[userId]", String.valueOf(kitsuUserId));
        queryUrl.addQueryParameter("filter[status]", kitsuList.getString());
        queryUrl.addQueryParameter("include", "anime");

        // User queries
        queryUrl.addQueryParameter("page[size]", Integer.toString(pageSize));
        queryUrl.addQueryParameter("page[number]", Integer.toString(pageNumber));

        if (!customParameters.isEmpty()) {
            for (StringPair parameter : customParameters) {
                queryUrl.addQueryParameter(parameter.getName(), parameter.getValue());
            }
        }

        return queryUrl.build();
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public LibraryParams clone() {
        List<StringPair> copiedParams = new ArrayList<>();

        for (StringPair pair : this.customParameters) {
            copiedParams.add(new StringPair(pair.getName(), pair.getValue()));
        }

        LibraryParams clone = new LibraryParams(kitsuUserId, kitsuList, copiedParams);

        clone.pageSize = this.pageSize;
        clone.pageNumber = this.pageNumber;

        return clone;
    }

    public enum KitsuList {
        CURRENTLY_WATCHING("current"),
        WANT_TO_WATCH("planned"),
        COMPLETED("completed"),
        ON_HOLD("on_hold"),
        DROPPED("dropped");
        private final String list;

        KitsuList(String list) {
            this.list = list;
        }

        public String getString() {
            return this.list;
        }
    }
}
