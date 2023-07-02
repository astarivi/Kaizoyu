package com.astarivi.kaizolib.irc.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Utils {
    public static @NotNull String shuffle (String s) {
        StringBuilder result = new StringBuilder(s);
        int n = result.length();
        Random rand = new Random();

        while (n>1) {
            int randomPoint = rand.nextInt(n);
            char randomChar = result.charAt(randomPoint);
            result.setCharAt(n-1,randomChar);
            n--;
        }

        return result.toString();
    }
}
