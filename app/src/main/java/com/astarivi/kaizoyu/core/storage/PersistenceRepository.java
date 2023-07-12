package com.astarivi.kaizoyu.core.storage;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.repositories.RepositoryDirectory;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.util.concurrent.FutureTask;


public class PersistenceRepository {
    private static volatile PersistenceRepository instance = null;
    private final ExtendedProperties appConfiguration;
    private final ExtendedProperties botsConfiguration;
    private final AppDatabase database;
    private final RepositoryDirectory repositoryDirectory;
    private final UserHttpClient httpClient = new UserHttpClient();

    private PersistenceRepository() {
        if (instance != null) {
            throw new RuntimeException("Duplicated singleton ConfigurationRepository");
        }

        if (KaizoyuApplication.application == null) {
            throw new RuntimeException("Application context hasn't been initialized yet");
        }

        Context context = KaizoyuApplication.getContext();

        appConfiguration = new ExtendedProperties(context, "Kaizoyu.properties");
        Utils.generateIrcUsername(appConfiguration);

        botsConfiguration = new ExtendedProperties(context, "NiblBots.properties");

        database = Room.databaseBuilder(
                KaizoyuApplication.getApplication().getApplicationContext(),
                AppDatabase.class,
                "kaizo-database"
        ).build(
        );

        repositoryDirectory = new RepositoryDirectory(database);
        AnalyticsClient.isEnabled = appConfiguration.getBooleanProperty("analytics", true);

        try {
            //noinspection ResultOfMethodCallIgnored
            new File(context.getFilesDir(), "log.txt").delete();
        } catch(Exception ignored) {
        }

        System.setProperty(
                "tinylog.directory",
                KaizoyuApplication.getApplication().getApplicationContext().getFilesDir().getAbsolutePath()
        );

        Logger.info("Starting logging session");
    }

    public static @NotNull PersistenceRepository getInstance() {
        if (instance == null) {
            synchronized (PersistenceRepository.class) {
                if (instance == null) instance = new PersistenceRepository();
            }
        }
        return instance;
    }

    public void applyConfigurationChanges() {
        switch (appConfiguration.getIntProperty("night_theme", 0)) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        AnalyticsClient.isEnabled = appConfiguration.getBooleanProperty("analytics", true);
    }

    public void saveSettings() {
        appConfiguration.save();
        botsConfiguration.save();
    }

    public ExtendedProperties getAppConfiguration() {
        return appConfiguration;
    }

    public ExtendedProperties getBotsConfiguration() {
        return botsConfiguration;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public RepositoryDirectory getRepositoryDirectory() {
        return repositoryDirectory;
    }

    public UserHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    protected void finalize() throws Throwable {
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            database.close();

            // Closing an HttpClient means it will join the main thread. If that were to happen,
            // android would raise a security exception. We close it inside another thread to avoid
            // this issue.
            FutureTask<Void> closingFuture = new FutureTask<>(httpClient::close, null);
            Thread thread = new Thread(closingFuture);
            thread.start();
            thread.join(10000);
        } catch(Throwable th) {
            throw th;
        } finally {
            super.finalize();
        }
    }
}
