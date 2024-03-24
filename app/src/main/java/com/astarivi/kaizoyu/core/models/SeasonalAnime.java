package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizolib.anilist.model.AiringSchedule;
import com.astarivi.kaizolib.anilist.model.AniListAnime;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

import lombok.Getter;


@Getter
public class SeasonalAnime extends Anime {
    private final String emissionTime;
    private final DayOfWeek emissionDay;
    private final boolean hasAired;
    private int currentEpisode = -1;

    // region Parcelable implementation

    protected SeasonalAnime(Parcel parcel) {
        super(parcel);
        emissionTime = parcel.readString();
        emissionDay = DayOfWeek.of(parcel.readInt());
        hasAired = parcel.readByte() != 0;
        currentEpisode = parcel.readInt();
    }

    public static final Creator<SeasonalAnime> CREATOR = new Creator<SeasonalAnime>() {
        @Override
        public SeasonalAnime createFromParcel(Parcel parcel) {
            return new SeasonalAnime(parcel);
        }

        @Override
        public SeasonalAnime[] newArray(int size) {
            return new SeasonalAnime[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(emissionTime);
        dest.writeInt(emissionDay.getValue());
        dest.writeByte((byte) (hasAired ? 1 : 0));
        dest.writeInt(currentEpisode);
    }

    // endregion

    // region Builders

    private SeasonalAnime(@NotNull SeasonalAnimeBuilder builder) {
        super(builder.anime);
        this.emissionTime = builder.emissionTime;
        this.emissionDay = builder.emissionDay;
        this.hasAired = builder.hasAired;
        this.currentEpisode = builder.currentEpisode;
    }

    public static SeasonalAnime fromAiringEpisode(AiringSchedule.Episode airingEpisode) {
        Calendar calendarOfDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendarOfDate.setTimeInMillis(airingEpisode.airingAt * 1000);

        ZonedDateTime airingTime = calendarOfDate
                .toInstant()
                .atZone(ZoneId.systemDefault());

        return new SeasonalAnime.SeasonalAnimeBuilder(airingEpisode.media)
                .setCurrentEpisode(airingEpisode.episode)
                .setEmissionDay(
                        airingTime
                                .toLocalDate()
                                .getDayOfWeek()
                )
                .setEmissionTime(
                        airingTime
                                .toLocalTime()
                                .format(
                                        DateTimeFormatter.ofPattern("hh:mm a")
                                )
                )
                .setHasAired(
                        ZonedDateTime
                                .now()
                                .isAfter(airingTime)
                )
                .build();
    }

    public static class SeasonalAnimeBuilder {
        private final AniListAnime anime;
        private String emissionTime;
        private DayOfWeek emissionDay;
        private boolean hasAired;
        private int currentEpisode = -1;

        public SeasonalAnimeBuilder(AniListAnime anime) {
            this.anime = anime;
        }

        public SeasonalAnimeBuilder setEmissionTime(String emissionTime) {
            this.emissionTime = emissionTime;
            return this;
        }

        public SeasonalAnimeBuilder setEmissionDay(DayOfWeek emissionDay) {
            this.emissionDay = emissionDay;
            return this;
        }

        public SeasonalAnimeBuilder setHasAired(boolean hasAired) {
            this.hasAired = hasAired;
            return this;
        }

        public SeasonalAnimeBuilder setCurrentEpisode(int episodeNumber) {
            this.currentEpisode = episodeNumber;
            return this;
        }

        public @Nullable SeasonalAnime build() {
            if (emissionTime == null || emissionDay == null) return null;

            return new SeasonalAnime(this);
        }
    }
    // endregion
}
