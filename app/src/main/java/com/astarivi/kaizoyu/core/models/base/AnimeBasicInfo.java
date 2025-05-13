package com.astarivi.kaizoyu.core.models.base;

import android.os.Parcelable;

import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;

import lombok.Getter;


public abstract class AnimeBasicInfo implements Parcelable {
    public abstract long getKitsuId();
    public abstract @Nullable String getSubtype();
    public abstract @Nullable String getTitleJp();
    public abstract @Nullable String getTitleEnJp();
    public abstract @Nullable String getTitleEn();
    public abstract @Nullable String getSynopsis();
    public abstract @NotNull AnimeType getType();
    public abstract @Nullable String getImageURL(ImageType type, ImageSize size);
    /**
     * Try to get the requested image size. If not found, try to fall back to a smaller size that's
     * available.
     */
    public abstract @Nullable String getImageURLorFallback(ImageType type, ImageSize size);
    public @Nullable String getOptimizedImageURLFor(ImageType type) {
        if (Data.isDeviceLowSpec()) {
            return getImageURL(type, ImageSize.TINY);
        } else {
            return getImageURLorFallback(type, ImageSize.SMALL);
        }
    }

    public @NotNull String getPreferredTitle() {
        boolean preferEnglish = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("prefer_english", true);

        if (preferEnglish) {
            final String englishTitle = getTitleEn();

            if (englishTitle != null) return englishTitle;
        }

        final String romajiTitle = getTitleEnJp();

        if (romajiTitle != null) return romajiTitle;

        final String jpTitle = getTitleJp();

        if (jpTitle == null) {
            Logger.error("Kitsu id {} has no usable titles.", getKitsuId());
            return "No title (Unknown)";
        }

        return jpTitle;
    }

    public @NotNull ArrayList<@NotNull String> getAllTitles() {
        ArrayList<String> titles = new ArrayList<>(3);

        if (getTitleEnJp() != null && !getTitleEnJp().isEmpty()) {
            titles.add(getTitleEnJp());
        }

        if (getTitleEn() != null && !getTitleEn().isEmpty()) {
            titles.add(getTitleEn());
        }

        if (getTitleJp() != null && !getTitleJp().isEmpty()) {
            titles.add(getTitleJp());
        }

        return titles;
    }

    public enum ImageType {
        /**
         * A cover image, wide.
         */
        COVER,
        /**
         * A poster image, tall.
         */
        POSTER
    }

    public enum ImageSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        ORIGINAL
    }

    public enum AnimeType {
        LOCAL,
        REMOTE,
        SEASONAL
    }

    @Getter
    public enum LocalList {
        WATCH_LATER(0),
        WATCHING(1),
        FINISHED(2),
        NOT_TRACKED(3);

        private final int value;

        LocalList(int i) {
            value = i;
        }

        public static LocalList fromValue(int value) {
            return LocalList.values()[value];
        }
    }
}
