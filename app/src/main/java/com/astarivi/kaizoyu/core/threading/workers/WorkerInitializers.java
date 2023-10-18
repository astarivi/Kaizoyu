package com.astarivi.kaizoyu.core.threading.workers;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.acra.ACRA;

import java.util.concurrent.TimeUnit;


public class WorkerInitializers {
    public static void queueWorkers(Context context) {
        if (ACRA.isACRASenderServiceProcess()) return;

        PeriodicWorkRequest updateWorkRequest = new PeriodicWorkRequest.Builder(
                UpdatePeriodicWorker.class,
                24,
                TimeUnit.HOURS,
                6,
                TimeUnit.HOURS
        ).setConstraints(
                new Constraints.Builder()
                        .setRequiresDeviceIdle(false)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
        ).build();

        PeriodicWorkRequest episodeWorkRequest = new PeriodicWorkRequest.Builder(
                EpisodePeriodicWorker.class,
                6,
                TimeUnit.HOURS,
                2,
                TimeUnit.HOURS
        ).setConstraints(
                new Constraints.Builder()
                        .setRequiresDeviceIdle(false)
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .setRequiresBatteryNotLow(true)
                        .build()
        ).build();

        WorkManager workManager = WorkManager.getInstance(context);

        workManager.enqueueUniquePeriodicWork(
                "update_task",
                ExistingPeriodicWorkPolicy.UPDATE,
                updateWorkRequest
        );

        workManager.enqueueUniquePeriodicWork(
                "episode_task",
                ExistingPeriodicWorkPolicy.UPDATE,
                episodeWorkRequest
        );
    }
}
