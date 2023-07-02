package com.astarivi.kaizolib.irc.exception;


public class StrictModeException extends Exception{
    public StrictModeException(Exception exception) {
        super(exception);
    }
}