package com.astarivi.kaizoyu.core.common;

import com.flurry.android.FlurryAgent;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class AnalyticsClient {
    public static boolean isEnabled = false;

    public static void logEvent(String event, Map<String, String> content) {
        if (!isEnabled) return;
        FlurryAgent.logEvent(event, content);
    }

    public static void logEvent(String event, boolean content) {
        if (!isEnabled) return;
        FlurryAgent.logEvent(event, content);
    }

    public static void logEvent(String event, @NotNull String content) {
        if (!isEnabled) return;

        HashMap<String, String> eventContent = new HashMap<>();
        eventContent.put("value", content);
        FlurryAgent.logEvent(event, eventContent);
    }

    public static void logEvent(String event) {
        if (!isEnabled) return;
        FlurryAgent.logEvent(event);
    }

    public static void onError(String errorId, String message, Throwable exception) {
        if (!isEnabled) return;
        FlurryAgent.onError(errorId, message, exception);
    }

    public static void onError(String errorId, String message, String exception) {
        if (!isEnabled) return;
        FlurryAgent.onError(errorId, message, exception);
    }
}
