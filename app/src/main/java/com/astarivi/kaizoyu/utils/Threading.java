package com.astarivi.kaizoyu.utils;

import android.os.Handler;
import android.os.Looper;

import com.astarivi.kaizoyu.core.threading.ThreadingAssistant;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;


public class Threading {
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

    public enum TASK {
        DATABASE, INSTANT
    }
}
