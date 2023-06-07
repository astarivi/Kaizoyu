package com.astarivi.kaizoyu.core.schedule;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.core.adapters.WebAdapter;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TreeMap;

import okhttp3.HttpUrl;


public class AssistedScheduleFetcher {
    public @Nullable TreeMap<DayOfWeek, @NotNull ArrayList<SeasonalAnime>> getSchedule() {
        String body = WebAdapter.getJSON(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("cdn.kaizoyu.ovh")
                        .addPathSegment("schedule.json")
                        .build()
        );

        if (body == null) return null;

        ScheduledAnime[] scheduledAnime;

        try {
            scheduledAnime = new ObjectMapper().readValue(body, ScheduledAnime[].class);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to decode schedule.json from cdn.kaizoyu.ovh");
            AnalyticsClient.onError("schedule_decode", "Failed to decode schedule.json from cdn.kaizoyu.ovh", e);
            return null;
        }

        return parse(scheduledAnime);
    }

    private @Nullable TreeMap<DayOfWeek, @NotNull ArrayList<SeasonalAnime>> parse(ScheduledAnime[] scheduledAnime) {
        TreeMap<DayOfWeek, @NotNull ArrayList<SeasonalAnime>> result = new TreeMap<>();

        for (ScheduledAnime anime :  scheduledAnime) {
            Calendar calendarOfDate = Calendar.getInstance(new Locale("en","UK"));
            calendarOfDate.setTimeInMillis(anime.timestamp * 1000);
            // Holy spaghetti
            DayOfWeek dow = calendarOfDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek();

            final SeasonalAnime seasonalAnime = new SeasonalAnime.SeasonalAnimeBuilder(anime.kitsu)
                    .setEmissionDay(dow)
                    .setHasAired(anime.aired)
                    .setCurrentEpisode(anime.episode)
                    .setEmissionTime(
                            calendarOfDate
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalTime()
                                    .format(
                                            DateTimeFormatter.ofPattern("hh:mm a")
                                    )
                    ).build();

            ArrayList<SeasonalAnime> sAnime = result.get(dow);

            if (sAnime == null) {
                sAnime = new ArrayList<>();
                result.put(dow, sAnime);
            }

            sAnime.add(seasonalAnime);
        }

        if (result.size() == 0) return null;

        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScheduledAnime {
        public int id;
        public String title;
        public long timestamp;
        public int episode;
        public boolean aired;
        public KitsuAnime kitsu;

        public ScheduledAnime() {
        }
    }
}
