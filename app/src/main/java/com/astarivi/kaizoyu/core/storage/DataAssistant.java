package com.astarivi.kaizoyu.core.storage;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.MainActivity;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.repositories.RepositoryDirectory;
import com.astarivi.kaizoyu.core.storage.properties.AppConfiguration;
import com.astarivi.kaizoyu.core.storage.properties.BotsConfiguration;
import com.astarivi.kaizoyu.core.storage.properties.ScheduleConfiguration;
import com.astarivi.kaizoyu.core.threading.ThreadingAssistant;
import com.astarivi.kaizoyu.core.threading.workers.WorkerInitializers;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class DataAssistant {
    private final UserHttpClient httpClient = new UserHttpClient();
    private final ThreadingAssistant thAssistant = new ThreadingAssistant();
    private final AppConfiguration configuration;
    private final BotsConfiguration botsConfiguration;
    private final ScheduleConfiguration scheduleConfiguration;
    private final AppDatabase database;
    private final RepositoryDirectory repositoryDirectory;

    public DataAssistant(Context context, @NotNull WeakReference<MainActivity> mainActivity){
        configuration = new AppConfiguration(context);
        botsConfiguration = new BotsConfiguration(context);
        scheduleConfiguration = new ScheduleConfiguration(context);

        WorkerInitializers.queueBackgroundPeriodicWorker(context);

        database = Room.databaseBuilder(
                mainActivity.get().getApplicationContext(),
                AppDatabase.class,
                "kaizo-database"
        ).build(
        );

        repositoryDirectory = new RepositoryDirectory(database);
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public RepositoryDirectory getRepositoryDirectory() {
        return repositoryDirectory;
    }

    public ThreadingAssistant getThreadingAssistant(){
        return thAssistant;
    }

    public UserHttpClient getUserHttpClient(){
        return httpClient;
    }

    public AppConfiguration getConfiguration() {
        return configuration;
    }

    public BotsConfiguration getBotsConfiguration() {
        return botsConfiguration;
    }

    public ScheduleConfiguration getScheduleConfiguration() {
        return scheduleConfiguration;
    }

    public void initializeSettings() {
        Properties appConfig = configuration.getConfiguration();
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

    public void close(){
        // Closing an HttpClient means it will join the main thread. If that were to happen,
        // android would raise a security exception. We close it inside another thread to avoid
        // this issue.
        Future closingFuture = thAssistant.submitToInstantThread(httpClient::close);

        // Wait for task to finish. If exception happens, ignore it.
        try {
            closingFuture.get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        thAssistant.close();
        database.close();
        clearCache();
    }

    public void save(){
        configuration.save();
        botsConfiguration.save();
        scheduleConfiguration.save();
    }

    public void clearCache(){
        // Delete all files in cache
        File[] files = MainActivity.getInstance().getCacheDir().listFiles();
        if(files == null) {
            return;
        }

        for(File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }
}
