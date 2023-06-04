package com.astarivi.kaizoyu.video.gui;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.databinding.PlayerSkipManagerBinding;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class PlayerSkipView extends LinearLayout {
    private PlayerSkipManagerBinding binding;
    private PlayerBarView playerBarView;
    private SkippingStatus status = SkippingStatus.IDLE;
    private int skipAmount = 0;

    public PlayerSkipView(Context context) {
        super(context);
        this.inflateView(context);
    }

    public PlayerSkipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.inflateView(context);
    }

    public PlayerSkipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.inflateView(context);
    }

    public PlayerSkipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.inflateView(context);
    }

    private void inflateView(Context context) {
        binding = PlayerSkipManagerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
        );
    }

    public void initialize(PlayerBarView playerBarView) {
        binding.skipContainerLeft.setVisibility(INVISIBLE);
        binding.skipContainerRight.setVisibility(INVISIBLE);

        this.playerBarView = playerBarView;

        binding.skipAnimationLeft.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                binding.skipContainerLeft.setVisibility(INVISIBLE);
                playerBarView.skipAmount(skipAmount, true);
                skipAmount = 0;
                status = SkippingStatus.IDLE;
                playerBarView.resume();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });

        binding.skipAnimationRight.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                binding.skipContainerRight.setVisibility(INVISIBLE);
                playerBarView.skipAmount(skipAmount, true);
                skipAmount = 0;
                status = SkippingStatus.IDLE;
                playerBarView.resume();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }

    public void skipAhead() {
        if (status == SkippingStatus.BACK) return;
        status = SkippingStatus.AHEAD;
        playerBarView.pause();

        binding.skipContainerRight.setVisibility(VISIBLE);
        binding.skipAnimationRight.playAnimation();
        skipAmount += 10000;

        binding.skipAmountRight.setText(
                String.format(
                        Locale.ENGLISH,
                        "%ds",
                        TimeUnit.MILLISECONDS.toSeconds(skipAmount)
                )
        );
    }

    public void skipBack() {
        if (status == SkippingStatus.AHEAD) return;
        status = SkippingStatus.BACK;
        playerBarView.pause();

        binding.skipContainerLeft.setVisibility(VISIBLE);
        binding.skipAnimationLeft.playAnimation();
        skipAmount -= 10000;

        binding.skipAmountLeft.setText(
                String.format(
                        Locale.ENGLISH,
                        "%ds",
                        TimeUnit.MILLISECONDS.toSeconds(Math.abs(skipAmount))
                )
        );
    }

    private enum SkippingStatus {
        IDLE,
        AHEAD,
        BACK
    }
}
