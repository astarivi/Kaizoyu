package com.astarivi.kaizoyu.core.models.anime;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;


public class AnimeMapper {
    public static @Nullable RemoteAnime tryUnwrap(@NotNull AnimeBasicInfo abi) {
        return switch (abi.getType()) {
            case SEASONAL, REMOTE -> (RemoteAnime) abi;
            default -> null;
        };
    }

    public static List<RemoteAnime> bulkRemoteFromKitsu(@NonNull List<KitsuAnime> anime) {
        return anime.stream()
                .map(AnimeMapper::remoteFromKitsu)
                .collect(Collectors.toList());
    }

    /**
     * Converts a KitsuAnime object to a RemoteAnime, by wrapping it.
     */
    @NonNull
    public static RemoteAnime remoteFromKitsu(@NonNull KitsuAnime kitsuAnime) {
        return new RemoteAnime(
                kitsuAnime
        );
    }

    /**
     * Fetches the RemoteAnime equivalent of given LocalAnime.
     * <p>
     * <b>Needs Internet connection, threaded only.<b/>
     */
    @NonNull
    @Contract("_ -> new")
    @ThreadedOnly
    public static RemoteAnime remoteFromLocal(@NonNull LocalAnime localAnime) throws KitsuException, ParsingError {
        return new RemoteAnime(
                KitsuPublic.get(localAnime.getKitsuId())
        );
    }

    /**
     * Creates a new LocalAnime from a RemoteAnime by discarding data.
     */
    @NonNull
    public static LocalAnime localFromRemote(@NonNull RemoteAnime remoteAnime, AnimeBasicInfo.LocalList localList) {
        return new LocalAnime(
                remoteAnime.getKitsuId(),
                localList,
                remoteAnime.getSubtype(),
                remoteAnime.getTitleJp(),
                remoteAnime.getTitleEn(),
                remoteAnime.getTitleEnJp(),
                remoteAnime.getSynopsis(),
                remoteAnime.getOptimizedImageURLFor(AnimeBasicInfo.ImageType.COVER),
                remoteAnime.getOptimizedImageURLFor(AnimeBasicInfo.ImageType.POSTER)
        );
    }

    @NonNull
    public static SavedAnime savedFromLocal(@NonNull LocalAnime localAnime) {
        return new SavedAnime(
                localAnime,
                System.currentTimeMillis()
        );
    }

    public static List<LocalAnime> bulkLocalFromSaved(@NonNull List<SavedAnime> sa) {
        return sa.stream()
                .map(AnimeMapper::localFromSaved)
                .collect(Collectors.toList());
    }

    @NonNull
    public static LocalAnime localFromSaved(@NonNull SavedAnime savedAnime) {
        LocalAnime local = savedAnime.anime;
        local.dbId = savedAnime.id;
        local.localList = AnimeBasicInfo.LocalList.fromValue(savedAnime.list);

        return local;
    }
}