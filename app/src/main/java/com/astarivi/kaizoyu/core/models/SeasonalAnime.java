package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseAnime;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;


public class SeasonalAnime extends Anime {
    private final String emissionTime;
    private final DayOfWeek emissionDay;
    private final boolean hasAired;

    public DayOfWeek getEmissionDay() {
        return emissionDay;
    }

    public String getEmissionTime() {
        return emissionTime;
    }

    public boolean hasAired() {
        return hasAired;
    }

    // region Parcelable implementation

    protected SeasonalAnime(Parcel parcel) {
        super(parcel);
        emissionTime = parcel.readString();
        emissionDay = DayOfWeek.of(parcel.readInt());
        hasAired = parcel.readByte() != 0;
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
    }

    // endregion

    // region Builders

    private SeasonalAnime(@NotNull SeasonalAnimeBuilder builder) {
        super(builder.anime);
        this.emissionTime = builder.emissionTime;
        this.emissionDay = builder.emissionDay;
        this.hasAired = builder.hasAired;
    }

    private SeasonalAnime(@NotNull AdditionalSeasonalAnimeBuilder builder) {
        super(builder.anime);
        this.emissionTime = builder.subsPleaseAnime.time;
        this.emissionDay = builder.emissionDay;
        this.hasAired = builder.subsPleaseAnime.aired != null && builder.subsPleaseAnime.aired;
    }

    public static class SeasonalAnimeBuilder {
        private final KitsuAnime anime;
        private String emissionTime;
        private DayOfWeek emissionDay;
        private boolean hasAired;

        public SeasonalAnimeBuilder(KitsuAnime anime) {
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

        public @Nullable SeasonalAnime build() {
            if (emissionTime == null || emissionDay == null) return null;

            return new SeasonalAnime(this);
        }
    }

    public static class AdditionalSeasonalAnimeBuilder {
        private KitsuAnime anime;
        private SubsPleaseAnime subsPleaseAnime;
        private final DayOfWeek emissionDay;

        public AdditionalSeasonalAnimeBuilder(DayOfWeek emissionDay) {
            this.emissionDay = emissionDay;
        }

        public AdditionalSeasonalAnimeBuilder setKitsuAnime(KitsuAnime anime) {
            this.anime = anime;
            return this;
        }

        public AdditionalSeasonalAnimeBuilder setSubsPleaseAnime(SubsPleaseAnime subsPleaseAnime) {
            this.subsPleaseAnime = subsPleaseAnime;
            return this;
        }

        public @Nullable SeasonalAnime build() {
            if (subsPleaseAnime == null || anime == null) return null;

            return new SeasonalAnime(this);
        }
    }

    public static class BulkSeasonalAnimeBuilder {
        private final DayOfWeek emissionDay;
        private final List<Pair<KitsuAnime, SubsPleaseAnime>> anime = new ArrayList<>();

        public BulkSeasonalAnimeBuilder(DayOfWeek day) {
            emissionDay = day;
        }

        public void addPairs(KitsuAnime kitsuAnime, SubsPleaseAnime subsPleaseAnime) {
            anime.add(
                    new Pair<>(kitsuAnime, subsPleaseAnime)
            );
        }

        public @Nullable List<SeasonalAnime> build() {
            if (anime.isEmpty()) return null;

            List<SeasonalAnime> results = new ArrayList<>();

            for (Pair<KitsuAnime, SubsPleaseAnime> pair : anime) {
                results.add(
                        new AdditionalSeasonalAnimeBuilder(emissionDay)
                                .setKitsuAnime(pair.first)
                                .setSubsPleaseAnime(pair.second)
                                .build()
                );
            }

            return results;
        }
    }

    // endregion
}
