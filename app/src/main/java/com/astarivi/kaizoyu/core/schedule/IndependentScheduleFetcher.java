package com.astarivi.kaizoyu.core.schedule;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.KitsuSearchParams;
import com.astarivi.kaizolib.kitsu.KitsuUtils;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizolib.subsplease.SubsPlease;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseAnime;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;


// FIXME: DEPRECATED
// ARCHIVED
public class IndependentScheduleFetcher {
    private final SubsPlease subsPlease;
    private final Kitsu kitsu;
    private final Properties scheduleRelation;

    public IndependentScheduleFetcher(UserHttpClient httpClient, Properties schedulePairs) {
        subsPlease = new SubsPlease(httpClient);
        kitsu = new Kitsu(httpClient);
        scheduleRelation = schedulePairs;
    }

    public @Nullable TreeMap<DayOfWeek, @Nullable List<SeasonalAnime>> getSchedule() {
        TimeZone localTz = TimeZone.getDefault();

        TreeMap<DayOfWeek, @Nullable List<SubsPleaseAnime>> subsPleaseAnime =
                subsPlease.getAiringAnime(localTz);

        if (subsPleaseAnime == null) return null;

        List<SubsPleaseAnime> animeToday = subsPlease.getAnimeAiringToday(localTz);

        return parse(subsPleaseAnime, animeToday);
    }

    // I present to you, the fabled monster of the unnecessary annotations:
    private @NotNull TreeMap<DayOfWeek, @Nullable List<SeasonalAnime>> parse(
            @NotNull TreeMap<DayOfWeek, @Nullable List<SubsPleaseAnime>> subsPleaseAnime,
            @Nullable List<SubsPleaseAnime> animeToday
    ) {
        TreeMap<DayOfWeek, List<SeasonalAnime>> results = new TreeMap<>();

        DayOfWeek dayToday = LocalDate.now().getDayOfWeek();

        for (DayOfWeek day : EnumSet.allOf(DayOfWeek.class)) {
            List<SubsPleaseAnime> subsPleaseAnimeList = subsPleaseAnime.get(day);

            if (day == dayToday && animeToday != null && !animeToday.isEmpty())
                subsPleaseAnimeList = animeToday;

            if (subsPleaseAnimeList == null || subsPleaseAnimeList.isEmpty()) {
                results.put(day, null);
                continue;
            }

            SeasonalAnime.BulkSeasonalAnimeBuilder seasonalAnimeBuilder = new SeasonalAnime.BulkSeasonalAnimeBuilder(day);

            for (SubsPleaseAnime spAnime : subsPleaseAnimeList) {
                KitsuAnime fetchedAnime;

                if (scheduleRelation.containsKey(spAnime.title)) {
                    int id = Integer.parseInt(
                            scheduleRelation.getProperty(spAnime.title)
                    );

                    fetchedAnime = getAnimeById(id);

                    if (fetchedAnime == null) {
                        scheduleRelation.remove(spAnime.title);
                        continue;
                    }
                } else {
                    fetchedAnime = getAnimeByTitle(spAnime.title);
                    if (fetchedAnime == null) {
                        continue;
                    }

//                    String fetchedTitle = fetchedAnime.attributes.titles.en_jp;
//                    double initialSimilarity = Utils.similarity(fetchedTitle, spAnime.title);
//
//                    if (initialSimilarity < 0.6 && !fetchedTitle.contains(spAnime.title)) {
//                        fetchedAnime = getAnimeByTitleAll(spAnime.title);
//                        if (initialSimilarity > Utils.similarity(fetchedAnime.attributes.titles.en_jp, spAnime.title)) {
//                            continue;
//                        }
//                    }

                    scheduleRelation.setProperty(spAnime.title, fetchedAnime.id);
                }

                spAnime.time = LocalTime.parse(
                        spAnime.time,
                        DateTimeFormatter.ofPattern("HH:mm")
                ).format(
                        DateTimeFormatter.ofPattern("hh:mm a")
                );

                Logger.debug(spAnime.title + " = " + fetchedAnime.attributes.canonicalTitle);
                seasonalAnimeBuilder.addPairs(fetchedAnime, spAnime);
            }

            results.put(day, seasonalAnimeBuilder.build());
        }

        return results;
    }

    private KitsuAnime getAnimeById(int id) {
        return this.kitsu.getAnimeById(id);
    }

    private KitsuAnime getAnimeByTitleAll(String title) {
        return this.kitsu.getAnime(
                new KitsuSearchParams().
                        setTitle(title)
        );
    }

    private KitsuAnime getAnimeByTitle(String title) {
        return this.kitsu.getAnime(
                new KitsuSearchParams().
                        setTitle(title).
                        setStatus(KitsuUtils.Status.CURRENT)
        );
    }
}
