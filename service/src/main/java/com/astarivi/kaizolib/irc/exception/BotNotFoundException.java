package com.astarivi.kaizolib.irc.exception;

public class BotNotFoundException extends Exception{
    public BotNotFoundException(String reason) {
        super(reason);
    }
}
