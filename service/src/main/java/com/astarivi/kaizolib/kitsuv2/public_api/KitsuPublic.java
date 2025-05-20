package com.astarivi.kaizolib.kitsuv2.public_api;

import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.parser.ParseJson;
import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizolib.kitsuv2.model.KitsuEpisode;
import com.astarivi.kaizolib.kitsuv2.model.RawResults;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class KitsuPublic extends Methods {
    public static @NotNull KitsuAnime get(long id) throws KitsuException, ParsingError {
        return KitsuAnime.deserializeOne(
            idRequest(id)
        );
    }

    public static RawResults rawSearch(@NotNull SearchParams params) throws KitsuException, ParsingError {
        return KitsuAnime.deserializeRawSearch(
                executeGet(params.buildURI())
        );
    }

    public static List<KitsuAnime> advancedSearch(@NotNull SearchParams params) throws KitsuException, ParsingError {
        return KitsuAnime.deserializeSearch(
                executeGet(params.buildURI())
        );
    }

    public static List<KitsuAnime> search(String title) throws KitsuException, ParsingError {
        return KitsuAnime.deserializeSearch(
                executeGet(new SearchParams().setTitle(title).buildURI())
        );
    }

    public static @NotNull List<KitsuEpisode> getEpisodesRange(long animeId, int from, int to, int totalLength) throws KitsuException, ParsingError {
        if (totalLength <= 20) {
            return KitsuEpisode.deserializeSearch(
                    episodesRequest(animeId, 20, 0)
            );
        }

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

        return KitsuEpisode.deserializeSearch(
                episodesRequest(animeId, limit, from)
        );
    }

    public static int episodeCount(long animeId) throws KitsuException, ParsingException {
        String req = episodesRequest(animeId, 1, 0);

        KitsuEpisode.SearchResults se = ParseJson.parseGeneric(req, KitsuEpisode.SearchResults.class);

        if (se.meta == null) return 0;

        return se.meta.count;
    }

    public static boolean isAnimeLongRunning(long animeId, int episodesLength) {
        if (episodesLength <= 24) return false;
        // We definitely want to treat this as long-running
        if (episodesLength > 100) return true;

        List<KitsuEpisode> result;

        try {
            result = KitsuEpisode.deserializeSearch(
                    episodesRequest(animeId, 1, episodesLength - 1)
            );
        } catch (KitsuException | ParsingError e) {
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
}
