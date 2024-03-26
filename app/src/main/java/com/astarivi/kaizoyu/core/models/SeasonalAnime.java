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
public class SeasonalAnime extends Anime implements Comparable<SeasonalAnime> {
    private final ZonedDateTime airingDateTime;
    private int currentEpisode = -1;

    // region Parcelable implementation

    protected SeasonalAnime(Parcel parcel) {
        super(parcel);
        airingDateTime = ZonedDateTime.parse(parcel.readString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        currentEpisode = parcel.readInt();
    }

    public String getEmissionTime() {
        return airingDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public DayOfWeek getEmissionDay() {
        return airingDateTime.getDayOfWeek();
    }

    public boolean hasAired() {
        return ZonedDateTime.now().isAfter(airingDateTime);
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
        dest.writeString(airingDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        dest.writeInt(currentEpisode);
    }

    // endregion

    // region Builders

    private SeasonalAnime(@NotNull SeasonalAnimeBuilder builder) {
        super(builder.anime);
        this.airingDateTime = builder.airingDateTime;
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
                .setAiringDateTime(airingTime)
                .build();
    }

    @Override
    public int compareTo(SeasonalAnime seasonalAnime) {
        return this.airingDateTime.toLocalTime().compareTo(
                seasonalAnime.getAiringDateTime().toLocalTime()
        );
    }

    public static class SeasonalAnimeBuilder {
        private final AniListAnime anime;
        private ZonedDateTime airingDateTime;
        private int currentEpisode = -1;

        public SeasonalAnimeBuilder(AniListAnime anime) {
            this.anime = anime;
        }

        public SeasonalAnimeBuilder setCurrentEpisode(int episodeNumber) {
            this.currentEpisode = episodeNumber;
            return this;
        }

        public SeasonalAnimeBuilder setAiringDateTime(ZonedDateTime airingDateTime) {
            this.airingDateTime = airingDateTime;
            return this;
        }

        public @Nullable SeasonalAnime build() {
            if (airingDateTime == null) return null;

            return new SeasonalAnime(this);
        }

    }
    // endregion
}
