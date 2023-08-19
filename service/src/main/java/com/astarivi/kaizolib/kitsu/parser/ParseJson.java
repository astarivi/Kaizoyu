package com.astarivi.kaizolib.kitsu.parser;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisodeResults;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class ParseJson {
    public static int parseEpisodesLength(@NotNull String jsonInString) throws ParsingException {
        KitsuEpisodeResults results = parseGeneric(jsonInString, KitsuEpisodeResults.class);

        if (results.meta == null) return 0;

        return results.meta.count;
    }

    public static @NotNull <T> T parseGeneric(@NotNull String jsonString, Class<T> resultType) throws ParsingException {
        try {
            return JsonMapper.getObjectReader().readValue(jsonString, resultType);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }
}
