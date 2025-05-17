package com.astarivi.kaizoyu.core.storage.database.repo;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.LocalAnime;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.models.base.EpisodeBasicInfo;
import com.astarivi.kaizoyu.core.models.episode.EpisodeMapper;
import com.astarivi.kaizoyu.core.models.episode.LocalEpisode;
import com.astarivi.kaizoyu.core.models.episode.RemoteEpisode;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisode;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisodeDao;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Objects;

import lombok.Getter;

public class SavedShowRepo {
    @Getter
    private final static SavedAnimeDao animeDao = PersistenceRepository.getInstance().getDatabase().savedAnimeDao();
    @Getter
    private final static SavedEpisodeDao episodeDao = PersistenceRepository.getInstance().getDatabase().savedEpisodeDao();

    private static void createOrUpdate(@NonNull SavedAnime sa) {
        if (sa.id <= 0) {
            if (animeDao.insert(sa) != -1L) return;
        }

        animeDao.update(sa);
    }

    private static void createOrUpdate(@NonNull SavedEpisode sa) {
        if (sa.id <= 0) {
            if (episodeDao.insert(sa) != -1L) return;
        }

        episodeDao.update(sa);
    }

    @ThreadedOnly
    public static @Nullable AnimeBasicInfo.LocalList getLocalListFrom(long kitsuId) {
        SavedAnime sa = animeDao.getByKitsuId(kitsuId);

        if (sa == null) return null;

        return AnimeBasicInfo.LocalList.fromValue(sa.list);
    }

    @ThreadedOnly
    public static void delete(AnimeBasicInfo anime) {
        if (anime instanceof LocalAnime la && la.dbId != 0) {
            animeDao.delete(la.dbId);
            return;
        }

        animeDao.deleteByKitsuId(anime.getKitsuId());

        Data.getTemporarySwitches().setPendingFavoritesRefresh(true);
        Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);
    }

    @ThreadedOnly
    public static void createOrUpdate(AnimeBasicInfo info, AnimeBasicInfo.LocalList localList) {
        SavedAnime savedAnime;

        if (info instanceof RemoteAnime remoteAnime) {
            SavedAnime sa = animeDao.getByKitsuId(info.getKitsuId());
            savedAnime = Objects.requireNonNullElseGet(sa, () -> AnimeMapper.savedFromLocal(
                    AnimeMapper.localFromRemote(remoteAnime, localList)
            ));
        } else if (info instanceof LocalAnime la) {
            savedAnime = AnimeMapper.savedFromLocal(la);
        } else {
            throw new IllegalStateException("This AnimeBasicInfo instance has no valid type to cast to.");
        }
        savedAnime.list = localList.getValue();

        createOrUpdate(savedAnime);

        Data.getTemporarySwitches().setPendingFavoritesRefresh(true);
    }

    @ThreadedOnly
    public static void saveEpisode(AnimeBasicInfo parent, EpisodeBasicInfo epi, int currentPlayerTime) {
        final boolean isAutoFavorite = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("auto_favorite", false);
        final boolean isAutoMove = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("auto_move", false);

        SavedAnime savedAnime;

        if (parent instanceof LocalAnime la) {
            savedAnime = AnimeMapper.savedFromLocal(la);
        } else if (parent instanceof RemoteAnime ra) {
            SavedAnime sa = animeDao.getByKitsuId(ra.getKitsuId());
            savedAnime = Objects.requireNonNullElseGet(sa, () -> AnimeMapper.savedFromLocal(
                    AnimeMapper.localFromRemote(
                            ra,
                            isAutoFavorite ? AnimeBasicInfo.LocalList.WATCHING : AnimeBasicInfo.LocalList.NOT_TRACKED
                    )
            ));
        } else {
            throw new IllegalStateException("This AnimeBasicInfo instance has no valid type to cast to.");
        }

        SavedEpisode savedEpisode;

        if (epi instanceof LocalEpisode le) {
            savedEpisode = EpisodeMapper.savedFromLocal(le);
        } else if (epi instanceof RemoteEpisode re) {
            SavedEpisode se = episodeDao.getEpisodeByOwnKitsuId(epi.getKitsuId());
            savedEpisode = Objects.requireNonNullElseGet(se, () -> EpisodeMapper.savedFromLocal(
                    EpisodeMapper.localFromRemote(
                            re,
                            currentPlayerTime
                    )
            ));
        } else {
            throw new IllegalStateException("This EpisodeBasicInfo instance has no valid type to cast to.");
        }

        savedEpisode.episode.animeKitsuId = savedAnime.anime.getKitsuId();
        savedEpisode.episode.currentPosition = currentPlayerTime;

        createOrUpdate(savedAnime);
        createOrUpdate(savedEpisode);

        Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);
    }

    public static void deleteAsync(AnimeBasicInfo anime, Consumer<Boolean> callback) {
        Threading.database(() -> {
            try {
                delete(anime);
            } catch(Exception e) {
                Logger.error("Error removing anime with Kitsu ID {} from database", anime.getKitsuId());
                Logger.error(e);
                AnalyticsClient.onError(
                        "database_save_anime",
                        "There was an error while deleting anime from the database",
                        e
                );
                Threading.runOnUiThread(() -> callback.accept(false));
                return;
            }

            Threading.runOnUiThread(() -> callback.accept(true));
        });
    }

    public static void createOrUpdateAsync(AnimeBasicInfo anime, AnimeBasicInfo.LocalList localList, Consumer<Boolean> callback) {
        Threading.database(() -> {
            try {
                createOrUpdate(anime, localList);
            } catch(Exception e) {
                Logger.error("Error posting show with Kitsu ID {} to database", anime.getKitsuId());
                Logger.error(e);
                AnalyticsClient.onError(
                        "database_save_anime",
                        "There was an error while deleting anime from the database",
                        e
                );
                Threading.runOnUiThread(() -> callback.accept(false));
                return;
            }

            Threading.runOnUiThread(() -> callback.accept(true));
        });
    }

    public static void saveEpisodeAsync(AnimeBasicInfo parent, EpisodeBasicInfo epi, int currentPlayerTime, @Nullable Consumer<Boolean> callback) {
        Threading.database(() -> {
           try {
               saveEpisode(parent, epi, currentPlayerTime);
           } catch (Exception e) {
               Logger.error("Error posting episode with Kitsu ID {} to database", epi.getKitsuId());
               Logger.error(e);
               AnalyticsClient.onError(
                       "database_save_anime",
                       "There was an error while deleting anime from the database",
                       e
               );
               if (callback != null) {
                   Threading.runOnUiThread(() -> callback.accept(false));
               }
               return;
           }

            if (callback != null) {
                Threading.runOnUiThread(() -> callback.accept(true));
            }
        });
    }
}
