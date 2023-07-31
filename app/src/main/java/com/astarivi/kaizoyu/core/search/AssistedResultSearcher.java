package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.nibl.Nibl;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.adapters.WebAdapter;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.utils.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;


public class AssistedResultSearcher {
    public @Nullable List<Result> searchEpisode(int kitsuId, String title, int episode) {
        Nibl nibl = new Nibl(
                Data.getUserHttpClient()
        );

        IndependentResultSearcher independentResultSearcher = new IndependentResultSearcher(
                nibl
        );

        SearchEnhancer searchEnhancer = getSearchEnhancer(kitsuId);

        if (searchEnhancer == null) return independentResultSearcher.searchEpisode(title, episode);

        List<NiblResult> niblResults = independentResultSearcher.fetchEpisode(
                searchEnhancer.title,
                searchEnhancer.episode != null ? searchEnhancer.episode + episode : episode
        );

        if (niblResults == null) {
            Logger.error("Got no results after using enhanced search.");
            return null;
        }

        searchEnhancer.filter(niblResults);

        return SearchUtils.parseResults(niblResults, nibl);
    }

    public @Nullable SearchEnhancer getSearchEnhancer(int kitsuId) {
        if (kitsuId == -1) return null;

        String body = WebAdapter.getJSON(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("search.kaizoyu.ovh")
                        .addPathSegments("api/v1/xdcc/search")
                        .addPathSegment(String.valueOf(kitsuId))
                        .build()
        );

        if (body == null) return null;

        SearchEnhancer enhancedSearch;

        try {
            enhancedSearch = new ObjectMapper().readValue(body, AssistedResultSearcher.SearchEnhancer.class);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to decode enhanced search from search.kaizoyu.ovh");
            return null;
        }

        return enhancedSearch;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchEnhancer {
        public String type;
        public int databaseId;
        public String responseType;
        public String title;
        public String regex;
        public Integer episode;

        @JsonIgnore
        public void filter(@NotNull List<NiblResult> niblResults) {
            if (responseType.equals("title")) return;

            Pattern pattern = Pattern.compile(regex);

            niblResults.removeIf(result -> {
                Matcher matcher = pattern.matcher(result.name);
                return !matcher.find();
            });
        }
    }
}
