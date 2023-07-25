package com.astarivi.kaizoyu.core.models.base;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.core.storage.database.data.embedded.EmbeddedAnime;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class AnimeBase {
    public abstract KitsuAnime getKitsuAnime();
    public abstract String getDefaultTitle();
    public abstract String getDisplayTitle();

    public EmbeddedAnime toEmbeddedDatabaseObject() {
        KitsuAnime kitsuAnime = getKitsuAnime();

        return new EmbeddedAnime(
                Integer.parseInt(kitsuAnime.id),
                kitsuAnime.attributes.subtype,
                kitsuAnime.attributes.titles.ja_jp,
                kitsuAnime.attributes.titles.en,
                kitsuAnime.attributes.titles.en_jp,
                kitsuAnime.attributes.synopsis,
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

    public @Nullable String getImageUrlFromSize(@NotNull ImageSize size, boolean isCover) {
        KitsuAnime anime = getKitsuAnime();

        if (isCover && anime.attributes.coverImage == null) return null;
        if (!isCover && anime.attributes.posterImage == null) return null;

        String image = null;

        switch (size) {
            case TINY:
                image = isCover ? anime.attributes.coverImage.tiny : anime.attributes.posterImage.tiny;
                break;
            case SMALL:
                image = isCover ? anime.attributes.coverImage.small : anime.attributes.posterImage.small;
                break;
            case MEDIUM:
                image = isCover ? anime.attributes.coverImage.medium : anime.attributes.posterImage.medium;
                break;
            case LARGE:
                image = isCover ? anime.attributes.coverImage.large : anime.attributes.posterImage.large;
                break;
            case ORIGINAL:
                image = isCover ? anime.attributes.coverImage.original : anime.attributes.posterImage.original;
                break;
        }

        return image;
    }

    public @Nullable String getImageUrlFromSizeWithFallback(@NotNull ImageSize size, boolean isCover) {
        KitsuAnime anime = getKitsuAnime();

        if (isCover && anime.attributes.coverImage == null) return null;
        if (!isCover && anime.attributes.posterImage == null) return null;

        String image = null;

        switch (size) {
            case ORIGINAL:
                image = isCover ? anime.attributes.coverImage.original : anime.attributes.posterImage.original;
                if (image != null) break;
            case LARGE:
                image = isCover ? anime.attributes.coverImage.large : anime.attributes.posterImage.large;
                if (image != null) break;
            case MEDIUM:
                image = isCover ? anime.attributes.coverImage.medium : anime.attributes.posterImage.medium;
                if (image != null) break;
            case SMALL:
                image = isCover ? anime.attributes.coverImage.small : anime.attributes.posterImage.small;
                if (image != null) break;
            case TINY:
                image = isCover ? anime.attributes.coverImage.tiny : anime.attributes.posterImage.tiny;
                break;
        }

        return image;
    }

    public @Nullable ImageSize getSizeWithFallback(@NotNull ImageSize size, boolean isCover) {
        KitsuAnime anime = getKitsuAnime();

        if (isCover && anime.attributes.coverImage == null) return null;
        if (!isCover && anime.attributes.posterImage == null) return null;

        ImageSize image = null;
        String url;

        switch (size) {
            case ORIGINAL:
                url = isCover ? anime.attributes.coverImage.original : anime.attributes.posterImage.original;
                image = ImageSize.ORIGINAL;
                if (url != null) break;
            case LARGE:
                url = isCover ? anime.attributes.coverImage.large : anime.attributes.posterImage.large;
                image = ImageSize.LARGE;
                if (url != null) break;
            case MEDIUM:
                url = isCover ? anime.attributes.coverImage.medium : anime.attributes.posterImage.medium;
                image = ImageSize.MEDIUM;
                if (url != null) break;
            case SMALL:
                url = isCover ? anime.attributes.coverImage.small : anime.attributes.posterImage.small;
                image = ImageSize.MEDIUM;
                if (url != null) break;
            case TINY:
                image = ImageSize.TINY;
                url = isCover ? anime.attributes.coverImage.tiny : anime.attributes.posterImage.tiny;
                if (url == null) image = null;
                break;
        }

        return image;
    }
}
