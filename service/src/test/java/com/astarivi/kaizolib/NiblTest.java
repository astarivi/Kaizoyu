package com.astarivi.kaizolib;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.nibl.Nibl;
import com.astarivi.kaizolib.nibl.model.NiblBot;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


public class NiblTest {
    private static UserHttpClient userHttpClient;
    private static Nibl nibl;

    @BeforeAll
    static void setup() {
        userHttpClient = UserHttpClient.getInstance();
        nibl = new Nibl(userHttpClient);
    }

    @Test
    @DisplayName("Nibl get bots list (and relation)")
    void testNiblBots() {
        List<NiblBot> bots = nibl.getBots();
        assertNotNull(bots);
        assertFalse(bots.isEmpty());

        Properties botsRelation = nibl.getBotsMap(null);
        assertNotNull(botsRelation);
        assertFalse(botsRelation.isEmpty());

        botsRelation = new Properties();
        nibl.getBotsMap(botsRelation);
        assertFalse(botsRelation.isEmpty());
    }

    @Test
    @DisplayName("Nibl get latest")
    void testNiblLatest() {
        List<NiblResult> results = nibl.getLatest(20);
        assertNotNull(results);
        assertEquals(results.size(), 20);
    }

    @Test
    @DisplayName("Nibl Anime Search")
    void testNiblSearch() {
        String[] testTiles = new String[] {"Vinland Saga", "Death Note", "One Piece", "Gintama"};

        for (String testTitle : testTiles) {
            List<NiblResult> results = nibl.searchAnime(20, testTitle);
            assertNotNull(results);
            assertFalse(results.isEmpty());
        }
    }

    @Test
    @DisplayName("Nibl Episode Search")
    void testNiblEpisodeSearch() {
        String[] testTiles = new String[] {"Vinland Saga", "Death Note", "One Piece", "Gintama"};

        for (String testTitle : testTiles) {
            List<NiblResult> results = nibl.searchAnimeEpisode(2, testTitle, 1);
            assertNotNull(results);
            assertFalse(results.isEmpty());
        }
    }

    @AfterAll
    static void finish() {
        userHttpClient.close();
        nibl = null;
    }
}
