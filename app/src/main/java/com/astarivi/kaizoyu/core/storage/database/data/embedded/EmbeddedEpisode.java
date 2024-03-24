package com.astarivi.kaizoyu.core.storage.database.data.embedded;

import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;


public class EmbeddedEpisode {
    public int kitsuId;
    public int parentKitsuId;
    public int episodeNumber;
    public int seasonNumber;
    public int relativeNumber;
    public int length;
    public int currentPosition;

    public EmbeddedEpisode(int kitsuId, int parentKitsuId, int episodeNumber, int seasonNumber,
                           int relativeNumber, int length, int currentPosition) {
        this.kitsuId = kitsuId;
        this.parentKitsuId = parentKitsuId;
        this.episodeNumber = episodeNumber;
        this.seasonNumber = seasonNumber;
        this.relativeNumber = relativeNumber;
        this.length = length;
        this.currentPosition = currentPosition;
    }

    @Deprecated
    public KitsuEpisode toKitsuEpisode() {
        return new KitsuEpisode.KitsuEpisodeBuilder(
                Integer.toString(kitsuId)
        ).setEpisodeNumber(
                episodeNumber
        ).setEpisodeSeasonNumber(
                seasonNumber
        ).setRelativeNumber(
                relativeNumber
        ).build(
        );
    }
}
