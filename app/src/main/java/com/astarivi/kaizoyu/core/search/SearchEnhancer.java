package com.astarivi.kaizoyu.core.search;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.nibl.model.NiblResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchEnhancer implements Parcelable {
    public long id;
    public String type;
    public long kitsu_id;
    public String title;
    public String regex;
    public Integer episode;

    // Mandatory empty constructor for serialization
    public SearchEnhancer() {
    }

    @JsonIgnore
    public void filter(@NotNull List<NiblResult> niblResults) {
        if (type.equals("title")) return;

        Pattern pattern = Pattern.compile(regex);

        niblResults.removeIf(result -> {
            Matcher matcher = pattern.matcher(result.name);
            return !matcher.find();
        });
    }

    @JsonIgnore
    public static @Nullable SearchEnhancer fromJson(String json) {
        try {
            return JsonMapper.getObjectReader().readValue(json, SearchEnhancer.class);
        } catch (IOException e) {
            Logger.error("Failed to decode SearchEnhancer from kaizoyu.ddns.net");
            Logger.error(e);
            return null;
        }
    }

    // Parcelable

    @JsonIgnore
    private SearchEnhancer(Parcel parcel) {
        id = parcel.readLong();
        type = parcel.readString();
        kitsu_id = parcel.readLong();
        title = parcel.readString();
        regex = parcel.readString();
        episode = (Integer) parcel.readValue(Integer.class.getClassLoader());
    }

    @JsonIgnore
    public static final Parcelable.Creator<SearchEnhancer> CREATOR = new Parcelable.Creator<>() {
        @Override
        public SearchEnhancer createFromParcel(Parcel parcel) {
            return new SearchEnhancer(parcel);
        }

        @Override
        public SearchEnhancer[] newArray(int size) {
            return new SearchEnhancer[size];
        }
    };

    @JsonIgnore
    @Override
    public int describeContents() {
        return 0;
    }

    @JsonIgnore
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(type);
        dest.writeLong(kitsu_id);
        dest.writeString(title);
        dest.writeString(regex);
        dest.writeValue(episode);
    }
}