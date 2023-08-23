package com.astarivi.kaizolib.kitsu;

import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethods;
import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;
import com.astarivi.kaizolib.kitsu.exception.NoResultsException;
import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsu.model.KitsuCategory;
import com.astarivi.kaizolib.kitsu.model.KitsuCategoriesResult;
import com.astarivi.kaizolib.kitsu.parser.ParseJson;

import org.tinylog.Logger;

import java.util.List;


public class KitsuRelations {
    private final UserHttpClient client;

    public KitsuRelations(UserHttpClient client) {
        this.client = client;
    }

    public KitsuRelations() {
        client = new UserHttpClient();
    }

    public List<KitsuCategory> getKitsuCategories(int kitsuId) throws
            NetworkConnectionException,
            NoResponseException,
            ParsingException,
            NoResultsException
    {
        String responseContent = HttpMethods.get(
                client,
                KitsuUtils.buildCategoriesUri(kitsuId),
                CommonHeaders.KITSU_HEADERS
        );

        KitsuCategoriesResult result = ParseJson.parseGeneric(responseContent, KitsuCategoriesResult.class);

        if (result.data == null || result.data.isEmpty()) {
            Logger.error("No results fetching Kitsu categories for anime id {}", kitsuId);
            throw new NoResultsException("No results found for this id");
        }

        return result.data;
    }
}
