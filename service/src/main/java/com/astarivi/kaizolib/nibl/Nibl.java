package com.astarivi.kaizolib.nibl;

import com.astarivi.kaizolib.common.network.HttpMethodsV2;
import com.astarivi.kaizolib.nibl.model.NiblBot;
import com.astarivi.kaizolib.nibl.model.NiblBotsResults;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizolib.nibl.model.NiblSearchResults;
import com.astarivi.kaizolib.nibl.parser.ParseJson;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import okhttp3.HttpUrl;
import okhttp3.Request;


public class Nibl {
    public static @Nullable List<NiblResult> searchAnimeEpisode(int limit, String anime, int episode) {
        return fetchResults(NiblUtils.buildEpisodeSearchUri(limit, anime, episode));
    }

    public static @Nullable List<NiblResult> searchAnime(int limit, String search) {
        return fetchResults(NiblUtils.buildSearchUri(limit, search));
    }

    public static @Nullable List<NiblResult> getLatest(int limit) {
        return fetchResults(NiblUtils.buildLatestAnimeUri(limit));
    }

    public static @Nullable List<NiblBot> getBots() {
        return fetchBots();
    }

    public static @Nullable Properties getBotsMap(@Nullable Properties botProperties) {
        List<NiblBot> bots = fetchBots();

        if (bots == null) return null;

        if (botProperties == null) botProperties = new Properties();

        for (NiblBot bot : bots) {
            botProperties.put(
                    String.valueOf(bot.id),
                    bot.name
            );
        }

        return botProperties;
    }

    private static @Nullable List<NiblBot> fetchBots() {
        String fetchResult = fetch(NiblUtils.buildBotsUri());

        if (fetchResult == null) return null;

        NiblBotsResults botsResults = ParseJson.parseBots(fetchResult);

        if (botsResults == null || botsResults.content == null) return null;

        return botsResults.content;
    }

    private static @Nullable List<NiblResult> fetchResults(HttpUrl url) {
        String responseContent = fetch(url);

        if (responseContent == null) return null;

        NiblSearchResults result = ParseJson.parse(responseContent);

        if (result == null || result.content == null || result.content.isEmpty()) return null;

        return result.content;
    }

    public static @Nullable String fetch(HttpUrl url) {
        Request.Builder getRequestBuilder = new Request.Builder();

        getRequestBuilder.url(url);
        getRequestBuilder.addHeader("Accept", "application/json");
        getRequestBuilder.addHeader("Content-Type", "application/json");
        String responseContent;

        try {
            responseContent = HttpMethodsV2.executeRequest(
                    getRequestBuilder.build()
            );
        } catch (IOException e) {
            return null;
        }

        return responseContent;
    }
}
