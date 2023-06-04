package com.astarivi.kaizoyu.core.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class ThreadingAssistant {
    private final ExecutorService databaseThread = Executors.newSingleThreadExecutor();
    private final ExecutorService instantThread = Executors.newCachedThreadPool();
    private final ExecutorService backgroundThread = Executors.newFixedThreadPool(2);

    public Future submitToInstantThread(Runnable download){
        return instantThread.submit(download);
    }

    public Future submitToGeneralThread(Runnable task) {
        return backgroundThread.submit(task);
    }

    public Future submitToDatabaseThread(Runnable task) {
        return databaseThread.submit(task);
    }

    public void close(){
        try {
            databaseThread.shutdown();
            instantThread.shutdownNow();
            backgroundThread.shutdownNow();
            instantThread.awaitTermination(2, TimeUnit.SECONDS);
            backgroundThread.awaitTermination(2, TimeUnit.SECONDS);
            databaseThread.awaitTermination(40, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
