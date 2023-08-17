package com.astarivi.kaizoyu.core.threading;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.Getter;


@Getter
public class ThreadingAssistant {
    @Getter(AccessLevel.NONE)
    private static volatile ThreadingAssistant _instance = null;
    private final ExecutorService databaseThread = Executors.newSingleThreadExecutor();
    private final ExecutorService instantThread = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledThread = Executors.newSingleThreadScheduledExecutor();

    private ThreadingAssistant() {
        if (_instance != null) {
            throw new RuntimeException("Duplicated singleton ThreadingAssistant");
        }
    }

    public static @NotNull ThreadingAssistant getInstance() {
        if (_instance == null) {
            synchronized (ThreadingAssistant.class) {
                if (_instance == null) _instance = new ThreadingAssistant();
            }
        }
        return _instance;
    }

    public Future<?> submitToInstantThread(Runnable download){
        return instantThread.submit(download);
    }

    public Future<?> submitToDatabaseThread(Runnable task) {
        return databaseThread.submit(task);
    }

    public ScheduledFuture<?> scheduleToPlayerThread(Runnable task, long delay, TimeUnit timeUnit) {
        return scheduledThread.schedule(task, delay, timeUnit);
    }

    @Override
    protected void finalize() throws Throwable {
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            databaseThread.shutdown();
            instantThread.shutdownNow();
            scheduledThread.shutdownNow();
            instantThread.awaitTermination(2, TimeUnit.SECONDS);
            scheduledThread.awaitTermination(2, TimeUnit.SECONDS);
            databaseThread.awaitTermination(40, TimeUnit.SECONDS);
        } catch (Throwable th) {
            throw th;
        } finally {
            super.finalize();
        }
    }
}
