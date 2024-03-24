package com.astarivi.kaizoyu.core.models.base;

import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizoyu.core.storage.database.data.embedded.EmbeddedAnime;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class AnimeBase {
    public abstract AniListAnime getAniListAnime();
    public abstract String getDefaultTitle();
    public abstract String getDisplayTitle();

    public EmbeddedAnime toEmbeddedDatabaseObject() {
        AniListAnime anime = getAniListAnime();

        return new EmbeddedAnime(
                Math.toIntExact(anime.id),
                anime.subtype,
                anime.title.japanese,
                anime.title.english,
                anime.title.romaji,
                anime.description,
                getImageUrlFromSize(ImageSize.TINY, true),
                getImageUrlFromSize(ImageSize.TINY, false)
        );
    }

    public @Nullable String getThumbnailUrl(boolean isCover) {
        if (Data.isDeviceLowSpec()) {
            return getImageUrlFromSize(ImageSize.TINY, isCover);
        } else {
            return getImageUrlFromSizeWithFallback(ImageSize.SMALL, isCover);
        }
    }

    // Note: banner means WIDE, cover means TALL
    public @Nullable String getImageUrlFromSize(@NotNull ImageSize size, boolean isBanner) {
        AniListAnime anime = getAniListAnime();

        // Handle banner
        if (isBanner && anime.bannerImage == null) return null;

        if (isBanner) {
            return anime.bannerImage;
        }

        if (anime.coverImage == null) return null;

        String image = null;

        switch (size) {
            // Tiny and small are deprecated. Perhaps we could introduce offline processing?
            case TINY:
            case SMALL:
            case MEDIUM:
                image = anime.coverImage.medium;
                break;
            case LARGE:
                image = anime.coverImage.large;
                break;
            case ORIGINAL:
                image = anime.coverImage.extraLarge;
                break;
        }

        return image;
    }

    public @Nullable String getImageUrlFromSizeWithFallback(@NotNull ImageSize size, boolean isBanner) {
        AniListAnime anime = getAniListAnime();

        // Handle banner
        if (isBanner && anime.bannerImage == null) return null;

        if (isBanner) {
            return anime.bannerImage;
        }

        if (anime.coverImage == null) return null;

        String image = null;

        switch (size) {
            case ORIGINAL:
                image = anime.coverImage.extraLarge;
                if (image != null) break;
            case LARGE:
                image = anime.coverImage.large;
                if (image != null) break;
            // Tiny and small are deprecated.
            case SMALL:
            case TINY:
            case MEDIUM:
                image = anime.coverImage.medium;
                break;
        }

        return image;
    }
}
