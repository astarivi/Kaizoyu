package com.astarivi.kaizoyu.video.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;

import java.util.Locale;


public class PlayerBarView extends LinearLayout {
    private MediaPlayer mediaPlayer;
    private LinearLayout playerInfoView;
    private PlayerBarBinding binding;
    private boolean isShowingBar = false;
    private boolean isInteractive = false;
    private View darkOverlay;
    private long pendingSkip;

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

    private void inflateView(Context context) {
        binding = PlayerBarBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
        );
    }

    @NotNull
    private String verboseMillisecondsToDuration(long duration) {
        if (duration < 0) return "??:??:??";


        long seconds = (duration / 1000) % 60 ;
        long minutes = (duration / (1000*60)) % 60;
        long hours = (duration / (1000*60*60)) % 24;

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setProgressFromTime(long time) {
        // Don't update the bar unless we're seeing it
        if (!isShowingBar) return;

        float length = mediaPlayer.getLength();
        float progress = (float) (((float) time / length) * 1000.0);

        binding.videoProgressBar.setProgress(
                Math.round(progress)
        );

        binding.currentTime.setText(
                verboseMillisecondsToDuration(time)
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
        if (!isShowingBar) return;
        binding.downloadSpeedMeter.setText(speed);
    }

    @SuppressLint("SetTextI18n")
    public void setMediaPlayer(MediaPlayer mPlayer, LinearLayout playerInfoView) {
        this.mediaPlayer = mPlayer;
        this.playerInfoView = playerInfoView;

        binding.currentTime.setText("00:00:00");
        binding.totalTime.setText(
                verboseMillisecondsToDuration(
                        mediaPlayer.getLength()
                )
        );

        binding.videoProgressBar.setProgress(0);
        this.setCacheProgress(0);

        this.takeControlOfUi();
    }

    private void takeControlOfUi() {
        mediaPlayer.setEventListener(event -> {
            switch(event.type) {
                case MediaPlayer.Event.Playing:
                    syncPlayButton(true);
                    break;
                case MediaPlayer.Event.EndReached:
                    this.setProgressFromTime(mediaPlayer.getLength());
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    syncPlayButton(false);
                    break;
                // TODO: Introduce some kind of message to go back, or show a button to replay
                case MediaPlayer.Event.TimeChanged:
                    this.setProgressFromTime(event.getTimeChanged());
                    break;
                case MediaPlayer.Event.LengthChanged:
                    binding.totalTime.setText(
                            verboseMillisecondsToDuration(
                                    event.getLengthChanged()
                            )
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

    public void initialize(View darkOverlay) {
        if (mediaPlayer == null) {
            Logger.error("Tried to play with a MediaPlayer that hasn't been set.");
            throw new RuntimeException("Tried to play with a MediaPlayer that doesn't exist. " +
                    "Please call setMediaPlayer(MediaPlayer mPlayer) first.");
        }

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

        // TODO Fix hide bar button not working correctly.
        binding.hideBar.setOnClickListener(v -> {
            isInteractive = false;
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
    }

    public void show() {
        if (isShowingBar) return;

        this.setVisibility(VISIBLE);
        playerInfoView.setVisibility(VISIBLE);
        darkOverlay.setVisibility(VISIBLE);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::hide, 4000);
        isShowingBar = true;
    }

    public void hide() {
        isShowingBar = false;

        if (isInteractive) {
            isInteractive = false;
            this.show();
            return;
        }

        this.setVisibility(INVISIBLE);
        darkOverlay.setVisibility(INVISIBLE);
        playerInfoView.setVisibility(INVISIBLE);
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
