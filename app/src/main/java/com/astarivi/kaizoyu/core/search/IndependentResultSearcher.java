package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.nibl.Nibl;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.models.Result;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class IndependentResultSearcher {
    private final Nibl nibl;

    public IndependentResultSearcher(UserHttpClient httpClient) {
        nibl = new Nibl(httpClient);
    }

    public IndependentResultSearcher(Nibl nibl) {
        this.nibl = nibl;
    }

    public @Nullable List<Result> searchEpisode(String title, int episode) {
        List<NiblResult> results = fetchEpisode(title, episode);

        if (results == null) return null;

        return SearchUtils.parseResults(results, nibl);
    }

    public @Nullable ArrayList<Result> searchEpisode(String title) {
        List<NiblResult> results = nibl.searchAnime(40, title);

        if (results == null) return null;

        return SearchUtils.parseResults(results, nibl);
    }

    public @Nullable List<NiblResult> fetchEpisode(String title, int episode) {
        if (episode > 999) {
            title = String.format(Locale.UK, "%s %d", title, episode);
            episode = -1;
        }

        return nibl.searchAnimeEpisode(40, title, episode);
    }
}
