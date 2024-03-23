package com.astarivi.kaizolib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.astarivi.kaizolib.anilist.AniList;
import com.astarivi.kaizolib.anilist.model.AiringSchedule;
import com.astarivi.kaizolib.anilist.model.AniListAnime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AniListTest {
    @Test
    @DisplayName("AniList by ID")
    void testById() throws Exception {
        AniList aniList = new AniList();

        AniListAnime anime = aniList.get(1);

        assertEquals(anime.title.english, "Cowboy Bebop");

        AniListAnime anime2 = aniList.get(154587);

        assertEquals(anime2.title.english, "Frieren: Beyond Journey’s End");
    }

    @Test
    @DisplayName("AniList search by title")
    void testSearch() throws Exception {
        AniList aniList = new AniList();
        List<AniListAnime> result = aniList.search("Mobile Suit Gundam: The Witch from Mercury", 1, 20);

        assertEquals(result.get(0).title.english, "Mobile Suit Gundam: The Witch from Mercury");

        assertTrue(result.size() > 1);
    }

    @Test
    @DisplayName("AniList get airing schedule")
    void testAiring() throws Exception {
        AniList aniList = new AniList();

        AiringSchedule airingSchedule = aniList.airingSchedule();
    }

    @Test
    @DisplayName("AniList get next episode airing")
    void testEpisodeAiring() throws Exception {
        AniList aniList = new AniList();

        AiringSchedule.Detached airingSchedule = aniList.airingNextEpisode(166610);
    }
}
