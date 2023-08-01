package com.astarivi.kaizolib.nibl.parser;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.nibl.model.NiblBotsResults;
import com.astarivi.kaizolib.nibl.model.NiblSearchResults;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;


public class ParseJson {
    public static @Nullable NiblSearchResults parse(@NotNull String jsonInString) {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, NiblSearchResults.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static @Nullable NiblBotsResults parseBots(@NotNull String jsonInString) {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, NiblBotsResults.class);
        } catch (IOException e) {
            return null;
        }
    }
}
