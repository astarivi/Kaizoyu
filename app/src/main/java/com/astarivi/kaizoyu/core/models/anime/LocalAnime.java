package com.astarivi.kaizoyu.core.models.anime;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;


/**
 * Represents a local anime, only containing crucial information to display this information
 * in offline mode. This is only present (and used) in the user lists.
 * <p>
 * This model is embedded into the database and any modification must be accompanied by a
 * database migration plan. Fields are public to allow modification of saved data (although this
 * should not be needed).
 */
@Getter
public class LocalAnime extends AnimeBasicInfo {
    public long kitsuId;
    @Ignore
    public int dbId = 0;
    @Ignore
    public LocalList localList = null;
    public String subtype;
    public String titleJp;
    public String titleEn;
    public String titleEnJp;
    public String synopsis;
    public String coverImgLink;
    public String posterImageLink;

    public LocalAnime(long kitsuId, LocalList localList, String subtype, String titleJp, String titleEn, String titleEnJp, String synopsis, String coverImgLink, String posterImageLink) {
        this.kitsuId = kitsuId;
        this.localList = localList;
        this.subtype = subtype;
        this.titleJp = titleJp;
        this.titleEn = titleEn;
        this.titleEnJp = titleEnJp;
        this.synopsis = synopsis;
        this.coverImgLink = coverImgLink;
        this.posterImageLink = posterImageLink;
    }

    @Override
    public @Nullable String getImageURL(ImageType type, ImageSize size) {
        switch (type) {
            case COVER -> {
                return coverImgLink;
            }
            case POSTER -> {
                return posterImageLink;
            }
        }

        return null;
    }

    @Override
    public @Nullable String getImageURLorFallback(ImageType type, ImageSize size) {
        return getImageURL(type, size);
    }

    @Override
    public @NotNull AnimeType getType() {
        return AnimeType.LOCAL;
    }

    // region Parcelable
    protected LocalAnime(@NonNull Parcel parcel) {
        kitsuId = parcel.readLong();
        dbId = parcel.readInt();
        subtype = parcel.readString();
        titleJp = parcel.readString();
        titleEn = parcel.readString();
        titleEnJp = parcel.readString();
        synopsis = parcel.readString();
        coverImgLink = parcel.readString();
        posterImageLink = parcel.readString();
    }

    public static final Creator<LocalAnime> CREATOR = new Creator<>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public LocalAnime createFromParcel(Parcel parcel) {
            return new LocalAnime(parcel);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public LocalAnime[] newArray(int size) {
            return new LocalAnime[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(kitsuId);
        dest.writeInt(dbId);
        dest.writeString(subtype);
        dest.writeString(titleJp);
        dest.writeString(titleEn);
        dest.writeString(titleEnJp);
        dest.writeString(synopsis);
        dest.writeString(coverImgLink);
        dest.writeString(posterImageLink);
    }
    // endregion
}
