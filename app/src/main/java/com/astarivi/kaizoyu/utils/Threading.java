package com.astarivi.kaizoyu.utils;

import android.os.Handler;
import android.os.Looper;

import com.astarivi.kaizoyu.core.threading.ThreadingAssistant;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Threading {
    // Use this sparingly. Prefer using a view .post method. Only use if no context is available,
    // or for compatibility reasons.
    public static void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static Future<?> submitTask(@NotNull TASK type, @NotNull Runnable task) {
        switch(type) {
            case DATABASE:
                return ThreadingAssistant.getInstance().submitToDatabaseThread(task);
            case INSTANT:
            default:
                return ThreadingAssistant.getInstance().submitToInstantThread(task);
        }
    }

    public static Future<?> database(Runnable task) {
        return ThreadingAssistant.getInstance().submitToDatabaseThread(task);
    }

    /**
     * Runs a series of Instant threads in sequential, FIFO order.
     * <p>
     * Returns a Future to all running tasks. Can be interrupted
     * to stop the next task in list from executing.
     */
    public static Future<?> instant(Runnable... tasks) {
        return ThreadingAssistant.getInstance().submitToInstantThread(() -> {
            for (Runnable task : tasks) {
                if (Thread.interrupted()) {
                    return;
                }

                try {
                    task.run();
                } catch (Exception e) {
                    Logger.error("Instant runnable execution failed. {}", e);
                }
            }
        });
    }

    public static ScheduledFuture<?> submitScheduledTask(@NotNull Runnable task, long delay, @NotNull TimeUnit timeUnit) {
        return ThreadingAssistant.getInstance().scheduleToPlayerThread(task, delay, timeUnit);
    }

    public enum TASK {
        DATABASE, INSTANT
    }

    public static class forResult {
        public static <T> Future<T> fromTask(@NotNull TASK type, @NotNull Callable<T> task) {
            if (type == TASK.DATABASE) {
                return ThreadingAssistant.getInstance().getDatabaseThread().submit(task);
            } else {
                return ThreadingAssistant.getInstance().getInstantThread().submit(task);
            }
        }
    }
}
