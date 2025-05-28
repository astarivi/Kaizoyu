package com.astarivi.kaizolib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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


public class NiblTest {
    private static UserHttpClient userHttpClient;

    @BeforeAll
    static void setup() {
        userHttpClient = UserHttpClient.getInstance();
    }

    @Test
    @DisplayName("Nibl get bots list (and relation)")
    void testNiblBots() {
        List<NiblBot> bots = Nibl.getBots();
        assertNotNull(bots);
        assertFalse(bots.isEmpty());

        Properties botsRelation = Nibl.getBotsMap(null);
        assertNotNull(botsRelation);
        assertFalse(botsRelation.isEmpty());

        botsRelation = new Properties();
        Nibl.getBotsMap(botsRelation);
        assertFalse(botsRelation.isEmpty());
    }

    @Test
    @DisplayName("Nibl get latest")
    void testNiblLatest() {
        List<NiblResult> results = Nibl.getLatest(20);
        assertNotNull(results);
        assertEquals(results.size(), 20);
    }

    @Test
    @DisplayName("Nibl Anime Search")
    void testNiblSearch() {
        String[] testTiles = new String[] {"Vinland Saga", "Death Note", "One Piece", "Gintama"};

        for (String testTitle : testTiles) {
            List<NiblResult> results = Nibl.searchAnime(20, testTitle);
            assertNotNull(results);
            assertFalse(results.isEmpty());
        }
    }

    @Test
    @DisplayName("Nibl Episode Search")
    void testNiblEpisodeSearch() {
        String[] testTiles = new String[] {"Vinland Saga", "Death Note", "One Piece", "Gintama"};

        for (String testTitle : testTiles) {
            List<NiblResult> results = Nibl.searchAnimeEpisode(2, testTitle, 1);
            assertNotNull(results);
            assertFalse(results.isEmpty());
        }
    }

    @AfterAll
    static void finish() {
        userHttpClient.close();
    }
}
