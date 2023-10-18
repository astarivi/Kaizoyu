package com.astarivi.kaizoyu.video.gui;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.databinding.PlayerBinding;
import com.astarivi.kaizoyu.utils.Threading;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import in.basulabs.audiofocuscontroller.AudioFocusController;
import lombok.Getter;


public class PlayerView extends LinearLayout {
    private PlayerBinding binding;
    private PlayerEventListener listener;
    private AudioFocusController audioController;
    @Getter
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
        LayoutInflater inflater = LayoutInflater.from(context);

        binding = PlayerBinding.inflate(
                inflater,
                this,
                true
        );
    }

    public void initialize(
            String animeTitle,
            String episodeTitle,
            PlayerEventListener eventListener,
            AudioFocusController audioController
    ) {
        listener = eventListener;
        this.audioController = audioController;

        // Set strings
        binding.animeTitle.setText(animeTitle);
        binding.episodeTitle.setText(episodeTitle);

        // Prepare children views
        binding.playerBar.setVisibility(View.INVISIBLE);
        binding.darkOverlay.setVisibility(View.INVISIBLE);
        binding.playerInfoBar.setVisibility(View.INVISIBLE);

        binding.topBackButton.setOnClickListener(v -> listener.onBackPressed());

        binding.skipManager.initialize(binding.playerBar);
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
        playerBar.initialize(binding.darkOverlay, listener, audioController);
        playerBar.show();
    }

    public void destroy() {
        binding.playerBar.terminate();

        if (mediaPlayer == null) return;

        mediaPlayer.pause();
        mediaPlayer.setMedia(null);
//        mediaPlayer.detachViews();

        Future<?> releaseFuture = Threading.submitTask(Threading.TASK.INSTANT, () ->
                mediaPlayer.release()
        );

        // FIXME: Investigate further
        try {
            releaseFuture.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            releaseFuture.cancel(true);
        }

        mediaPlayer = null;
    }

    public PlayerBarView getPlayerBar() {
        return binding.playerBar;
    }

    public PlayerSkipView getSkipManager() {
        return binding.skipManager;
    }

    public interface PlayerEventListener {
        void onBackPressed();
        void onPlayingStateChanged(boolean isPlaying);
        void onVideoFinished();
    }
}
