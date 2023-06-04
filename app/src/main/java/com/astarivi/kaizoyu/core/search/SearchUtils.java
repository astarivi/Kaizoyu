package com.astarivi.kaizoyu.core.search;

import com.astarivi.kaizolib.nibl.Nibl;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.video.VideoParser;
import com.astarivi.kaizoyu.core.video.VideoQuality;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class SearchUtils {
    public static @Nullable ArrayList<Result> parseResults (
            @NotNull List<NiblResult> results,
            @NotNull Nibl nibl)
    {
        ArrayList<Result> parsedResults = new ArrayList<>();
        Properties botsMap = Data.getProperties(Data.CONFIGURATION.BOTS);

        // Filters
        boolean ipv6Capable = Boolean.parseBoolean(
                Data.getProperties(Data.CONFIGURATION.APP)
                        .getProperty("show_ipv6", "false")
        );

        for (NiblResult result : results) {
            // Don't show empty results
            if (result.name == null || result.name.equals("")) continue;

            // Filters
            String botName = botsMap.getProperty(
                    String.valueOf(result.botId),
                    null
            );

            if (botName == null) {
                nibl.getBotsMap(botsMap);
                if (!botsMap.containsKey(String.valueOf(result.botId))) continue;

                Data.saveProperties(Data.CONFIGURATION.BOTS);

                botName = botsMap.getProperty(
                        String.valueOf(result.botId)
                );
            }

            if (!ipv6Capable && botName.toLowerCase().contains("ipv6")) continue;

            String fileExtension = VideoParser.extensionFromFilename(result.name);

            // Don't show unplayable results.
            if (fileExtension.equals("Unknown") || fileExtension.equals("Non-Video")) continue;

            VideoQuality quality = VideoParser.qualityFromFilename(result.name);

            String cleanedFilename = VideoParser.cleanFilename(result.name);

            parsedResults.add(
                    new Result(
                            result,
                            cleanedFilename,
                            fileExtension,
                            quality,
                            botName
                    )
            );
        }

        if (parsedResults.isEmpty()) return null;

        return parsedResults;
    }
}
