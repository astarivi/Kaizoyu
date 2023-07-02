package com.astarivi.kaizolib.nibl;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.common.util.ResponseToString;
import com.astarivi.kaizolib.nibl.model.NiblBot;
import com.astarivi.kaizolib.nibl.model.NiblBotsResults;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizolib.nibl.model.NiblSearchResults;
import com.astarivi.kaizolib.nibl.parser.ParseJson;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class Nibl {
    private final UserHttpClient client;

    public Nibl(UserHttpClient client) {
        this.client = client;
    }
    public Nibl() {
        client = new UserHttpClient();
    }

    public @Nullable List<NiblResult> searchAnimeEpisode(int limit, String anime, int episode) {
        return this.fetchResults(NiblUtils.buildEpisodeSearchUri(limit, anime, episode));
    }

    public @Nullable List<NiblResult> searchAnime(int limit, String search) {
        return this.fetchResults(NiblUtils.buildSearchUri(limit, search));
    }

    public @Nullable List<NiblResult> getLatest(int limit) {
        return this.fetchResults(NiblUtils.buildLatestAnimeUri(limit));
    }

    public @Nullable List<NiblBot> getBots() {
        return this.fetchBots();
    }

    public @Nullable Properties getBotsMap(@Nullable Properties botProperties) {
        List<NiblBot> bots = this.fetchBots();

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

    private @Nullable List<NiblBot> fetchBots() {
        String fetchResult = this.fetch(NiblUtils.buildBotsUri());

        if (fetchResult == null) return null;

        NiblBotsResults botsResults = ParseJson.parseBots(fetchResult);

        if (botsResults == null || botsResults.content == null) return null;

        return botsResults.content;
    }

    private @Nullable List<NiblResult> fetchResults(HttpUrl url) {
        String responseContent = this.fetch(url);

        if (responseContent == null) return null;

        NiblSearchResults result = ParseJson.parse(responseContent);

        if (result == null || result.content == null || result.content.isEmpty()) return null;

        return result.content;
    }

    public @Nullable String fetch(HttpUrl url) {
        Request.Builder getRequestBuilder = new Request.Builder();

        getRequestBuilder.url(url);
        getRequestBuilder.addHeader("Accept", "application/json");
        getRequestBuilder.addHeader("Content-Type", "application/json");
        Response response;

        try {
            response = client.executeRequest(
                    getRequestBuilder.build()
            );
        } catch (IOException e) {
            return null;
        }

        int responseCode = response.code();
        final String responseContent = ResponseToString.read(response);
        if (responseContent == null) {
            return null;
        }

        switch(responseCode) {
            case 304:
            case 200:
                return responseContent;
            default:
                Logger.error("Couldn't connect to Kitsu, or the request was denied when fetching.");
                break;
        }

        return null;
    }
}
