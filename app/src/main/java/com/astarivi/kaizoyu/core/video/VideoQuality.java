package com.astarivi.kaizoyu.core.video;

import androidx.annotation.NonNull;


public enum VideoQuality {
    FHD ("1080p"),
    UHD ("2160p"),
    HD ("720p"),
    ISD ("540p"),
    FSD ("480p"),
    SD ("360p"),
    DVD ("DVD"),
    UNKNOWN ("Unknown");

    private final String quality;

    VideoQuality(String qt) {
        quality = qt;
    }

    @NonNull
    @Override
    public String toString() {
        return quality;
    }
}