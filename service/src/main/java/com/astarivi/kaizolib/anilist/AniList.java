package com.astarivi.kaizolib.anilist;

import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;


public class AniList extends AniListCommon {
    public static @NotNull AniListQuery.Single get(long id){
        TreeMap<String, Object> variables = new TreeMap<>();
        variables.put("id", id);

        GraphQLRequest graphQLRequest = new GraphQLRequest(
                ANIME_QUERY_BY_ID,
                variables
        );

        return new AniListQuery.Single(graphQLRequest);
    }

    public static @NotNull AniListQuery.Paged search(String title) {
        TreeMap<String, Object> variables = new TreeMap<>();
        variables.put("name", title);

        PagedGraphQLRequest graphQlContent = new PagedGraphQLRequest(
                ANIME_QUERY_SEARCH_TITLE,
                variables
        );

        return new AniListQuery.Paged(graphQlContent);
    }

    public static @NotNull AniListQuery.Paged query(TYPE queryType) {
        TreeMap<String, Object> variables = new TreeMap<>();

        PagedGraphQLRequest graphQlContent = new PagedGraphQLRequest(
                queryType.getQuery(),
                variables
        );

        return new AniListQuery.Paged(graphQlContent);
    }

    public enum TYPE {
        TRENDING(TRENDING_ANIME_QUERY);
        private final String query;
        TYPE(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }
    }
}
