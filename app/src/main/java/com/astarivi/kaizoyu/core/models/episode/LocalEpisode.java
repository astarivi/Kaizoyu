package com.astarivi.kaizoyu.core.models.episode;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import com.astarivi.kaizoyu.core.models.base.EpisodeBasicInfo;

import org.jetbrains.annotations.Contract;

import lombok.Getter;
import lombok.Setter;


@Getter
public class LocalEpisode extends EpisodeBasicInfo {
    @Ignore
    public int dbId = 0;
    public long kitsuId;
    public long animeKitsuId;
    public int number;
    @Setter
    public int length;
    public int currentPosition;

    public LocalEpisode(long kitsuId, long animeKitsuId, int number, int length, int currentPosition) {
        this.kitsuId = kitsuId;
        this.animeKitsuId = animeKitsuId;
        this.number = number;
        this.length = length;
        this.currentPosition = currentPosition;
    }

    @Override
    public EpisodeType getType() {
        return EpisodeType.LOCAL;
    }

    // region Parcelable
    protected LocalEpisode(Parcel parcel) {
        kitsuId = parcel.readLong();
        animeKitsuId = parcel.readLong();
        number = parcel.readInt();
        length = parcel.readInt();
        currentPosition = parcel.readInt();
    }

    public static final Creator<LocalEpisode> CREATOR = new Creator<>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public LocalEpisode createFromParcel(Parcel parcel) {
            return new LocalEpisode(parcel);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public LocalEpisode[] newArray(int size) {
            return new LocalEpisode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(kitsuId);
        dest.writeLong(animeKitsuId);
        dest.writeInt(number);
        dest.writeInt(length);
        dest.writeInt(currentPosition);
    }
    // endregion
}
