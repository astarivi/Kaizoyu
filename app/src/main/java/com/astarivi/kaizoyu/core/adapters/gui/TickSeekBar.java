package com.astarivi.kaizoyu.core.adapters.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.res.ResourcesCompat;

import com.astarivi.kaizoyu.R;

import java.util.Arrays;

import lombok.Getter;


public class TickSeekBar extends AppCompatSeekBar {
    @Getter
    private int[] tickPositions = null;
    private Drawable ticksDrawable = null;

    public TickSeekBar(@NonNull Context context) {
        super(context);
    }

    public TickSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public TickSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        int dotsArrayResource;
        int dotDrawableId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try (TypedArray attributesArray = getContext().obtainStyledAttributes(attrs, R.styleable.TickSeekBar, 0, 0)) {
                dotsArrayResource = attributesArray.getResourceId(R.styleable.TickSeekBar_dots_positions, 0);
                dotDrawableId = attributesArray.getResourceId(R.styleable.TickSeekBar_dots_drawable, 0);
            }
        } else {
            TypedArray attributesArray = getContext().obtainStyledAttributes(attrs, R.styleable.TickSeekBar, 0, 0);
            dotsArrayResource = attributesArray.getResourceId(R.styleable.TickSeekBar_dots_positions, 0);
            dotDrawableId = attributesArray.getResourceId(R.styleable.TickSeekBar_dots_drawable, 0);
            attributesArray.close();
            attributesArray.recycle();
        }

        if (dotsArrayResource != 0) {
            tickPositions = getResources().getIntArray(dotsArrayResource);
            Arrays.sort(tickPositions);
        }

        if (dotDrawableId != 0) {
            ticksDrawable = ResourcesCompat.getDrawable(getResources(), dotDrawableId, null);
        }
    }

    public void setTicks(int[] dots) {
        tickPositions = Arrays.copyOf(dots, dots.length);
        Arrays.sort(tickPositions);
        invalidate();
    }

    public void setTicksDrawable(@DrawableRes int dotsDrawable) {
        ticksDrawable = ResourcesCompat.getDrawable(getResources(), dotsDrawable, null);
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTicks(canvas);
    }

    private void drawTicks(Canvas canvas) {
        if (tickPositions == null || tickPositions.length == 0 || ticksDrawable == null) return;

        final int newHeight = getProgressDrawable().getIntrinsicHeight() + 2;
        final int w = ticksDrawable.getIntrinsicWidth();
        final int h = ticksDrawable.getIntrinsicHeight();
        final int newWidth = (int) (w * ((float) newHeight / h)) + 8;
        final int halfW = newWidth >= 0 ? newWidth / 2 : 1;
        final int halfH = newHeight >= 0 ? newHeight / 2 : 1;
        ticksDrawable.setBounds(-halfW, -halfH, halfW, halfH);

        float spacing = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / ((float) getMax());
        final int saveCount = canvas.save();
        canvas.translate(getPaddingLeft(), (float) getHeight() / 2F);

        int positionSum = 0;

        for (int position : tickPositions) {
            canvas.translate((position - positionSum) * spacing, 0);
            ticksDrawable.draw(canvas);
            positionSum += position;
        }

        canvas.restoreToCount(saveCount);
    }
}
