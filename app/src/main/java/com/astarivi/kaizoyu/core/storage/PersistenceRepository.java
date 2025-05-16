package com.astarivi.kaizoyu.core.storage;

import android.app.ActivityManager;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityManagerCompat;
import androidx.room.Room;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.migrations.Migrations;
import com.astarivi.kaizoyu.core.storage.ids.AnimeIdsDatabase;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;

import org.acra.ACRA;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.util.concurrent.FutureTask;

import lombok.AccessLevel;
import lombok.Getter;


@Getter
public class PersistenceRepository {
    @Getter(AccessLevel.NONE)
    private static volatile PersistenceRepository _instance = null;
    private final ExtendedProperties appConfiguration;
    private final ExtendedProperties botsConfiguration;
    private final AppDatabase database;
    private final UserHttpClient httpClient = UserHttpClient.getInstance();
    private final boolean isDeviceLowSpec;

    private PersistenceRepository() {
        if (_instance != null) {
            throw new RuntimeException("Duplicated singleton ConfigurationRepository");
        }

        if (KaizoyuApplication.application == null) {
            throw new RuntimeException("Application context hasn't been initialized yet");
        }

        Context context = KaizoyuApplication.getContext();

        // Check for low specs device
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);

        isDeviceLowSpec = ActivityManagerCompat.isLowRamDevice(activityManager) || memInfo.totalMem < 1000000000L;

        appConfiguration = new ExtendedProperties(context, "Kaizoyu.properties");
        Utils.generateIrcUsername(appConfiguration);

        botsConfiguration = new ExtendedProperties(context, "NiblBots.properties");

        database = Room.databaseBuilder(
                KaizoyuApplication.getApplication().getApplicationContext(),
                AppDatabase.class,
                "kaizo-database"
        ).addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3
        ).build(
        );

        boolean areAnalyticsEnabled = appConfiguration.getBooleanProperty("analytics", false);

        AnalyticsClient.isEnabled = areAnalyticsEnabled;
        ACRA.getErrorReporter().setEnabled(areAnalyticsEnabled);

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

        // Check for ids database updates
        Threading.instant(AnimeIdsDatabase::tryUpdate);

        applyConfigurationChanges();
    }

    public static @NotNull PersistenceRepository getInstance() {
        if (_instance == null) {
            synchronized (PersistenceRepository.class) {
                if (_instance == null) _instance = new PersistenceRepository();
            }
        }
        return _instance;
    }

    public void applyConfigurationChanges() {
        Logger.info("Applying configuration changes");
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

        boolean areAnalyticsEnabled = appConfiguration.getBooleanProperty("analytics", false);

        AnalyticsClient.isEnabled = areAnalyticsEnabled;
        ACRA.getErrorReporter().setEnabled(areAnalyticsEnabled);
    }

    public void saveSettings() {
        appConfiguration.save();
        botsConfiguration.save();
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
