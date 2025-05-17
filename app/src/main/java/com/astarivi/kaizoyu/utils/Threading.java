package com.astarivi.kaizoyu.utils;

import android.os.Handler;
import android.os.Looper;

import com.astarivi.kaizoyu.core.threading.ThreadingAssistant;

import org.jetbrains.annotations.NotNull;

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

    public static Future<?> instant(Runnable task) {
        return ThreadingAssistant.getInstance().submitToInstantThread(task);
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
