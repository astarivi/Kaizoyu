package com.astarivi.kaizoyu.core.models.base;

import android.os.Parcelable;

import androidx.annotation.NonNull;


public abstract class EpisodeBasicInfo implements Parcelable, Comparable<EpisodeBasicInfo> {
    public abstract long getKitsuId();
    public abstract long getAnimeKitsuId();
    public abstract int getNumber();
    public abstract int getLength();
    public abstract void setLength(int length);
    public abstract EpisodeType getType();

    @Override
    public int compareTo(@NonNull EpisodeBasicInfo o) {
        return getNumber() - o.getNumber();
    }

    public enum EpisodeType {
        LOCAL,
        REMOTE
    }
}
