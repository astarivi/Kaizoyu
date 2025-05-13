package com.astarivi.kaizoyu.core.models.episode;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.model.KitsuEpisode;
import com.astarivi.kaizoyu.core.models.base.EpisodeBasicInfo;
import com.astarivi.kaizoyu.utils.Data;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Objects;

import lombok.Getter;


@Getter
public class RemoteEpisode extends EpisodeBasicInfo {
    private final long animeKitsuId;
    private final KitsuEpisode internal;

    public RemoteEpisode(KitsuEpisode internal, long animeKitsuId) {
        this.animeKitsuId = animeKitsuId;
        this.internal = internal;
    }

    @Override
    public long getKitsuId() {
        return internal.id;
    }

    @Override
    public int getNumber() {
        return internal.attributes.number;
    }

    @Override
    public int getLength() {
        return internal.attributes.length;
    }

    @Override
    public void setLength(int length) {
        internal.attributes.length = length;
    }

    @Override
    public EpisodeType getType() {
        return EpisodeType.REMOTE;
    }

    public @Nullable String getPreferredTitle() {
        boolean preferEnglish = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("prefer_english", true);

        if (preferEnglish) {
            final String englishTitle = internal.attributes.titles.en;

            if (englishTitle != null) return englishTitle;
        }

        final String romajiTitle = internal.attributes.titles.en_jp;

        if (romajiTitle != null) return romajiTitle;

        return internal.attributes.titles.ja_jp;
    }

    // region Parcelable
    protected RemoteEpisode(Parcel parcel) {
        animeKitsuId = parcel.readLong();

        try {
            internal = JsonMapper.deserializeGeneric(Objects.requireNonNull(parcel.readString()), KitsuEpisode.class);
        } catch (Exception e) {
            Logger.error("Failed to deserialize RemoteEpisode. Fatal.");
            throw new RuntimeException(e);
        }
    }

    public static final Creator<RemoteEpisode> CREATOR = new Creator<>() {
        @Override
        public RemoteEpisode createFromParcel(Parcel parcel) {
            return new RemoteEpisode(parcel);
        }

        @Override
        public RemoteEpisode[] newArray(int size) {
            return new RemoteEpisode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(animeKitsuId);

        String serializedEp;

        try {
            serializedEp = JsonMapper.getObjectWriter().writeValueAsString(internal);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        dest.writeString(serializedEp);
    }
    // endregion
}
