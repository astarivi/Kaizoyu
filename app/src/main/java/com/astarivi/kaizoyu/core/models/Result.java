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


public class Result implements Parcelable {
    protected final NiblResult result;
    protected final VideoQuality quality;
    protected final String fileExtension;
    protected final String cleanedFilename;
    protected final String botName;

    public Result(@NotNull NiblResult result, String cleanedFilename, String fileExtension,
                  VideoQuality quality, String botName) {
        this.result = result;
        this.cleanedFilename = cleanedFilename;
        this.fileExtension = fileExtension;
        this.quality = quality;
        this.botName = botName;
    }

    public NiblResult getNiblResult() {
        return result;
    }

    public String getBotName() {
        return botName;
    }

    public String getCleanedFilename() {
        return cleanedFilename;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public VideoQuality getQuality() {
        return quality;
    }

    public @NotNull String getXDCCCommand() {
        return String.format(
                Locale.ENGLISH,
                "%s :xdcc send #%d",
                botName,
                result.number
        );
    }

    public @NotNull String getContents() {
        return String.format(
                Locale.ENGLISH,
                "%s %d %s",
                botName,
                result.number,
                getCleanedFilename()
        );
    }

    // region Parcelable implementation

    protected Result(@NotNull Parcel parcel){
        ObjectMapper mapper = new ObjectMapper();
        try {
            result = mapper.readValue(parcel.readString(), NiblResult.class);
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
            serializedResult = mapper.writeValueAsString(result);
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
