package com.astarivi.kaizoyu.utils;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.repositories.RepositoryDirectory;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;


public class Data {
    private static final TemporarySwitches temporarySwitches = new TemporarySwitches();

    public static UserHttpClient getUserHttpClient() {
        return PersistenceRepository.getInstance().getHttpClient();
    }

    public static Properties getProperties(@NotNull CONFIGURATION type) {
        switch (type) {
            case BOTS:
                return PersistenceRepository.getInstance().getBotsConfiguration().getConfiguration();
            case APP:
            default:
                return PersistenceRepository.getInstance().getAppConfiguration().getConfiguration();
        }
    }

    public static void saveProperties(@NotNull CONFIGURATION type) {
        switch (type) {
            case BOTS:
                PersistenceRepository.getInstance().getBotsConfiguration().save();
            case APP:
            default:
                PersistenceRepository.getInstance().getAppConfiguration().save();
        }
    }

    public static void reloadProperties() {
        PersistenceRepository.getInstance().applyConfigurationChanges();
    }

    public static AppDatabase getDatabase() {
        return PersistenceRepository.getInstance().getDatabase();
    }

    public static RepositoryDirectory getRepositories() {
        return PersistenceRepository.getInstance().getRepositoryDirectory();
    }

    public static TemporarySwitches getTemporarySwitches() {
        return temporarySwitches;
    }

    public enum CONFIGURATION {
        APP, BOTS
    }

    public static class TemporarySwitches {
        private boolean pendingFavoritesRefresh = false;
        private boolean pendingSeenEpisodeStateRefresh = false;

        public boolean isPendingFavoritesRefresh() {
            return pendingFavoritesRefresh;
        }

        public void setPendingFavoritesRefresh(boolean value) {
            pendingFavoritesRefresh = value;
        }

        public boolean isPendingSeenEpisodeStateRefresh() {
            return pendingSeenEpisodeStateRefresh;
        }

        public void setPendingSeenEpisodeStateRefresh(boolean pendingSeenEpisodeStateRefresh) {
            this.pendingSeenEpisodeStateRefresh = pendingSeenEpisodeStateRefresh;
        }
    }
}
