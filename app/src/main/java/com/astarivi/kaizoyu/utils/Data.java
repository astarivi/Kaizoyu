package com.astarivi.kaizoyu.utils;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.Setter;


public class Data {
    @Getter
    private static final TemporarySwitches temporarySwitches = new TemporarySwitches();

    public static UserHttpClient getUserHttpClient() {
        return PersistenceRepository.getInstance().getHttpClient();
    }

    public static ExtendedProperties getProperties(@NotNull CONFIGURATION type) {
        switch (type) {
            case BOTS:
                return PersistenceRepository.getInstance().getBotsConfiguration();
            case APP:
            default:
                return PersistenceRepository.getInstance().getAppConfiguration();
        }
    }

    public static void reloadProperties() {
        PersistenceRepository.getInstance().applyConfigurationChanges();
    }

    public static AppDatabase getDatabase() {
        return PersistenceRepository.getInstance().getDatabase();
    }

    public static boolean isDeviceLowSpec() {
        return PersistenceRepository.getInstance().isDeviceLowSpec();
    }

    public enum CONFIGURATION {
        APP, BOTS
    }

    @Getter
    @Setter
    public static class TemporarySwitches {
        private boolean pendingFavoritesRefresh = false;
        private boolean pendingSeenEpisodeStateRefresh = false;
    }
}
