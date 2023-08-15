package com.astarivi.kaizoyu.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.astarivi.kaizoyu.core.video.VideoQuality;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import lombok.Getter;


@Getter
public class Result implements Parcelable {
    protected final NiblResult niblResult;
    protected final VideoQuality quality;
    protected final String fileExtension;
    protected final String cleanedFilename;
    protected final String botName;

    public Result(@NotNull NiblResult niblResult, String cleanedFilename, String fileExtension,
                  VideoQuality quality, String botName) {
        this.niblResult = niblResult;
        this.cleanedFilename = cleanedFilename;
        this.fileExtension = fileExtension;
        this.quality = quality;
        this.botName = botName;
    }

    public @NotNull String getXDCCCommand() {
        return String.format(
                Locale.ENGLISH,
                "%s :xdcc send #%d",
                botName,
                niblResult.number
        );
    }

    public @NotNull String getContents() {
        return String.format(
                Locale.ENGLISH,
                "%s %d %s",
                botName,
                niblResult.number,
                getCleanedFilename()
        );
    }

    // region Parcelable implementation

    protected Result(@NotNull Parcel parcel){
        ObjectMapper mapper = new ObjectMapper();
        try {
            niblResult = mapper.readValue(parcel.readString(), NiblResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        quality = VideoQuality.valueOf(parcel.readString());
        fileExtension = parcel.readString();
        cleanedFilename = parcel.readString();
        botName = parcel.readString();
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel parcel) {
            return new Result(parcel);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ObjectMapper mapper = new ObjectMapper();

        String serializedResult;

        try {
            serializedResult = mapper.writeValueAsString(niblResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        dest.writeString(serializedResult);

        dest.writeString(quality.name());
        dest.writeString(fileExtension);
        dest.writeString(cleanedFilename);
        dest.writeString(botName);
    }

    // endregion
}
