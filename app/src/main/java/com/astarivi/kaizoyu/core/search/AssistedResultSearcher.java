package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.nibl.Nibl;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.adapters.WebAdapter;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;

import okhttp3.HttpUrl;


public class AssistedResultSearcher {

    @ThreadedOnly
    public static @Nullable List<Result> searchEpisode(SearchEnhancer searchEnhancer, int kitsuId, String title, int episode) {
        Nibl nibl = new Nibl(
                Data.getUserHttpClient()
        );

        IndependentResultSearcher independentResultSearcher = new IndependentResultSearcher(
                nibl
        );

        if (searchEnhancer == null) return independentResultSearcher.searchEpisode(title, episode);

        List<NiblResult> niblResults = independentResultSearcher.fetchEpisode(
                searchEnhancer.title,
                searchEnhancer.episode != null ? searchEnhancer.episode + episode : episode
        );

        if (niblResults == null) {
            Logger.error("Got no results after using enhanced search.");
            return null;
        }

        searchEnhancer.filter(niblResults);

        return SearchUtils.parseResults(niblResults, nibl);
    }

    @ThreadedOnly
    public static @Nullable SearchEnhancer getSearchEnhancer(int kitsuId) {
        if (kitsuId == -1) return null;

        String body = WebAdapter.getJSON(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("search.kaizoyu.ovh")
                        .addPathSegments("api/v1/xdcc/search")
                        .addPathSegment(String.valueOf(kitsuId))
                        .build()
        );

        if (body == null) return null;

        return SearchEnhancer.fromJson(body);
    }
}
