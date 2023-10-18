package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;
import com.astarivi.kaizoyu.core.storage.database.data.embedded.EmbeddedEpisode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.TreeSet;

import lombok.Getter;


@Getter
public class Episode implements Parcelable, Comparable<Episode> {
    protected final KitsuEpisode kitsuEpisode;
    protected final int animeId;

    public Episode(@NonNull KitsuEpisode kitsuEpisode, int animeId) {
        this.kitsuEpisode = kitsuEpisode;
        this.animeId = animeId;
    }

    public String getDefaultTitle() {
        KitsuEpisode.KitsuEpisodeTitles titles = kitsuEpisode.attributes.titles;

        if (titles.en != null) return titles.en;
        if (titles.en_us != null) return titles.en_us;
        if (titles.en_jp != null) return titles.en_jp;
        return titles.ja_jp;
    }

    public EmbeddedEpisode toEmbeddedDatabaseObject(int currentPosition) {
        KitsuEpisode kitsuEpisode = getKitsuEpisode();

        return new EmbeddedEpisode(
                Integer.parseInt(kitsuEpisode.id),
                this.animeId,
                kitsuEpisode.attributes.number,
                kitsuEpisode.attributes.seasonNumber != null ? kitsuEpisode.attributes.seasonNumber: 0,
                kitsuEpisode.attributes.relativeNumber != null ? kitsuEpisode.attributes.relativeNumber : 0,
                kitsuEpisode.attributes.length != null ? kitsuEpisode.attributes.length : 0,
                currentPosition
        );
    }

    // region Parcelable implementation

    protected Episode(@NonNull Parcel parcel) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            kitsuEpisode = mapper.readValue(parcel.readString(), KitsuEpisode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        animeId = parcel.readInt();
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
        ObjectMapper mapper = new ObjectMapper();

        String serializedEpisode;

        try {
            serializedEpisode = mapper.writeValueAsString(kitsuEpisode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        dest.writeString(serializedEpisode);
        dest.writeInt(animeId);
    }

    @Override
    public int compareTo(Episode episode) {
        return this.kitsuEpisode.attributes.number - episode.kitsuEpisode.attributes.number;
    }

    // endregion

    // region Builders

    public static class BulkEpisodeBuilder {
        private final List<KitsuEpisode> episode;
        private final int animeId;

        public BulkEpisodeBuilder(List<KitsuEpisode> episode, int animeId) {
            this.episode = episode;
            this.animeId = animeId;
        }

        public BulkEpisodeBuilder addEpisode(KitsuEpisode episode) {
            this.episode.add(episode);
            return this;
        }

        public TreeSet<Episode> build() {
            TreeSet<Episode> result = new TreeSet<>();

            for (KitsuEpisode kitsuEpisode : this.episode) {
                result.add(
                        new Episode(kitsuEpisode, animeId)
                );
            }

            return result;
        }
    }

    // endregion
}
