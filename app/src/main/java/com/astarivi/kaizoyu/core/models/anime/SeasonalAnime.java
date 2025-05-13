package com.astarivi.kaizoyu.core.models.anime;

import android.os.Parcel;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;


@Getter
public class SeasonalAnime extends RemoteAnime implements Comparable<SeasonalAnime> {
    private final ZonedDateTime airingDateTime;
    private int currentEpisode = -1;

    public String getEmissionTime() {
        return airingDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public DayOfWeek getEmissionDay() {
        return airingDateTime.getDayOfWeek();
    }

    public boolean hasAired() {
        return ZonedDateTime.now().isAfter(airingDateTime);
    }

    @Override
    public @NotNull AnimeType getType() {
        return AnimeType.SEASONAL;
    }

    // region Parcelable
    protected SeasonalAnime(Parcel parcel) {
        super(parcel);
        airingDateTime = ZonedDateTime.parse(parcel.readString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        currentEpisode = parcel.readInt();
    }

    public static final Creator<SeasonalAnime> CREATOR = new Creator<>() {
        @NonNull
        @Contract("_ -> new")
        @Override
        public SeasonalAnime createFromParcel(Parcel parcel) {
            return new SeasonalAnime(parcel);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public SeasonalAnime[] newArray(int size) {
            return new SeasonalAnime[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(airingDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        dest.writeInt(currentEpisode);
    }
    // endregion

    @Override
    public int compareTo(@NonNull SeasonalAnime o) {
        return this.airingDateTime.toLocalTime().compareTo(
                o.getAiringDateTime().toLocalTime()
        );
    }
}
