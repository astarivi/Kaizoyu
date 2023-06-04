package com.astarivi.kaizoyu.core.threading.workers;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;


public class WorkerInitializers {
    public static void queueBackgroundPeriodicWorker(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                BackgroundPeriodicWorker.class,
                24,
                TimeUnit.HOURS,
                3,
                TimeUnit.HOURS
        ).setConstraints(
                constraints
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "imageCacheBackgroundWorker",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
        );
    }
}
