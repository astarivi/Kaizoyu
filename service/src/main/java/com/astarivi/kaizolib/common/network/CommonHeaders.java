package com.astarivi.kaizolib.common.network;

import com.astarivi.kaizolib.common.util.StringPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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
}
