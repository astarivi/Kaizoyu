package com.astarivi.kaizolib.anilist;

import com.astarivi.kaizolib.anilist.exception.AniListException;
import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.anilist.model.AiringSchedule;
import com.astarivi.kaizolib.common.exception.NoResponseException;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;


public class AniListSchedule extends AniListCommon {
    public static AiringSchedule airingSchedule() throws AniListException, IOException {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long weekStart = calendar.getTimeInMillis() / 1000;

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return airingSchedule(
                weekStart,
                calendar.getTimeInMillis() / 1000
        );
    }

    public static AiringSchedule airingSchedule(long weekStart, long weekEnd) throws AniListException, IOException {
        long page = 1;

        AiringSchedule airingSchedule = new AiringSchedule();

        while (true) {
            TreeMap<String, Object> variables = new TreeMap<>();
            variables.put("page", page);
            variables.put("week_start", weekStart);
            variables.put("week_end", weekEnd);

            GraphQLRequest graphQlContent = new GraphQLRequest(
                    AIRING_SCHEDULE_QUERY,
                    variables
            );

            String response = HttpMethodsV2.executeRequest(
                    getRequestFor(graphQlContent)
            );

            if (!airingSchedule.deserialize(response)) break;

            page++;
        }

        return airingSchedule;
    }

    public static @Nullable AiringSchedule.Episode nextAiringEpisodeFor(long aniListId) throws ParsingError, IOException {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        long timestampNow = calendar.getTimeInMillis() / 1000;

        TreeMap<String, Object> variables = new TreeMap<>();
        variables.put("media_id", aniListId);
        variables.put("start", timestampNow);

        GraphQLRequest graphQlContent = new GraphQLRequest(
                AIRING_ANIME_QUERY,
                variables
        );

        String response;
        try {
            response = HttpMethodsV2.executeRequest(
                    getRequestFor(graphQlContent)
            );
        } catch(NoResponseException e) {
            if (e.getMessage().equals("404")) {
                return null;
            }

            throw e;
        }

        return AiringSchedule.Episode.deserialize(response);
    }
}
