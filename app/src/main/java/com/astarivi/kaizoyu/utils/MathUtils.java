package com.astarivi.kaizoyu.utils;

public class MathUtils {
    public static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    public static float lerp(
            float outputMax, float outputMin, float inputMin, float inputMax, float value) {
        if (value <= inputMin) {
            return outputMax;
        }
        if (value >= inputMax) {
            return outputMin;
        }

        return lerp(outputMax, outputMin, (value - inputMin) / (inputMax - inputMin));
    }
}
