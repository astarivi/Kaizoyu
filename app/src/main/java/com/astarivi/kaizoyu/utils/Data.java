package com.astarivi.kaizoyu.utils;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.MainActivity;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.repositories.RepositoryDirectory;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;


public class Data {
    private static final TemporarySwitches temporarySwitches = new TemporarySwitches();

    public static UserHttpClient getUserHttpClient() {
        return MainActivity.getInstance().getDataAssistant().getUserHttpClient();
    }

    public static Properties getProperties(@NotNull CONFIGURATION type) {
        switch (type) {
            case BOTS:
                return MainActivity.getInstance().getDataAssistant().getBotsConfiguration().getConfiguration();
            case SCHEDULE:
                return MainActivity.getInstance().getDataAssistant().getScheduleConfiguration().getConfiguration();
            case APP:
            default:
                return MainActivity.getInstance().getDataAssistant().getConfiguration().getConfiguration();
        }
    }

    public static void saveProperties(@NotNull CONFIGURATION type) {
        switch (type) {
            case BOTS:
                MainActivity.getInstance().getDataAssistant().getBotsConfiguration().save();
            case SCHEDULE:
                MainActivity.getInstance().getDataAssistant().getScheduleConfiguration().save();
            case APP:
            default:
                MainActivity.getInstance().getDataAssistant().getConfiguration().save();
        }
    }

    public static void reloadProperties() {
        MainActivity.getInstance().getDataAssistant().initializeSettings();
    }

    public static AppDatabase getDatabase() {
        return MainActivity.getInstance().getDataAssistant().getDatabase();
    }

    public static RepositoryDirectory getRepositories() {
        return MainActivity.getInstance().getDataAssistant().getRepositoryDirectory();
    }

    public static TemporarySwitches getTemporarySwitches() {
        return temporarySwitches;
    }

    public enum CONFIGURATION {
        APP, BOTS, SCHEDULE
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
