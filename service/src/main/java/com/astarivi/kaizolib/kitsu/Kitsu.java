package com.astarivi.kaizolib.kitsu;

import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethods;
import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;
import com.astarivi.kaizolib.kitsu.exception.NoResultsException;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisodeResults;
import com.astarivi.kaizolib.kitsu.model.KitsuResourceResult;
import com.astarivi.kaizolib.kitsu.model.KitsuSearchResults;
import com.astarivi.kaizolib.kitsu.parser.ParseJson;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;


public class Kitsu {
    private final UserHttpClient client;

    public Kitsu(UserHttpClient client) {
        this.client = client;
    }

    public Kitsu() {
        client = new UserHttpClient();
    }

    public @NotNull List<KitsuAnime> searchAnime(@NotNull KitsuSearchParams params) throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        return fetchAnime(params.buildURI());
    }

    // Convenience method.
    public @NotNull KitsuAnime getAnime(@NotNull KitsuSearchParams params) throws
            NoResultsException,
            NetworkConnectionException,
            ParsingException,
            NoResponseException {
        return fetchAnime(params.setLimit(1).buildURI()).get(0);
    }

    public @NotNull KitsuAnime getAnimeById(int id) throws NetworkConnectionException, NoResponseException, ParsingException, NoResultsException {
        String responseContent = HttpMethods.get(client, KitsuUtils.buildIdUri(id), CommonHeaders.KITSU_HEADERS);

        KitsuResourceResult animeResult = ParseJson.parseAnimeResource(responseContent);

        if (animeResult.data == null) {
            Logger.debug("Kitsu URL {} yielded no results. (Anime search)");
            throw new NoResultsException("No results found for this URL");
        }

        return animeResult.data;
    }

    public @NotNull List<KitsuAnime> getTrendingAnime() throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        return this.fetchAnime(KitsuUtils.buildTrendingAnimeUri());
    }

    public int getAnimeEpisodesLength(int id) throws NetworkConnectionException, NoResponseException, ParsingException {
        String responseContent = HttpMethods.get(
                client,
                KitsuUtils.buildEpisodesUri(id, 1, 0),
                CommonHeaders.KITSU_HEADERS
        );

        return ParseJson.parseEpisodesLength(responseContent);
    }

    public boolean isAnimeLongRunning(int animeId, int episodesLength) throws
            NetworkConnectionException,
            ParsingException
    {
        if (episodesLength <= 24) return false;
        // We definitely want to treat this as long-running
        if (episodesLength > 100) return true;

        List<KitsuEpisode> result;

        // Kitsu is weird, innit
        try {
            result = this.fetchEpisodes(KitsuUtils.buildEpisodesUri(animeId, 1, episodesLength - 1));
        } catch (NoResponseException | NoResultsException e) {
            return true;
        }

        if (result.isEmpty()) return true;
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

    public @NotNull KitsuEpisode getEpisode(int animeId, int episode) throws
            NoResultsException,
            NetworkConnectionException,
            ParsingException,
            NoResponseException
    {

        return this.fetchEpisodes(KitsuUtils.buildEpisodeUri(animeId, episode)).get(0);

    }

    // Not recommended for long-running anime.
    public @NotNull List<KitsuEpisode> getAllEpisodes(int animeId, int length) throws
            NoResultsException,
            NetworkConnectionException,
            ParsingException,
            NoResponseException
    {
        if (length <= 20) return this.fetchEpisodes(KitsuUtils.buildEpisodesUri(animeId, 20, 0));

        List<KitsuEpisode> result = new ArrayList<>();

        int totalPages = (int) Math.ceil(length / 20F);
        int currentPage = 0;

        while (totalPages > currentPage) {
            HttpUrl url = KitsuUtils.buildEpisodesUri(animeId, 20, currentPage * 20);
            List<KitsuEpisode> fetchedEpisodes = this.fetchEpisodes(url);

            result.addAll(fetchedEpisodes);
            fetchedEpisodes.clear();

            currentPage++;
        }

        if (result.isEmpty()) throw new NoResultsException("No results for this range");

        return result;
    }

    public @NotNull List<KitsuEpisode> getEpisodesRange(int animeId, int from, int to, int totalLength) throws
            NoResultsException,
            NetworkConnectionException,
            ParsingException,
            NoResponseException
    {
        if (totalLength <= 20) return this.fetchEpisodes(KitsuUtils.buildEpisodesUri(animeId, 20, 0));

        if (to > totalLength) to = totalLength;
        if (from > totalLength || from > to || to - from > 20) {
            Logger.error(
                    "Tried to fetch episode range with bad parameters. {} {} {} {}",
                    animeId,
                    from,
                    to,
                    totalLength
            );
            throw new IllegalArgumentException("This set of positions (from - to, totalLength) is not possible.");
        }

        from -= 1;
        int limit = to - from;

        return this.fetchEpisodes(
                KitsuUtils.buildEpisodesUri(animeId, limit, from)
        );
    }

    private @NotNull List<KitsuEpisode> fetchEpisodes(HttpUrl url) throws
            NetworkConnectionException,
            NoResponseException,
            ParsingException,
            NoResultsException
    {
        String responseContent = HttpMethods.get(client, url, CommonHeaders.KITSU_HEADERS);

        KitsuEpisodeResults episodeResult = ParseJson.parseEpisodes(responseContent);

        if (episodeResult.data == null || episodeResult.data.isEmpty()) {
            Logger.debug("Kitsu URL {} yielded no results. (Episode search)");
            throw new NoResultsException("No results found for this URL");
        }

        return episodeResult.data;
    }

    private @NotNull List<KitsuAnime> fetchAnime(HttpUrl url) throws
            NetworkConnectionException,
            NoResponseException,
            ParsingException,
            NoResultsException
    {
        String responseContent = HttpMethods.get(client, url, CommonHeaders.KITSU_HEADERS);

        KitsuSearchResults animeResult = ParseJson.parseAnime(responseContent);

        if (animeResult.data == null || animeResult.data.isEmpty()) {
            Logger.debug("Kitsu URL {} yielded no results. (Anime search)");
            throw new NoResultsException("No results found for this URL");
        }

        return animeResult.data;
    }
}
