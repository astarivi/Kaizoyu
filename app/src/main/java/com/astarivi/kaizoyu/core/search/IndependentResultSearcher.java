package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.nibl.Nibl;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Result;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class IndependentResultSearcher {
    @ThreadedOnly
    public static @Nullable List<Result> searchEpisode(String title, int episode) {
        List<NiblResult> results = fetchEpisode(title, episode);

        if (results == null) return null;

        return SearchUtils.parseResults(results);
    }

    @ThreadedOnly
    public static @Nullable ArrayList<Result> searchEpisode(String title) {
        List<NiblResult> results = Nibl.searchAnime(40, title);

        if (results == null) return null;

        return SearchUtils.parseResults(results);
    }

    @ThreadedOnly
    public static @Nullable List<NiblResult> fetchEpisode(String title, int episode) {
        if (episode > 999) {
            title = String.format(Locale.UK, "%s %d", title, episode);
            episode = -1;
        }

        return Nibl.searchAnimeEpisode(40, title, episode);
    }
}
