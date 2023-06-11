package com.astarivi.kaizoyu.video.gui;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import com.astarivi.kaizoyu.databinding.PlayerBinding;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.util.ArrayList;


public class PlayerView extends LinearLayout {
    private PlayerBinding binding;
    private LayoutInflater inflater;
    private PlayerEventListener listener;
    private GestureDetectorCompat gestureDetector;
    private MediaPlayer mediaPlayer;

    public PlayerView(Context context) {
        super(context);
        inflateView(context);
    }

    public PlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    public PlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateView(context);
    }

    private void inflateView(Context context) {
        inflater = LayoutInflater.from(context);

        binding = PlayerBinding.inflate(
                inflater,
                this,
                true
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        performClick();

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();

        return true;
    }

    public void initialize(
            String animeTitle,
            String episodeTitle,
            PlayerEventListener eventListener
    ) {
        listener = eventListener;

        // Set strings
        binding.animeTitle.setText(animeTitle);
        binding.episodeTitle.setText(episodeTitle);

        // Prepare children views
        binding.playerBar.setVisibility(View.INVISIBLE);
        binding.darkOverlay.setVisibility(View.INVISIBLE);
        binding.playerInfoBar.setVisibility(View.INVISIBLE);

        binding.topBackButton.setOnClickListener(v -> listener.onBackPressed());

        gestureDetector = new GestureDetectorCompat(
                getContext(),
                new PlayerView.PlayerGestureListener()
        );

        binding.skipManager.initialize(binding.playerBar);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setCacheProgress(int progress) {
        binding.playerBar.setCacheProgress(progress);
    }

    public void setDownloadSpeed(String speed) {
        binding.playerBar.setDownloadSpeed(speed);
    }

    public void play(File video) {
        final ArrayList<String> args = new ArrayList<>();
        args.add("--file-caching=2000");

        LibVLC libVlc = new LibVLC(getContext(), args);
        mediaPlayer = new MediaPlayer(libVlc);

        binding.playerBar.setSubtitlesOnClickListener(v -> PlayerTrackMenuView.show(
                getContext(),
                binding,
                mediaPlayer
        ));

        mediaPlayer.attachViews(binding.videoFrame, null, true, true);

        PlayerBarView playerBar = binding.playerBar;
        playerBar.setMediaPlayer(mediaPlayer, binding.playerInfoBar);
        final Media media = new Media(libVlc, Uri.fromFile(video));
        mediaPlayer.setMedia(media);
        mediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT);
        media.release();
        binding.videoFrame.setVisibility(View.VISIBLE);
        playerBar.initialize(binding.darkOverlay);
        playerBar.show();
    }

    public void destroy() {
        binding.playerBar.terminate();

        if (mediaPlayer == null) return;

        mediaPlayer.pause();
        mediaPlayer.setMedia(null);
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            binding.playerBar.show();

            return super.onSingleTapConfirmed(event);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {

            int eventX = (int)event.getX();
            int eventY = (int)event.getY();
            int height = binding.getRoot().getHeight();
            int width = binding.getRoot().getWidth();
            int halfWidth = width / 2;

            if (eventX > halfWidth) {
                binding.skipManager.skipAhead();
            } else {
                binding.skipManager.skipBack();
            }

            return super.onDoubleTap(event);
        }
    }


    public interface PlayerEventListener {
        void onBackPressed();
    }
}
