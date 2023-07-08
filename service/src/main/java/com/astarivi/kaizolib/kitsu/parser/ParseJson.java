package com.astarivi.kaizolib.kitsu.parser;

import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisodeResults;
import com.astarivi.kaizolib.kitsu.model.KitsuResourceResult;
import com.astarivi.kaizolib.kitsu.model.KitsuSearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;


public class ParseJson {
    public static int parseEpisodesLength(@NotNull String jsonInString) throws ParsingException {
        KitsuEpisodeResults results = parseEpisodes(jsonInString);

        if (results.meta == null) return 0;

        return results.meta.count;
    }

    public static @NotNull KitsuEpisodeResults parseEpisodes(@NotNull String jsonInString) throws ParsingException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, KitsuEpisodeResults.class);
        } catch (JsonProcessingException e) {
            throw new ParsingException(e);
        }
    }

    public static @NotNull KitsuSearchResults parseAnime(@NotNull String jsonInString) throws ParsingException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, KitsuSearchResults.class);
        } catch (JsonProcessingException e) {
            throw new ParsingException(e);
        }
    }

    public static @NotNull KitsuResourceResult parseAnimeResource(@NotNull String jsonInString) throws ParsingException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, KitsuResourceResult.class);
        } catch (JsonProcessingException e) {
            throw new ParsingException(e);
        }
    }
}
