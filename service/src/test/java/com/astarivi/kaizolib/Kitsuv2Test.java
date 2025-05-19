package com.astarivi.kaizolib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizolib.kitsuv2.public_api.SearchParams;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


public class Kitsuv2Test {
    @Test
    @DisplayName("Kitsuv2 get by Anime ID")
    void testKitsuById() throws Exception {
        KitsuAnime anime = KitsuPublic.get(43806);
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
    @DisplayName("Kitsuv2 search (20 results limit)")
    void testKitsuSearch() throws Exception {
        List<KitsuAnime> anime = KitsuPublic.advancedSearch(
                new SearchParams()
                        .setTitle("Attack on Titan")
                        .setCustomParameter("sort", "popularityRank")
        );

        assertNotNull(anime);
        assertFalse(anime.isEmpty());
        assertEquals(20, anime.size());

        for (KitsuAnime individualAnime : anime) {
            assertNotNull(individualAnime);
        }
    }
}
