package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.video.VideoQuality;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class Organizer {
    public static @NotNull TreeMap<VideoQuality, List<Result>> organizeResultsByQuality(@NotNull List<Result> results) {
        TreeMap<VideoQuality, List<Result>> resultingMap = new TreeMap<>();

        for (Result result : results) {
            VideoQuality quality = result.getQuality();

            if (!resultingMap.containsKey(quality)) resultingMap.put(quality, new ArrayList<>());

            List<Result> currentList = resultingMap.get(quality);

            if (currentList == null) continue;

            currentList.add(result);
        }

        return resultingMap;
    }

    public static @NotNull TreeMap<String, Result> organizeResultsByClearTitle(@NotNull List<Result> results) {
        TreeMap<String, Result> resultingMap = new TreeMap<>();

        for (Result result : results) {
            String clearTitle = String.format(
                    "%s : %s | %s",
                    result.getBotName(),
                    result.getNiblResult().size,
                    result.getCleanedFilename()
            );

            resultingMap.put(clearTitle, result);
        }

        return resultingMap;
    }
}
