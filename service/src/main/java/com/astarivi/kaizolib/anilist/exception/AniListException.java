package com.astarivi.kaizolib.anilist.exception;

public class AniListException extends Exception {
    public AniListException() {
    }

    public AniListException(String message) {
        super(message);
    }

    public AniListException(String message, Throwable cause) {
        super(message, cause);
    }

    public AniListException(Throwable cause) {
        super(cause);
    }
}
