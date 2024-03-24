package com.astarivi.kaizoyu.core.schedule;

import com.astarivi.kaizolib.anilist.AniList;
import com.astarivi.kaizolib.anilist.exception.AniListException;
import com.astarivi.kaizolib.anilist.model.AiringSchedule;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.TreeMap;


@ThreadedOnly
public class AssistedScheduleFetcher {
    public static @Nullable SeasonalAnime getSingle(int animeId) throws AniListException, IOException {
        AniList aniList = new AniList();
        AiringSchedule.Episode airingEpisode = aniList.airingNextEpisode(animeId);

        if (airingEpisode == null) return null;

        return SeasonalAnime.fromAiringEpisode(airingEpisode);
    }

    public static TreeMap<DayOfWeek, ArrayList<SeasonalAnime>> getSchedule() throws AniListException, IOException {
        AniList aniList = new AniList();
        AiringSchedule airingSchedule = aniList.airingSchedule();

        return parse(airingSchedule);
    }

    private static @Nullable TreeMap<DayOfWeek, @NotNull ArrayList<SeasonalAnime>> parse(AiringSchedule scheduledAnime) {
        TreeMap<DayOfWeek, @NotNull ArrayList<SeasonalAnime>> result = new TreeMap<>();

        for (AiringSchedule.Episode airingEpisode :  scheduledAnime.episodes) {
            final SeasonalAnime seasonalAnime = SeasonalAnime.fromAiringEpisode(airingEpisode);

            if (seasonalAnime == null) continue;

            DayOfWeek dow = seasonalAnime.getEmissionDay();

            ArrayList<SeasonalAnime> sAnime = result.get(dow);

            if (sAnime == null) {
                sAnime = new ArrayList<>();
                result.put(dow, sAnime);
            }

            sAnime.add(seasonalAnime);
        }

        if (result.isEmpty()) return null;

        return result;
    }
}
