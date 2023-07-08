package com.astarivi.kaizolib.kitsu.exception;

import org.jetbrains.annotations.NotNull;


public class KitsuExceptionManager {
    public static @NotNull FailureCode getFailureCode(Exception e) {
        if (e instanceof NetworkConnectionException) {
            return FailureCode.NetworkConnectionException;
        } else if (e instanceof NoResponseException) {
            return FailureCode.NoResponseException;
        } else if (e instanceof NoResultsException) {
            return FailureCode.NoResultsException;
        } else if (e instanceof ParsingException) {
            return FailureCode.ParsingException;
        }

        return FailureCode.Generic;
    }

    public enum FailureCode {
        NetworkConnectionException,
        NoResponseException,
        NoResultsException,
        ParsingException,
        Generic
    }
}
