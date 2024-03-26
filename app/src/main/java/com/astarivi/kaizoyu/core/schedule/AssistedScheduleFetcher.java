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
import java.util.TreeMap;
import java.util.TreeSet;


@ThreadedOnly
public class AssistedScheduleFetcher {
    public static @Nullable SeasonalAnime getSingle(int animeId) throws AniListException, IOException {
        AniList aniList = new AniList();
        AiringSchedule.Episode airingEpisode = aniList.airingNextEpisode(animeId);

        if (airingEpisode == null) return null;

        return SeasonalAnime.fromAiringEpisode(airingEpisode);
    }

    public static TreeMap<DayOfWeek, TreeSet<SeasonalAnime>> getSchedule() throws AniListException, IOException {
        AniList aniList = new AniList();
        AiringSchedule airingSchedule = aniList.airingSchedule();

        return parse(airingSchedule);
    }

    private static @Nullable TreeMap<DayOfWeek, @NotNull TreeSet<SeasonalAnime>> parse(AiringSchedule scheduledAnime) {
        TreeMap<DayOfWeek, @NotNull TreeSet<SeasonalAnime>> result = new TreeMap<>();

        for (AiringSchedule.Episode airingEpisode :  scheduledAnime.episodes) {
            final SeasonalAnime seasonalAnime = SeasonalAnime.fromAiringEpisode(airingEpisode);

            if (seasonalAnime == null) continue;

            if (!seasonalAnime.getAniListAnime().subtype.equals("TV")) continue;

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
}
