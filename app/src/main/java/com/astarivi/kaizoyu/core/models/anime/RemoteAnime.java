package com.astarivi.kaizoyu.core.models.anime;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import lombok.Getter;


/**
 * Represents a remote, Kitsu fetched anime. Contains full metadata. This is the main type
 * that should be used across the app.
 * <p>
 * This is backed by a single KitsuAnime internal structure.
 */
public class RemoteAnime extends AnimeBasicInfo {
    @Getter
    private final @NotNull KitsuAnime internal;

    public RemoteAnime(@NotNull KitsuAnime internal) {
        this.internal = internal;
    }

    @Override
    public long getKitsuId() {
        return internal.id;
    }

    @Override
    public @Nullable String getSubtype() {
        return internal.attributes.subtype;
    }

    @Override
    public @Nullable String getTitleJp() {
        return internal.attributes.titles.ja_jp;
    }

    @Override
    public @Nullable String getTitleEnJp() {
        return internal.attributes.titles.en_jp;
    }

    @Override
    public @Nullable String getTitleEn() {
        String en = internal.attributes.titles.en;

        if (en != null) return en;

        return internal.attributes.titles.en_us;
    }

    @Override
    public @Nullable String getSynopsis() {
        return internal.attributes.synopsis;
    }

    @Override
    public @Nullable String getImageURL(ImageType type, ImageSize size) {
        if (type == ImageType.COVER && internal.attributes.coverImage == null) return null;
        if (type == ImageType.POSTER && internal.attributes.posterImage == null) return null;

        return switch (size) {
            case TINY ->
                    type == ImageType.COVER ? internal.attributes.coverImage.tiny : internal.attributes.posterImage.tiny;
            case SMALL ->
                    type == ImageType.COVER ? internal.attributes.coverImage.small : internal.attributes.posterImage.small;
            case MEDIUM ->
                    type == ImageType.COVER ? internal.attributes.coverImage.medium : internal.attributes.posterImage.medium;
            case LARGE ->
                    type == ImageType.COVER ? internal.attributes.coverImage.large : internal.attributes.posterImage.large;
            case ORIGINAL ->
                    type == ImageType.COVER ? internal.attributes.coverImage.original : internal.attributes.posterImage.original;
        };
    }

    @Override
    public @Nullable String getImageURLorFallback(ImageType type, ImageSize size) {
        if (type == ImageType.COVER && internal.attributes.coverImage == null) return null;
        if (type == ImageType.POSTER && internal.attributes.posterImage == null) return null;

        String image = null;

        switch (size) {
            case ORIGINAL:
                image = type == ImageType.COVER ? internal.attributes.coverImage.original : internal.attributes.posterImage.original;
                if (image != null) break;
            case LARGE:
                image = type == ImageType.COVER ? internal.attributes.coverImage.large : internal.attributes.posterImage.large;
                if (image != null) break;
            case MEDIUM:
                image = type == ImageType.COVER ? internal.attributes.coverImage.medium : internal.attributes.posterImage.medium;
                if (image != null) break;
            case SMALL:
                image = type == ImageType.COVER ? internal.attributes.coverImage.small : internal.attributes.posterImage.small;
                if (image != null) break;
            case TINY:
                image = type == ImageType.COVER ? internal.attributes.coverImage.tiny : internal.attributes.posterImage.tiny;
                break;
        }

        return image;
    }

    @Override
    public @NotNull AnimeType getType() {
        return AnimeType.REMOTE;
    }

    // region Parcelable
    protected RemoteAnime(Parcel parcel) {
        try {
            internal = JsonMapper.deserializeGeneric(parcel.readString(), KitsuAnime.class);
        } catch (Exception e) {
            Logger.error("Failed to deserialize RemoteAnime. Fatal.");
            throw new RuntimeException(e);
        }
    }

    public static final Creator<RemoteAnime> CREATOR = new Creator<>() {
        @Override
        public RemoteAnime createFromParcel(Parcel parcel) {
            return new RemoteAnime(parcel);
        }

        @Override
        public RemoteAnime[] newArray(int size) {
            return new RemoteAnime[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        String serializedAnime;

        try {
            serializedAnime = JsonMapper.getObjectWriter().writeValueAsString(internal);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        dest.writeString(serializedAnime);
    }
    // endregion
}
