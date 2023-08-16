package com.astarivi.kaizoyu.core.threading.workers;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;


public class WorkerInitializers {
    public static void queueWorkers(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                UpdatePeriodicWorker.class,
                24,
                TimeUnit.HOURS,
                6,
                TimeUnit.HOURS
        ).setConstraints(
                constraints
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "update_task",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
        );
    }
}
