package com.astarivi.kaizolib.kitsu;

import com.astarivi.kaizolib.kitsu.model.*;
import com.astarivi.kaizolib.kitsu.parser.ParseJson;
import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.common.util.ResponseToString;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Kitsu {
    private final UserHttpClient client;

    public Kitsu(UserHttpClient client) {
        this.client = client;
    }

    public Kitsu() {
        client = new UserHttpClient();
    }

    public @Nullable List<KitsuAnime> searchAnime(@NotNull KitsuSearchParams params) {
        return fetchAnime(params.buildURI());
    }

    // Convenience method.
    public @Nullable KitsuAnime getAnime(@NotNull KitsuSearchParams params) {
        List<KitsuAnime> result = fetchAnime(params.setLimit(1).buildURI());
        if (result == null) return null;
        return result.get(0);
    }

    public @Nullable KitsuAnime getAnimeById(int id) {
        String response = this.fetch(KitsuUtils.buildIdUri(id));

        if (response == null) return null;

        KitsuResourceResult result = ParseJson.parseAnimeResource(response);

        if (result == null) return null;

        return result.data;
    }

    public @Nullable List<KitsuAnime> getTrendingAnime() {
        return this.fetchAnime(KitsuUtils.buildTrendingAnimeUri());
    }

    public int getAnimeEpisodesLength(int id) {
        String responseContent = this.fetch(
                KitsuUtils.buildEpisodesUri(id, 1, 0)
        );

        if (responseContent == null) return 0;

        return ParseJson.parseEpisodesLength(responseContent);
    }

    public boolean isAnimeLongRunning(int animeId, int episodesLength) {
        if (episodesLength <= 24) return false;
        // We definitely want to treat this as long-running
        if (episodesLength > 100) return true;

        List<KitsuEpisode> result = this.fetchEpisodes(KitsuUtils.buildEpisodesUri(animeId, 1, episodesLength - 1));

        if (result == null || result.isEmpty()) return true;
        if (result.get(0).attributes == null) return true;

        String lastEpisodeAirdate = result.get(0).attributes.airdate;

        if (lastEpisodeAirdate == null) return true;

        LocalDate currentDate = LocalDate.now();
        LocalDate emissionDate = LocalDate.parse(
                lastEpisodeAirdate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        return !currentDate.isAfter(emissionDate);
    }

    // Not recommended for long-running anime.
    public @Nullable List<KitsuEpisode> getAllEpisodes(int animeId, int length) {
        if (length <= 20) return this.fetchEpisodes(KitsuUtils.buildEpisodesUri(animeId, 20, 0));

        List<KitsuEpisode> result = new ArrayList<>();

        int totalPages = (int) Math.ceil(length / 20F);
        int currentPage = 0;

        while (totalPages > currentPage) {
            HttpUrl url = KitsuUtils.buildEpisodesUri(animeId, 20, currentPage * 20);
            List<KitsuEpisode> fetchedEpisodes = this.fetchEpisodes(url);

            if (fetchedEpisodes != null) {
                result.addAll(fetchedEpisodes);
                fetchedEpisodes.clear();
            }

            currentPage++;
        }

        if (result.isEmpty()) return null;

        return result;
    }

    public @Nullable List<KitsuEpisode> getEpisodesRange(int animeId, int from, int to, int totalLength) {
        if (totalLength <= 20) return this.fetchEpisodes(KitsuUtils.buildEpisodesUri(animeId, 20, 0));

        if (to > totalLength) to = totalLength;
        if (from > totalLength || from > to || to - from > 20) return null;

        from -= 1;
        int limit = to - from;

        return this.fetchEpisodes(
                KitsuUtils.buildEpisodesUri(animeId, limit, from)
        );
    }

    private @Nullable List<KitsuEpisode> fetchEpisodes(HttpUrl url) {
        String responseContent = this.fetch(url);

        if (responseContent == null) return null;

        KitsuEpisodeResults episodeResult = ParseJson.parseEpisodes(responseContent);

        if (episodeResult == null || episodeResult.data == null || episodeResult.data.isEmpty()) return null;

        return episodeResult.data;
    }

    private @Nullable List<KitsuAnime> fetchAnime(HttpUrl url) {
        String responseContent = this.fetch(url);

        if (responseContent == null) return null;

        KitsuSearchResults animeResult = ParseJson.parseAnime(responseContent);

        if (animeResult == null || animeResult.data == null || animeResult.data.isEmpty()) return null;

        return animeResult.data;
    }

    private @Nullable String fetch(HttpUrl url) {
        Request.Builder getRequestBuilder = new Request.Builder();
        getRequestBuilder.url(url);
        getRequestBuilder.addHeader("Accept","application/vnd.api+json");
        getRequestBuilder.addHeader("Content-Type","application/vnd.api+json");
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
