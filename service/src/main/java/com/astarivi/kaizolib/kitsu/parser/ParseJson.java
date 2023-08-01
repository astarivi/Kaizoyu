package com.astarivi.kaizolib.kitsu.parser;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisodeResults;
import com.astarivi.kaizolib.kitsu.model.KitsuResourceResult;
import com.astarivi.kaizolib.kitsu.model.KitsuSearchResults;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class ParseJson {
    public static int parseEpisodesLength(@NotNull String jsonInString) throws ParsingException {
        KitsuEpisodeResults results = parseEpisodes(jsonInString);

        if (results.meta == null) return 0;

        return results.meta.count;
    }

    public static @NotNull KitsuEpisodeResults parseEpisodes(@NotNull String jsonInString) throws ParsingException {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, KitsuEpisodeResults.class);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    public static @NotNull KitsuSearchResults parseAnime(@NotNull String jsonInString) throws ParsingException {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, KitsuSearchResults.class);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    public static @NotNull KitsuResourceResult parseAnimeResource(@NotNull String jsonInString) throws ParsingException {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, KitsuResourceResult.class);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }
}
