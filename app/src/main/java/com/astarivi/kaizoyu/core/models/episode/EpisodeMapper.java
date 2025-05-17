package com.astarivi.kaizoyu.core.models.episode;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.kitsuv2.model.KitsuEpisode;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisode;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class EpisodeMapper {
    public static List<RemoteEpisode> bulkRemoteFromKitsu(@NonNull List<KitsuEpisode> episodes, long animeKitsuId) {
        return episodes.stream()
                .map((a) -> EpisodeMapper.remoteFromKitsu(a, animeKitsuId))
                .collect(Collectors.toList());
    }

    public static Set<RemoteEpisode> bulkRemoteSetFromKitsu(@NonNull List<KitsuEpisode> episodes, long animeKitsuId) {
        return episodes.stream()
                .map((a) -> EpisodeMapper.remoteFromKitsu(a, animeKitsuId))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @NonNull
    public static RemoteEpisode remoteFromKitsu(@NonNull KitsuEpisode kitsuEpisode, long animeKitsuId) {
        return new RemoteEpisode(
                kitsuEpisode,
                animeKitsuId
        );
    }

    /**
     * Creates a new LocalEpisode from a RemoteEpisode by discarding data.
     */
    @NonNull
    public static LocalEpisode localFromRemote(@NonNull RemoteEpisode remoteEpisode, int position) {
        return new LocalEpisode(
                remoteEpisode.getKitsuId(),
                remoteEpisode.getAnimeKitsuId(),
                remoteEpisode.getNumber(),
                remoteEpisode.getInternal().attributes.length,
                position
        );
    }

    @NonNull
    public static LocalEpisode localFromSaved(@NonNull SavedEpisode saved) {
        LocalEpisode local = saved.episode;
        local.dbId = saved.id;
        return local;
    }

    @NonNull
    public static SavedEpisode savedFromLocal(@NonNull LocalEpisode saved) {
        return new SavedEpisode(
                saved,
                System.currentTimeMillis()
        );
    }
}
