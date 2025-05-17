package com.astarivi.kaizolib.common.exception;

import java.io.IOException;

public class NoResponseException extends IOException {
    public NoResponseException() {
    }

    public NoResponseException(String message) {
        super(message);
    }

    public NoResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResponseException(Throwable cause) {
        super(cause);
    }
}
