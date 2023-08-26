package com.astarivi.kaizoyu.core.theme;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.MaterialColors;


public class Colors {
    public static @ColorInt int getColorFromString(String string, float saturation, float value) {
        return Color.HSVToColor(new float[]{
                (float) Math.abs(string.hashCode() % 360),
                saturation,
                value
        });
    }

    public static @ColorInt int getSemiTransparentStatusBar(View source, @AttrRes int color) {
        return ColorUtils.setAlphaComponent(
                MaterialColors.getColor(
                        source,
                        color
                ),
                153
        );
    }

    public static @ColorInt int getColorScrim(View source, @AttrRes int color) {
        return ColorUtils.setAlphaComponent(
                MaterialColors.getColor(
                        source,
                        color
                ),
                200
        );
    }

    public static GradientDrawable fadeSurfaceFromStatusBar(View source, @AttrRes int color, GradientDrawable.Orientation orientation) {
        GradientDrawable gd = new GradientDrawable(
                orientation,
                new int[]{
                        getSemiTransparentStatusBar(source, color),
                        ColorUtils.setAlphaComponent(
                                MaterialColors.getColor(
                                        source,
                                        color
                                ),
                                0
                        )
                }
        );

        gd.setCornerRadius(0f);

        return gd;
    }
}
