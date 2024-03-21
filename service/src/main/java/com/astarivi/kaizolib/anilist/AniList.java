package com.astarivi.kaizolib.anilist;

import com.astarivi.kaizolib.anilist.base.AniListBase;
import com.astarivi.kaizolib.anilist.exception.AniListException;
import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class AniList extends AniListBase {
    public @NotNull AniListAnime get(long id) throws AniListException, IOException {
        TreeMap<String, Long> variables = new TreeMap<>();
        variables.put("id", id);

        GraphQLRequest<Map<String, Long>> graphQlContent = new GraphQLRequest<>(
                ANIME_QUERY_BY_ID,
                variables
        );

        String response = HttpMethodsV2.executeRequest(
                getRequestFor(graphQlContent)
        );

        return AniListAnime.deserializeOne(response);
    }

    public @NotNull List<AniListAnime> search(String title, int page, int limit) throws AniListException, IOException {
        TreeMap<String, Object> variables = new TreeMap<>();
        variables.put("name", title);
        variables.put("page", page);
        variables.put("limit", limit);

        GraphQLRequest<Map<String, Object>> graphQlContent = new GraphQLRequest<>(
                ANIME_QUERY_SEARCH_TITLE,
                variables
        );

        String response = HttpMethodsV2.executeRequest(
                getRequestFor(graphQlContent)
        );

        return AniListAnime.deserializeMany(response);
    }
}
