package com.astarivi.kaizoyu.core.threading;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class ThreadingAssistant {
    private static volatile ThreadingAssistant instance = null;
    private final ExecutorService databaseThread = Executors.newSingleThreadExecutor();
    private final ExecutorService instantThread = Executors.newCachedThreadPool();

    private ThreadingAssistant() {
        if (instance != null) {
            throw new RuntimeException("Duplicated singleton ThreadingAssistant");
        }
    }

    public static @NotNull ThreadingAssistant getInstance() {
        if (instance == null) {
            synchronized (ThreadingAssistant.class) {
                if (instance == null) instance = new ThreadingAssistant();
            }
        }
        return instance;
    }

    public Future<?> submitToInstantThread(Runnable download){
        return instantThread.submit(download);
    }

    public Future<?> submitToDatabaseThread(Runnable task) {
        return databaseThread.submit(task);
    }

    @Override
    protected void finalize() throws Throwable {
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            databaseThread.shutdown();
            instantThread.shutdownNow();
            instantThread.awaitTermination(2, TimeUnit.SECONDS);
            databaseThread.awaitTermination(40, TimeUnit.SECONDS);
        } catch (Throwable th) {
            throw th;
        } finally {
            super.finalize();
        }
    }
}
