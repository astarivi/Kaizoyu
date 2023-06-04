package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBase;
import com.astarivi.kaizoyu.utils.Data;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


public class Anime extends AnimeBase implements Parcelable {
    protected final KitsuAnime anime;

    public Anime(@NonNull KitsuAnime anime) {
        this.anime = anime;
    }

    @Override
    public KitsuAnime getKitsuAnime() {
        return this.anime;
    }

    public String getDefaultTitle() {
        KitsuAnime.KitsuAnimeTitles titles = anime.attributes.titles;

        if (titles.en_jp != null) return titles.en_jp;
        if (titles.en != null) return titles.en;
        if (titles.en_us != null) return titles.en_us;
        return titles.ja_jp;
    }

    public String getDisplayTitle() {
        KitsuAnime.KitsuAnimeTitles titles = anime.attributes.titles;

        boolean preferEnglish = Boolean.parseBoolean(
                Data.getProperties(Data.CONFIGURATION.APP)
                        .getProperty("prefer_english", "true")
        );

        if (preferEnglish) {
            if (titles.en != null) return titles.en;
            if (titles.en_us != null) return titles.en_us;
            if (titles.en_jp != null) return titles.en_jp;
            return titles.ja_jp;
        }

        if (titles.en_jp != null) return titles.en_jp;
        if (titles.en != null) return titles.en;
        if (titles.en_us != null) return titles.en_us;
        return titles.ja_jp;
    }

    // region Parcelable implementation

    protected Anime(@NonNull Parcel parcel) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            anime = mapper.readValue(parcel.readString(), KitsuAnime.class);
        } catch (JsonProcessingException e) {
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
        ObjectMapper mapper = new ObjectMapper();

        String serializedAnime;

        try {
            serializedAnime = mapper.writeValueAsString(anime);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        dest.writeString(serializedAnime);
    }

    // endregion

    // region Builders

    public static class BulkAnimeBuilder {
        private final List<KitsuAnime> anime;

        public BulkAnimeBuilder(List<KitsuAnime> anime) {
            this.anime = anime;
        }

        public BulkAnimeBuilder addAnime(KitsuAnime anime) {
            this.anime.add(anime);
            return this;
        }

        public ArrayList<Anime> build() {
            ArrayList<Anime> result = new ArrayList<>();

            for (KitsuAnime kitsuAnime : this.anime) {
                result.add(
                        new Anime(kitsuAnime)
                );
            }

            return result;
        }
    }

    // endregion
}
