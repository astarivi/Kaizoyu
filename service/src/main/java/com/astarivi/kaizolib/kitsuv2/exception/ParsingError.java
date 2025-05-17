package com.astarivi.kaizolib.kitsuv2.exception;

import com.astarivi.kaizolib.anilist.exception.AniListException;


public class ParsingError extends AniListException {
    public ParsingError() {
    }

    public ParsingError(String message) {
        super(message);
    }

    public ParsingError(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingError(Throwable cause) {
        super(cause);
    }
}