package com.astarivi.kaizolib.subsplease;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.common.util.ResponseToString;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseAnime;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseResult;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseTodayResult;
import com.astarivi.kaizolib.subsplease.parser.ParseJson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;


@Deprecated
public class SubsPlease {
    private final UserHttpClient client;

    public SubsPlease(UserHttpClient httpClient) {
        client = httpClient;
    }

    public SubsPlease() {
        client = UserHttpClient.getInstance();
    }

    public @Nullable List<SubsPleaseAnime> getAnimeAiringToday(@NotNull TimeZone tz) {
        String result = fetch(
                SubsPleaseUtils.buildTodayUrl(
                        tz.getID()
                )
        );

        if (result == null) return null;

        SubsPleaseTodayResult parsedResult = ParseJson.parseToday(result);

        if (parsedResult == null) return null;

        return parsedResult.schedule;
    }

    public @Nullable TreeMap<DayOfWeek, @Nullable List<SubsPleaseAnime>> getAiringAnime(@NotNull TimeZone tz) {
        String result = fetch(
                SubsPleaseUtils.buildEmissionUrl(
                        tz.getID()
                )
        );

        if (result == null) return null;

        SubsPleaseResult parsedResult = ParseJson.parse(result);

        if (parsedResult == null || parsedResult.schedule == null) return null;

        TreeMap<DayOfWeek, List<SubsPleaseAnime>> results = new TreeMap<>();

        for (DayOfWeek day : EnumSet.allOf(DayOfWeek.class)) {
            List<SubsPleaseAnime> animeForThisDay = parsedResult.schedule.getDay(day);

            if (animeForThisDay == null) {
                results.put(day, null);
                continue;
            }

            results.put(day, animeForThisDay);
        }

        return results;
    }

    public @Nullable String fetch(HttpUrl url) {
        Request.Builder getRequestBuilder = new Request.Builder();

        getRequestBuilder.url(url);
        getRequestBuilder.addHeader("Accept", "application/json");
        getRequestBuilder.addHeader("Content-Type", "application/json");

        Response response;

        try {
            response = client.executeRequest(
                    getRequestBuilder.build()
            );
        } catch (IOException e) {
            return null;
        }

        int responseCode = response.code();
        final String responseContent = ResponseToString.read(response);
        if (responseContent == null) {
            return null;
        }

        switch(responseCode) {
            case 304:
            case 200:
                return responseContent;
            default:
                Logger.error("Couldn't connect to SubsPlease, or the request was denied when fetching.");
                break;
        }

        return null;
    }
}
