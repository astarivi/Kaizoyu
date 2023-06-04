package com.astarivi.kaizoyu.core.video;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;


public class VideoParser {
    private final static String[] preferredBots = new String[]{"CR-HOLLAND|NEW", "CR-HOLLAND-IPv6|NEW", "CR-ARUTHA|NEW", "CR-ARUTHA-IPv6|NEW", "ARUTHA-BATCH|1080p", "ARUTHA-BATCH|720p", "ARUTHA-BATCH|SD"};
    private final static String[] fileTypes = new String[]{"mp4", "mkv", "avi", "mov", "ts", "vob", "flv", "wmv"};
    private final static String[] rejectedFileTypes = new String[]{"rar", "zip"};
    private static final TreeMap<String, VideoQuality> qualityMap = new TreeMap<String, VideoQuality>() {
        {
            put("360p", VideoQuality.SD);
            put("480p", VideoQuality.FSD);
            put("848x480", VideoQuality.FSD);
            put("540p", VideoQuality.ISD);
            put("720p", VideoQuality.HD);
            put("1280x720", VideoQuality.HD);
            put("bd720", VideoQuality.HD);
            put("1080p", VideoQuality.FHD);
            put("1920x1080", VideoQuality.FHD);
            put("2160p", VideoQuality.UHD);
            put("3840x2160", VideoQuality.UHD);
            put("dvd", VideoQuality.DVD);
            put("xvid", VideoQuality.DVD);
            put("dvix", VideoQuality.DVD);
        }};

    public static @NotNull VideoQuality qualityFromFilename(@Nullable String filename) {
        if (filename == null) return VideoQuality.UNKNOWN;

        filename = filename.toLowerCase();

        for (Map.Entry<String,VideoQuality> entry : qualityMap.entrySet()) {
            if (filename.contains(entry.getKey())){
                return entry.getValue();
            }
        }

        return VideoQuality.UNKNOWN;
    }

    public static @NotNull String extensionFromFilename(@NotNull String filename) {
        filename = filename.toLowerCase();

        if (filename.length() < 3) {
            return "Non-Video"; // Quite probably corrupt
        }

        String extension = filename.substring(filename.length() - 3);

        for (String fileType : fileTypes) {
            if (extension.contains(fileType)){
                return fileType;
            }
        }

        for (String fileType : rejectedFileTypes) {
            if (extension.contains(fileType)){
                return "Non-Video";
            }
        }

        return "Unknown";
    }

    public static @NotNull String cleanFilename(@NotNull String filename) {
        //Remove file extension
        if (filename.length() > 3) {
            filename = filename.substring(0, filename.length() - 3);
        }

        return filename.replaceAll("\\[.*?]","")
                .replace("_", " ")
                .replace(".", " ")
                .trim();
    }

    public static boolean isPreferredBot(String botName) {
        for (String bot : preferredBots) {
            if (botName.equals(bot)) {
                return true;
            }
        }

        return false;
    }
}
