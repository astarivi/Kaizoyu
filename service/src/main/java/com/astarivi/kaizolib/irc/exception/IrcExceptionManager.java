package com.astarivi.kaizolib.irc.exception;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class IrcExceptionManager {
    public static @NotNull FailureCode getFailureCode(Exception e) {
        if (e instanceof StrictModeException) {
            return FailureCode.StrictModeFailure;
        } else if (e instanceof BotNotFoundException) {
            return FailureCode.BotNotFound;
        } else if (e instanceof BlacklistedIpException) {
            return FailureCode.BlacklistedIp;
        } else if (e instanceof NoQuickRetryException) {
            return FailureCode.NoQuickRetry;
        } else if (e instanceof TimeoutException) {
            return FailureCode.TimeOut;
        } else if (e instanceof IOException) {
            return FailureCode.IoException;
        } else if (e instanceof GenericHandshakeException) {
            return FailureCode.GenericHandshakeError;
        }

        return FailureCode.Generic;
    }

    public enum FailureCode{
        TimeOut,
        Generic,
        NoQuickRetry,
        IoException,
        StrictModeFailure,
        GenericHandshakeError,
        BlacklistedIp,
        BotNotFound
    }
}
