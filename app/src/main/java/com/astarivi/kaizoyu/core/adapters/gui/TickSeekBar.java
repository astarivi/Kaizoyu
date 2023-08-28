package com.astarivi.kaizoyu.core.adapters.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.astarivi.kaizoyu.R;


public class TickSeekBar extends AppCompatSeekBar {
    private int[] tickPositions = null;
    private Bitmap ticksBitmap = null;

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
        }

        if (dotDrawableId != 0) {
            ticksBitmap = BitmapFactory.decodeResource(getResources(), dotDrawableId);
        }
    }

    public void setDots(int[] dots) {
        tickPositions = dots;
        invalidate();
    }

    public void setDotsDrawable(@DrawableRes int dotsDrawable) {
        ticksBitmap = BitmapFactory.decodeResource(getResources(), dotsDrawable);
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        float step = width / (float) (getMax());

        if (tickPositions != null && tickPositions.length != 0 && ticksBitmap != null) {
            for (int position : tickPositions) {
                canvas.drawBitmap(ticksBitmap, position * step, 0, null);
            }
        }
    }
}
