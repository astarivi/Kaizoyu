package com.astarivi.kaizoyu.video.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.databinding.PlayerBarBinding;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;

import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class PlayerBarView extends LinearLayout {
    private MediaPlayer mediaPlayer;
    private LinearLayout playerInfoView;
    private PlayerBarBinding binding;
    private boolean isInteractive = false;
    private View darkOverlay;
    private ScheduledFuture<?> showFuture;
    private String totalDuration;
    private PlayerView.PlayerEventListener playerEventListener;

    // region Constructors

    public PlayerBarView(Context context) {
        super(context);
        this.inflateView(context);
    }

    public PlayerBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.inflateView(context);
    }

    public PlayerBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.inflateView(context);
    }

    public PlayerBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.inflateView(context);
    }

    // endregion

    private void inflateView(Context context) {
        binding = PlayerBarBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
        );
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        State state = new State(super.onSaveInstanceState(), mediaPlayer, playerInfoView);
        bundle.putParcelable(State.STATE, state);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            State customViewState = getStateFromBundle(bundle);
            if (customViewState == null) {
                super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
                return;
            }
            mediaPlayer = customViewState.getMediaPlayer();
            playerInfoView = customViewState.getPlayerInfoBar();
            super.onRestoreInstanceState(customViewState.getSuperState());
            return;
        }
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
    }

    @SuppressWarnings("deprecation")
    private @Nullable State getStateFromBundle(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable(State.STATE, State.class);
        }
        return bundle.getParcelable(State.STATE);
    }

    @NotNull
    private String verboseMillisecondsToDuration(long duration) {
        if (duration < 0) return "??:??:??";


        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000*60)) % 60;
        long hours = (duration / (1000*60*60)) % 24;

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setProgressFromTime(long time) {
        float length = mediaPlayer.getLength();
        float progress = (float) (((float) time / length) * 1000.0);

        binding.videoProgressBar.setProgress(
                Math.round(progress)
        );

        binding.currentTime.setText(
                String.format(
                        Locale.UK,
                        "%s / %s",
                        verboseMillisecondsToDuration(time),
                        totalDuration
                )
        );
    }

    private long getTimeFromProgress(long progress) {
        return Math.round(((float) progress / 1000.0) * (float) mediaPlayer.getLength());
    }

    private void syncPlayButton(boolean isPlaying) {
        ImageView interactionButton = binding.pause;

        if (isPlaying) {
            interactionButton.setImageResource(R.drawable.ic_stop);
        } else {
            interactionButton.setImageResource(R.drawable.ic_resume);
        }
    }

    public void setCacheProgress(int progress) {
        if (progress == 100) binding.downloadSpeedMeter.setVisibility(GONE);
        binding.videoProgressBar.setSecondaryProgress(progress * 10);
    }

    public void setDownloadSpeed(String speed) {
        binding.downloadSpeedMeter.setText(speed);
    }

    @SuppressLint("SetTextI18n")
    public void setMediaPlayer(MediaPlayer mPlayer, LinearLayout playerInfoView) {
        this.mediaPlayer = mPlayer;
        this.playerInfoView = playerInfoView;

        binding.currentTime.setText("00:00:00");
        totalDuration = verboseMillisecondsToDuration(
                mediaPlayer.getLength()
        );

        binding.currentTime.setText("00:00:00 / " + totalDuration);

        binding.videoProgressBar.setProgress(0);
        this.setCacheProgress(0);

        this.takeControlOfUi();
    }

    private void takeControlOfUi() {
        mediaPlayer.setEventListener(event -> {
            switch(event.type) {
                case MediaPlayer.Event.Playing:
                    if (playerEventListener != null) playerEventListener.onPlayingStateChanged(true);
                    syncPlayButton(true);
                    break;
                case MediaPlayer.Event.EndReached:
                    this.setProgressFromTime(mediaPlayer.getLength());
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    if (playerEventListener != null) playerEventListener.onPlayingStateChanged(false);
                    syncPlayButton(false);
                    break;
                // TODO: Introduce some kind of message to go back, or show a button to replay
                case MediaPlayer.Event.TimeChanged:
                    this.setProgressFromTime(event.getTimeChanged());
                    break;
                case MediaPlayer.Event.LengthChanged:
                    totalDuration = verboseMillisecondsToDuration(
                            event.getLengthChanged()
                    );
                    break;
            }
        });
    }

    private void bestowControlOfUi() {
        mediaPlayer.setEventListener(null);
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void resume() {
        mediaPlayer.play();
    }

    public void initialize(View darkOverlay, PlayerView.PlayerEventListener playerEventListener) {
        if (mediaPlayer == null) {
            Logger.error("Tried to play with a MediaPlayer that hasn't been set.");
            throw new RuntimeException("Tried to play with a MediaPlayer that doesn't exist. " +
                    "Please call setMediaPlayer(MediaPlayer mPlayer) first.");
        }

        this.playerEventListener = playerEventListener;
        this.darkOverlay = darkOverlay;

        final ImageView skipIntro = binding.skipIntro;

        skipIntro.setOnClickListener(v -> {
            isInteractive = true;
            skipAmount(90000, true);
            // Update immediately
            binding.currentTime.setText(
                    verboseMillisecondsToDuration(
                            mediaPlayer.getTime()
                    )
            );

            mediaPlayer.play();
        });

        final ImageView interactionButton = binding.pause;

        interactionButton.setOnClickListener(v -> {
            isInteractive = true;

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        });

        binding.videoProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();

                binding.currentTime.setText(
                        verboseMillisecondsToDuration(
                                getTimeFromProgress(
                                        progress
                                )
                        )
                );

                isInteractive = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                isInteractive = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTime(
                        getTimeFromProgress(
                                seekBar.getProgress()
                        ),
                        false
                );

                // Audio workaround for seeking bug
                final IMedia.Track currentAudioTrack = mediaPlayer.getSelectedTrack(IMedia.Track.Type.Audio);
                mediaPlayer.unselectTrackType(IMedia.Track.Type.Audio);

                if (currentAudioTrack != null) {
                    mediaPlayer.selectTrack(currentAudioTrack.id);
                }

                if (!mediaPlayer.isPlaying()) mediaPlayer.play();
                isInteractive = true;
            }
        });

        binding.hideBar.setOnClickListener(v -> {
            isInteractive = false;
            if (showFuture != null && !showFuture.isDone()) showFuture.cancel(false);
            this.hide();
        });

        mediaPlayer.play();
    }

    public void seekTime(long time, boolean fast) {
        if (time > mediaPlayer.getLength()) {
            time = mediaPlayer.getLength();
        }

        mediaPlayer.setTime(time, fast);
    }

    public void skipAmount(long amountMs, boolean fast) {
        long currentTime = mediaPlayer.getTime() + amountMs;

        seekTime(currentTime, fast);
    }

    public void terminate() {
        if (mediaPlayer != null) mediaPlayer.setEventListener(null);
        mediaPlayer = null;
        if (showFuture != null && !showFuture.isDone()) showFuture.cancel(true);
    }

    public void show() {
        if (showFuture != null && !showFuture.isDone()) {
            isInteractive = false;
            showFuture.cancel(true);
            showFuture = null;
            this.hide();
            return;
        }

        this.setVisibility(VISIBLE);
        playerInfoView.setVisibility(VISIBLE);
        darkOverlay.setVisibility(VISIBLE);
        scheduleShow();
    }

    private void scheduleShow() {
        showFuture = Threading.submitScheduledTask(this::hide, 4, TimeUnit.SECONDS);
    }

    public void hide() {
        if (isInteractive) {
            isInteractive = false;
            scheduleShow();
            return;
        }

        // If view is detached.
        try {
            binding.getRoot().post(() -> {
                // Very stupid try block inside try block, but not actually because they're not in
                // the same thread. Still as stupid, though.
                try {
                    this.setVisibility(INVISIBLE);
                    darkOverlay.setVisibility(INVISIBLE);
                    playerInfoView.setVisibility(INVISIBLE);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public void setSubtitlesOnClickListener(OnClickListener listener) {
        binding.subtitlesSelector.setOnClickListener(listener);
    }

    protected static class State extends BaseSavedState {
        protected static final String STATE = "PlayerBarView.STATE";
        private final MediaPlayer mediaPlayer;
        private final LinearLayout playerInfoView;

        public State(Parcelable superState, MediaPlayer mediaPlayer, @Nullable LinearLayout playerInfoView) {
            super(superState);
            this.mediaPlayer = mediaPlayer;
            this.playerInfoView = playerInfoView;
        }

        public MediaPlayer getMediaPlayer(){
            return this.mediaPlayer;
        }

        public LinearLayout getPlayerInfoBar() {
            return this.playerInfoView;
        }
    }
}
