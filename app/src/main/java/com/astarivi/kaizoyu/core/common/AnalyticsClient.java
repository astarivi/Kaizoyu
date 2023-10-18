package com.astarivi.kaizoyu.core.common;

import org.acra.ACRA;
import org.tinylog.Logger;


public class AnalyticsClient {
    public static boolean isEnabled = false;

    public static void logBreadcrumb(String event) {
        if (!isEnabled) return;

        final String tag = "Event at " + System.currentTimeMillis();

        ACRA.getErrorReporter().putCustomData(tag, event);
        Logger.info(String.format("%s %s", tag, event));
    }

    public static void onError(String errorId, String message, Throwable exception) {
        if (!isEnabled) return;

        ACRA.getErrorReporter().putCustomData(
                "Event at " + System.currentTimeMillis(),
                String.format("Caught error: %s, with message: %s", errorId, message)
        );

        ACRA.getErrorReporter().handleSilentException(exception);
    }

    public static void onError(String errorId, String message, String exception) {
        if (!isEnabled) return;

        onError(errorId, message, new Exception(exception));
    }
}
