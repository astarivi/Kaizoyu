package com.astarivi.kaizoyu.utils;

import android.os.Handler;
import android.os.Looper;

import com.astarivi.kaizoyu.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;


public class Threading {
    public static void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static Future submitTask(@NotNull TASK type, @NotNull Runnable task) {
        switch(type) {
            case DATABASE:
                return MainActivity.getInstance().getDataAssistant().getThreadingAssistant().submitToDatabaseThread(task);
            case INSTANT:
                return MainActivity.getInstance().getDataAssistant().getThreadingAssistant().submitToInstantThread(task);
            case BACKGROUND:
            default:
                return MainActivity.getInstance().getDataAssistant().getThreadingAssistant().submitToGeneralThread(task);
        }
    }

    public enum TASK {
        BACKGROUND, DATABASE, INSTANT
    }
}
