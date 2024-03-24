package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizoyu.core.models.base.AnimeBase;
import com.astarivi.kaizoyu.utils.Data;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Anime extends AnimeBase implements Parcelable {
    protected final AniListAnime anime;

    public Anime(@NonNull AniListAnime anime) {
        this.anime = anime;
    }

    @Override
    public AniListAnime getAniListAnime() {
        return this.anime;
    }

    public String getDefaultTitle() {
        AniListAnime.Titles titles = anime.title;

        if (titles.romaji != null) return titles.romaji;
        if (titles.english != null) return titles.english;
        return titles.japanese;
    }

    public String getDisplayTitle() {
        AniListAnime.Titles titles = anime.title;

        boolean preferEnglish = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("prefer_english", true);

        if (preferEnglish) {
            if (titles.english != null) return titles.english;
            return titles.japanese;
        }

        return getDefaultTitle();
    }

    // region Parcelable implementation

    protected Anime(@NonNull Parcel parcel) {
        try {
            anime = JsonMapper.deserializeGeneric(
                    Objects.requireNonNull(parcel.readString()),
                    AniListAnime.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Creator<Anime> CREATOR = new Creator<Anime>() {
        @Override
        public Anime createFromParcel(Parcel parcel) {
            return new Anime(parcel);
        }

        @Override
        public Anime[] newArray(int size) {
            return new Anime[size];
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
            serializedAnime = JsonMapper.getObjectWriter().writeValueAsString(anime);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        dest.writeString(serializedAnime);
    }

    // endregion

    // region Builders

    public static class BulkAnimeBuilder {
        private final List<AniListAnime> anime;

        public BulkAnimeBuilder(List<AniListAnime> anime) {
            this.anime = anime;
        }

        public BulkAnimeBuilder addAnime(AniListAnime anime) {
            this.anime.add(anime);
            return this;
        }

        public ArrayList<Anime> build() {
            ArrayList<Anime> result = new ArrayList<>();

            for (AniListAnime aniListAnime : this.anime) {
                result.add(
                        new Anime(aniListAnime)
                );
            }

            return result;
        }
    }

    // endregion
}
