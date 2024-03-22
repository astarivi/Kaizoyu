package com.astarivi.kaizolib.anilist;

import com.astarivi.kaizolib.anilist.base.AniListBase;
import com.astarivi.kaizolib.anilist.exception.AniListException;
import com.astarivi.kaizolib.anilist.model.AiringSchedule;
import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


public class AniList extends AniListBase {
    public @NotNull AniListAnime get(long id) throws AniListException, IOException {
        TreeMap<String, Long> variables = new TreeMap<>();
        variables.put("id", id);

        GraphQLRequest<Map<String, Long>> graphQlContent = new GraphQLRequest<>(
                ANIME_QUERY_BY_ID,
                variables
        );

        String response = HttpMethodsV2.executeRequest(
                getRequestFor(graphQlContent)
        );

        return AniListAnime.deserializeOne(response);
    }

    public @NotNull List<AniListAnime> search(String title, int page, int limit) throws AniListException, IOException {
        TreeMap<String, Object> variables = new TreeMap<>();
        variables.put("name", title);
        variables.put("page", page);
        variables.put("limit", limit);

        GraphQLRequest<Map<String, Object>> graphQlContent = new GraphQLRequest<>(
                ANIME_QUERY_SEARCH_TITLE,
                variables
        );

        String response = HttpMethodsV2.executeRequest(
                getRequestFor(graphQlContent)
        );

        return AniListAnime.deserializeMany(response);
    }

    public AiringSchedule airingSchedule() throws AniListException, IOException {
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

    public AiringSchedule airingSchedule(long weekStart, long weekEnd) throws AniListException, IOException {
        long page = 1;

        AiringSchedule airingSchedule = new AiringSchedule();

        while (true) {
            TreeMap<String, Long> variables = new TreeMap<>();
            variables.put("page", page);
            variables.put("week_start", weekStart);
            variables.put("week_end", weekEnd);

            GraphQLRequest<Map<String, Long>> graphQlContent = new GraphQLRequest<>(
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
}
