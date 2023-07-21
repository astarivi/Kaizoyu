package com.astarivi.kaizoyu.core.schedule;

import com.astarivi.kaizoyu.core.adapters.WebAdapter;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.HttpUrl;


public class AnimeScheduleChecker {
    public static @Nullable SeasonalAnime getSeasonalAnime(int animeId) {
        // Check if the anime is included in the schedule before fetching the schedule itself
        List<Integer> idsList = fetchIds();

        if (idsList == null) return null;

        if (!idsList.contains((Integer) animeId)) return null;

        // It is in the schedule, get the schedule and search it.
        AssistedScheduleFetcher.ScheduledAnime[] scheduledAnimeList = AssistedScheduleFetcher.getScheduledAnime();

        if (scheduledAnimeList == null) return null;

        for (AssistedScheduleFetcher.ScheduledAnime scheduledAnime :  scheduledAnimeList) {
            if (!scheduledAnime.kitsu.id.equals(String.valueOf(animeId))) continue;

            return scheduledAnime.toSeasonalAnime();
        }

        return null;
    }

    private static @Nullable List<Integer> fetchIds() {
        String body = WebAdapter.getJSON(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("cdn.kaizoyu.ovh")
                        .addPathSegment("schedule_ids.json")
                        .build()
        );

        if (body == null) return null;

        int[] ids;

        try {
            ids = new ObjectMapper().readValue(body, int[].class);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to decode schedule_ids.json from cdn.kaizoyu.ovh");
            AnalyticsClient.onError("schedule_decode", "Failed to decode schedule_ids.json from cdn.kaizoyu.ovh", e);
            return null;
        }

        return Arrays.stream(ids).boxed().collect(Collectors.toList());
    }
}
