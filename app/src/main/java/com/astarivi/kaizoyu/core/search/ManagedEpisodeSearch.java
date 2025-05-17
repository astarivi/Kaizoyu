package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;


/**
 * Exports managed episode searching methods to use everywhere.
 */
public class ManagedEpisodeSearch {
    @ThreadedOnly
    public static @Nullable List<Result> search(AnimeBasicInfo anime, int episode, @Nullable SearchEnhancer se) {
        if (se == null) {
            // Let's try to search by all titles ourselves.
            // Order will be Romaji -> English -> Japanese
            for (String title : anime.getAllTitles()) {
                List<Result> result = IndependentResultSearcher.searchEpisode(title, episode);

                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }

            return null;
        }

        List<NiblResult> niblResults = IndependentResultSearcher.fetchEpisode(
                se.title,
                se.episode != null ? se.episode + episode : episode
        );

        if (niblResults == null) {
            Logger.error("Got no results after using enhanced search.");
            return null;
        }

        se.filter(niblResults);

        return SearchUtils.parseResults(niblResults);
    }
}
