package com.astarivi.kaizoyu.core.schedule;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.anime.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import okhttp3.HttpUrl;
import okhttp3.Request;


@ThreadedOnly
public class AssistedScheduleFetcher {
    private static final HttpUrl SCHEDULE_URL = new HttpUrl.Builder()
            .scheme("https")
            .host("raw.githubusercontent.com")
            .addPathSegments("astar-workspace/k.delivery/refs/heads/main/dist/schedule.json")
            .build();

    public static @Nullable SeasonalAnime getSingle(AnimeBasicInfo remoteAnime) throws IOException {
        RemoteSchedule[] remoteSchedules = fetchSchedule();

        for (RemoteSchedule remoteSchedule : remoteSchedules) {
            if (remoteAnime.getKitsuId() == remoteSchedule.id) {
                return remoteSchedule.toSeasonal();
            }
        }

        return null;
    }

    public static TreeMap<DayOfWeek, TreeSet<SeasonalAnime>> getSchedule() throws IOException {
        return fetchAndParse();
    }

    private static @Nullable TreeMap<DayOfWeek, @NotNull TreeSet<SeasonalAnime>> fetchAndParse() throws IOException {
        TreeMap<DayOfWeek, @NotNull TreeSet<SeasonalAnime>> result = new TreeMap<>();

        RemoteSchedule[] remoteSchedules = fetchSchedule();

        for (RemoteSchedule remoteSchedule : remoteSchedules) {
            final SeasonalAnime seasonalAnime = remoteSchedule.toSeasonal();

            // Ignore other types of shows
            if (!seasonalAnime.getInternal().attributes.subtype.equals("TV")) continue;

            DayOfWeek dow = seasonalAnime.getEmissionDay();

            TreeSet<SeasonalAnime> sAnime = result.get(dow);

            if (sAnime == null) {
                sAnime = new TreeSet<>();
                result.put(dow, sAnime);
            }

            sAnime.add(seasonalAnime);
        }

        if (result.isEmpty()) return null;

        return result;
    }

    @NonNull
    private static RemoteSchedule[] fetchSchedule() throws IOException {
        Request.Builder builder = new Request.Builder().url(SCHEDULE_URL);

        CommonHeaders.addTo(builder, CommonHeaders.TEXT_HEADERS);

        String result = HttpMethodsV2.executeRequest(
                builder.get().build()
        );

        return JsonMapper.deserializeGeneric(result, RemoteSchedule[].class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoteSchedule {
        public long id;
        public long airingAt;
        public int next_episode;
        public KitsuAnime data;

        /**
         * @noinspection unused
         */
        public RemoteSchedule() {
        }

        @JsonIgnore
        @NonNull
        @Contract(" -> new")
        SeasonalAnime toSeasonal() {
            Calendar calendarOfDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendarOfDate.setTimeInMillis(airingAt * 1000);

            ZonedDateTime airingTime = calendarOfDate
                    .toInstant()
                    .atZone(ZoneId.systemDefault());

            return new SeasonalAnime(
                    data,
                    airingTime,
                    next_episode
            );
        }
    }
}
