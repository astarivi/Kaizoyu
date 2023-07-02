package com.astarivi.kaizolib.nibl.parser;

import com.astarivi.kaizolib.nibl.model.NiblBotsResults;
import com.astarivi.kaizolib.nibl.model.NiblSearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ParseJson {
    public static @Nullable NiblSearchResults parse(@NotNull String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, NiblSearchResults.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static @Nullable NiblBotsResults parseBots(@NotNull String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, NiblBotsResults.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
