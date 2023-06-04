package com.astarivi.kaizoyu.core.threading.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;


public class BackgroundPeriodicWorker extends Worker {

    public BackgroundPeriodicWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        // May want to do something here later, like check for new releases
        return Result.success();
    }
}
