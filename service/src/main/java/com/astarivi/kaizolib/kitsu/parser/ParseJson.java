package com.astarivi.kaizolib.kitsu.parser;

import com.astarivi.kaizolib.kitsu.model.KitsuEpisodeResults;
import com.astarivi.kaizolib.kitsu.model.KitsuResourceResult;
import com.astarivi.kaizolib.kitsu.model.KitsuSearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ParseJson {
    public static int parseEpisodesLength(@NotNull String jsonInString) {
        KitsuEpisodeResults results = parseEpisodes(jsonInString);

        if (results == null || results.meta == null) return 0;

        return results.meta.count;
    }

    public static @Nullable KitsuEpisodeResults parseEpisodes(@NotNull String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, KitsuEpisodeResults.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static @Nullable KitsuSearchResults parseAnime(@NotNull String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, KitsuSearchResults.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static @Nullable KitsuResourceResult parseAnimeResource(@NotNull String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, KitsuResourceResult.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
