package com.astarivi.kaizoyu.core.storage;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.repositories.RepositoryDirectory;
import com.astarivi.kaizoyu.core.storage.properties.AppConfiguration;
import com.astarivi.kaizoyu.core.storage.properties.BotsConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.FutureTask;


public class PersistenceRepository {
    private static volatile PersistenceRepository instance = null;
    private final AppConfiguration appConfiguration;
    private final BotsConfiguration botsConfiguration;
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

        appConfiguration = new AppConfiguration(context);
        botsConfiguration = new BotsConfiguration(context);
        database = Room.databaseBuilder(
                KaizoyuApplication.getApplication().getApplicationContext(),
                AppDatabase.class,
                "kaizo-database"
        ).build(
        );

        repositoryDirectory = new RepositoryDirectory(database);
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
        Properties appConfig = appConfiguration.getConfiguration();

        int nightTheme = Integer.parseInt(appConfig.getProperty("night_theme", "0"));

        switch (nightTheme) {
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

        AnalyticsClient.isEnabled = Boolean.parseBoolean(
                appConfig.getProperty("analytics", "false")
        );
    }

    public void saveSettings() {
        appConfiguration.save();
        botsConfiguration.save();
    }

    public AppConfiguration getAppConfiguration() {
        return appConfiguration;
    }

    public BotsConfiguration getBotsConfiguration() {
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
            FutureTask<Void> closingFuture = new FutureTask<>((Runnable) httpClient::close, null);
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
