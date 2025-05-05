package com.astarivi.kaizolib.kitsuv2.exception;


public class KitsuException extends Exception {
    public KitsuException() {
    }

    public KitsuException(String message) {
        super(message);
    }

    public KitsuException(String message, Throwable cause) {
        super(message, cause);
    }

    public KitsuException(Throwable cause) {
        super(cause);
    }
}
