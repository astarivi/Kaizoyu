package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.core.storage.database.data.embedded.EmbeddedEpisode;

import lombok.Getter;
import lombok.Setter;


@Getter
public class Episode implements Parcelable, Comparable<Episode> {
    protected final int animeId;
    protected int number;
    @Setter
    protected int length;

    public Episode(int animeId, int number, int length) {
        this.animeId = animeId;
        this.number = number;
        this.length = length;
    }

    public EmbeddedEpisode toEmbeddedDatabaseObject(int currentPosition) {
        return new EmbeddedEpisode(
                0,
                animeId,
                number,
                0,
                0,
                length,
                currentPosition
        );
    }

    // region Parcelable implementation

    protected Episode(@NonNull Parcel parcel) {
        animeId = parcel.readInt();
        number = parcel.readInt();
        length = parcel.readInt();
    }

    public static final Parcelable.Creator<Episode> CREATOR = new Parcelable.Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel parcel) {
            return new Episode(parcel);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(animeId);
        dest.writeInt(number);
        dest.writeInt(length);
    }

    @Override
    public int compareTo(Episode episode) {
        return number - episode.getNumber();
    }

    // endregion
}
