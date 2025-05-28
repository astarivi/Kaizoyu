package com.astarivi.kaizolib.common.exception;

import java.io.IOException;

public class UnexpectedStatusCodeException extends IOException {
    private final int code;

    public UnexpectedStatusCodeException(int code) {
        super(String.valueOf(code));
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
