package com.astarivi.kaizolib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.astarivi.kaizolib.anilist.AniList;
import com.astarivi.kaizolib.anilist.AniListQuery;
import com.astarivi.kaizolib.anilist.AniListSchedule;
import com.astarivi.kaizolib.anilist.model.AiringSchedule;
import com.astarivi.kaizolib.anilist.model.AniListAnime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


public class AniListTest {
    @Test
    @DisplayName("AniList by ID")
    void testById() throws Exception {
        AniListQuery.Single query = AniList.get(1);
        AniListAnime anime = query.get();
        assertEquals(anime.title.english, "Cowboy Bebop");

        AniListQuery.Single query2 = AniList.get(154587);
        AniListAnime anime2 = query2.get();
        assertEquals(anime2.title.english, "Frieren: Beyond Journey’s End");
    }

    @Test
    @DisplayName("AniList search by title")
    void testSearch() throws Exception {
        AniListQuery.Paged query = AniList.search("Mobile Suit Gundam: The Witch from Mercury");

        List<AniListAnime> result = query.next();

        assertEquals(result.get(0).title.english, "Mobile Suit Gundam: The Witch from Mercury");

        assertTrue(result.size() > 1);
    }

    @Test
    @DisplayName("AniList generic query")
    void testGenericQuery() throws Exception {
        AniListQuery.Paged query = AniList.sortedBy(AniList.TYPE.TRENDING);
        List<AniListAnime> result = query.next();
    }

    @Test
    @DisplayName("AniList get airing schedule")
    void testAiring() throws Exception {
        AiringSchedule airingSchedule = AniListSchedule.airingSchedule();
    }

    @Test
    @DisplayName("AniList get next episode airing")
    void testEpisodeAiring() throws Exception {
        AiringSchedule.Episode airingSchedule = AniListSchedule.nextAiringEpisodeFor(166610);
    }
}
