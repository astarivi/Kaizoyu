package com.astarivi.kaizolib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.KitsuRelations;
import com.astarivi.kaizolib.kitsu.KitsuSearchParams;
import com.astarivi.kaizolib.kitsu.KitsuUtils;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;
import com.astarivi.kaizolib.kitsu.exception.NoResultsException;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizolib.kitsu.model.KitsuCategories;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;


public class KitsuTest {
    private static UserHttpClient userHttpClient;
    private static Kitsu kitsu;
    private static KitsuRelations relations;

    @BeforeAll
    static void setup() {
        userHttpClient = new UserHttpClient();
        kitsu = new Kitsu(userHttpClient);
        relations = new KitsuRelations(userHttpClient);
    }

    @Test
    @DisplayName("Kitsu get by Anime ID")
    void testKitsuById() throws NetworkConnectionException, ParsingException, NoResponseException, NoResultsException {
        KitsuAnime anime = kitsu.getAnimeById(43806);
        assertNotNull(anime);
        assertNotNull(anime.attributes);
        assertNotNull(anime.attributes.titles);
        assertNotNull(anime.attributes.posterImage);
        assertNotNull(anime.attributes.coverImage);
        assertEquals("Chainsaw Man", anime.attributes.canonicalTitle);
        assertEquals("Chainsaw Man", anime.attributes.titles.en);
        assertEquals("2022-10-11", anime.attributes.startDate);
    }

    @Test
    @DisplayName("Kitsu relations - Categories")
    void testKitsuRelationsCategories() throws NetworkConnectionException, ParsingException, NoResponseException, NoResultsException {
        List<KitsuCategories> categories = relations.getKitsuCategories(13);
        assertNotNull(categories);
    }

    @Test
    @DisplayName("Kitsu JP character decoding (Anime ID 43806)")
    void testKitsuJPDecoding() throws NetworkConnectionException, ParsingException, NoResponseException, NoResultsException {
        KitsuAnime anime = kitsu.getAnimeById(43806);
        assertNotNull(anime);
        assertNotNull(anime.attributes);

        byte[] rawTitle = hexStringToByteArray("e38381e382a7e383b3e382bde383bce3839ee383b3");

        assertEquals(
                new String(
                        rawTitle,
                        StandardCharsets.UTF_8
                ),
                anime.attributes.titles.ja_jp
        );
    }

    @Test
    @DisplayName("Kitsu single search convenience method")
    void testKitsuConvenienceSearch() throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        KitsuAnime anime = kitsu.getAnime(
                new KitsuSearchParams()
                        .setTitle("Attack on Titan")
                        .setStatus(KitsuUtils.Status.FINISHED)
                        .setSeasonYear(2013)
        );

        assertNotNull(anime);
        assertNotNull(anime.attributes);
        assertEquals("2013-04-07", anime.attributes.startDate);
    }

    @Test
    @DisplayName("Kitsu search (20 results limit)")
    void testKitsuSearch() throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        List<KitsuAnime> anime = kitsu.searchAnime(
                new KitsuSearchParams()
                        .setTitle("Attack on Titan")
                        .setLimit(20)
                        .setCustomParameter("sort", "popularityRank")
        );

        assertNotNull(anime);
        assertFalse(anime.isEmpty());
        assertEquals(anime.size(), 20);

        for (KitsuAnime individualAnime : anime) {
            assertNotNull(individualAnime);
        }
    }

    @Test
    @DisplayName("Kitsu trending (10 results limit)")
    void testKitsuTrending() throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        List<KitsuAnime> anime = kitsu.getTrendingAnime();

        assertNotNull(anime);
        assertFalse(anime.isEmpty());
        assertEquals(anime.size(), 10);
    }

    @Test
    @DisplayName("Kitsu long-running series detection")
    void testKitsuEpisodesLength() throws NetworkConnectionException, ParsingException, NoResponseException {
        // One Piece
        assertTrue(
                kitsu.isAnimeLongRunning(
                        12,
                        kitsu.getAnimeEpisodesLength(12)
                )
        );

        // Attack on Titan, a series that's obviously not long-running
        assertFalse(
                kitsu.isAnimeLongRunning(
                        7442,
                        kitsu.getAnimeEpisodesLength(7442)
                )
        );

        // Gintama
        assertTrue(
                kitsu.isAnimeLongRunning(
                        818,
                        kitsu.getAnimeEpisodesLength(818)
                )
        );

        // Death Note, a tricky case where it has >24 episodes, but is not long-running
        assertFalse(
                kitsu.isAnimeLongRunning(
                        1376,
                        kitsu.getAnimeEpisodesLength(1376)
                )
        );
    }

    @Test
    @DisplayName("Kitsu get all episodes of a series")
    void testKitsuEpisodes() throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        // Single episode search
        KitsuEpisode aotEpisode = kitsu.getEpisode(7442, 10);
        assertNotNull(aotEpisode);

        // Attack on Titan
        List<KitsuEpisode> aotEpisodes = kitsu.getAllEpisodes(7442, kitsu.getAnimeEpisodesLength(7442));

        assertNotNull(aotEpisodes);
        assertEquals(aotEpisodes.size(), 25);

        // Death Note
        List<KitsuEpisode> deathNoteEpisodes = kitsu.getAllEpisodes(1376, kitsu.getAnimeEpisodesLength(1376));

        assertNotNull(deathNoteEpisodes);
        assertEquals(deathNoteEpisodes.size(), 37);
    }

    @Test
    @DisplayName("Kitsu get range of episodes of a series")
    void testKitsuEpisodesRange() throws NoResultsException, NetworkConnectionException, ParsingException, NoResponseException {
        int totalLength = kitsu.getAnimeEpisodesLength(6448);

        // Hunter x Hunter
        List<KitsuEpisode> aotEpisodes = kitsu.getEpisodesRange(6448, 21, 40, totalLength);

        assertNotNull(aotEpisodes);
        assertEquals(aotEpisodes.size(), 20);
    }

    @AfterAll
    static void finish() {
        userHttpClient.close();
        relations = null;
        kitsu = null;
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
