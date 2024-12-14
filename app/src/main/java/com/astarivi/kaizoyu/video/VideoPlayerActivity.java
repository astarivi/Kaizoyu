package com.astarivi.kaizoyu.video;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisode;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivityVideoPlayerBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.video.gui.PlayerSkipView;
import com.astarivi.kaizoyu.video.gui.PlayerView;
import com.astarivi.kaizoyu.video.utils.AnimeEpisodeManager;
import com.astarivi.kaizoyu.video.utils.BundleUtils;
import com.astarivi.zparc.Zparc;
import com.google.android.material.snackbar.Snackbar;

import org.tinylog.Logger;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

import in.basulabs.audiofocuscontroller.AudioFocusController;


public class VideoPlayerActivity extends AppCompatActivityTheme {
    private ActivityVideoPlayerBinding binding;
    private VideoPlayerViewModel viewModel;
    private GestureDetectorCompat gestureDetector;
    private Zparc spark;
    private AnimeEpisodeManager animeEpisodeManager = null;
    private boolean isPlaying = false;
    private AudioFocusController audioFocusController = null;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (binding == null || !isPlaying || intent.getExtras() == null) return;

            MediaPlayer mediaPlayer = binding.mainPlayer.getMediaPlayer();
            if (mediaPlayer == null) return;

            BundleUtils.PictureInPictureAction action = BundleUtils.PictureInPictureAction.valueOf(
                    intent.getExtras().getString("action")
            );

            switch(action) {
                case PAUSE_OR_RESUME:
                    if (mediaPlayer.isPlaying()) {
                        audioFocusController.abandonFocus();
                        mediaPlayer.pause();
                    } else {
                        audioFocusController.requestFocus();
                        mediaPlayer.play();
                    }
                    break;
                case REWIND_TEN:
                    PlayerSkipView skipView = binding.mainPlayer.getSkipManager();

                    if (skipView == null) return;

                    skipView.skipBack();
                    break;
                case FORWARD_TEN:
                    PlayerSkipView skip = binding.mainPlayer.getSkipManager();

                    if (skip == null) return;

                    skip.skipAhead();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(VideoPlayerViewModel.class);

        // Bundle verification
        final Bundle bundle = getIntent().getExtras();

        if (bundle == null) return;

        final Result result = BundleUtils.getResultFromBundle(bundle);
        if (result == null) {
            finish();
            return;
        }

        // If we're operating in basic mode, decode from bundle.
        try {
            animeEpisodeManager = new AnimeEpisodeManager.Builder(bundle)
                    .build();
        } catch (IllegalArgumentException illegalArgumentException) {
            // We don't have enough data to operate
            if (!bundle.getBoolean("isAdvancedMode", false)) {
                finish();
                return;
            }
        }

        Logger.info("Bundle decoded");

        // Audio controller

        audioFocusController = new AudioFocusController.Builder(this)
                .setAcceptsDelayedFocus(true)
                .setPauseWhenAudioIsNoisy(false)
                .setPauseWhenDucked(false)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setDurationHint(AudioManager.AUDIOFOCUS_GAIN)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setStream(AudioManager.STREAM_SYSTEM)
                .setAudioFocusChangeListener(new AudioFocusController.OnAudioFocusChangeListener() {
                    @Override
                    public void decreaseVolume() {

                    }

                    @Override
                    public void increaseVolume() {

                    }

                    @Override
                    public void pause() {
                        if (binding == null || !isPlaying) return;

                        MediaPlayer mediaPlayer = binding.mainPlayer.getMediaPlayer();
                        if (mediaPlayer == null) return;

                        mediaPlayer.pause();
                    }

                    @Override
                    public void resume() {
                        if (binding == null || !isPlaying) return;

                        MediaPlayer mediaPlayer = binding.mainPlayer.getMediaPlayer();
                        if (mediaPlayer == null) return;

                        mediaPlayer.play();
                    }
                })
                .build();

        // Hide stuff
        binding.mainPlayer.setVisibility(View.INVISIBLE);

        // Prepare progress bar
        binding.initialDownloadProgress.setIndeterminate(false);
        binding.initialDownloadProgress.setProgress(0);
        binding.initialDownloadProgress.setMax(10);

        // Everything else
        binding.downloadStatus.setText(getResources().getString(R.string.executing_irc_handshake));
        gestureDetector = new GestureDetectorCompat(this, new PlayerGestureListener());

        binding.mainPlayer.initialize(
                animeEpisodeManager != null ? animeEpisodeManager.getAnimeTitle() : result.getCleanedFilename(),
                animeEpisodeManager != null ? animeEpisodeManager.getEpisodeTitle(this) : getString(R.string.advanced_mode_title),
                new PlayerView.PlayerEventListener() {
                    @Override
                    public void onBackPressed() {
                        finish();
                    }

                    @Override
                    public void onPlayingStateChanged(boolean isPlaying) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode()) {
                            setPictureInPictureParams(makeParams(isPlaying));
                        }
                    }

                    @Override
                    public void onVideoFinished() {
                        finish();
                    }
                },
                audioFocusController
        );
        Logger.info("Initialized main player");
        // Fullscreen setup

        // Draw behind screen cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            final WindowManager.LayoutParams wManager = getWindow().getAttributes();
            wManager.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Draw behind system bars (edge to edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false); // This doesn't seem to do the trick by itself
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // Show system bars only when swiping down, and hide them again automatically
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        // Actually hide the goddamn bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        Logger.info("System bars hidden.");

        spark = new Zparc.Builder(this)
                .setView(binding.getRoot())
                .setDuration(4000)
                .setAnimList(Zparc.ANIM_BLUE_PURPLE)
                .build();

        // Handle errors
        viewModel.getIrcFailure().observe(this, failureCode -> {
            if (failureCode == null) return;

            String message;

            switch (failureCode) {
                case TimeOut:
                    message = "Connection to IRC has timed out. Check your internet connection and retry later.";
                    break;
                case NoQuickRetry:
                    message = "You have been rate-limited by the server. Please wait some minutes.";
                    break;
                case StrictModeFailure:
                    message = "Strict mode has stopped this connection to protect your privacy.";
                    break;
                case BlacklistedIp:
                    message = "Your IP seems to have been blacklisted. Try disabling/changing your VPN.";
                    break;
                case GenericHandshakeError:
                    message = "The server has declined your handshake. Is your connection private?";
                    break;
                case BotNotFound:
                    message = "The option you selected is currently offline. Please try another one.";
                    break;
                default:
                    message = "There was a general I/O exception. Check your internet connection, and/or app permissions.";
                    break;
            }

            AnalyticsClient.onError("irc_failure", result.getContents(), failureCode.name());

            Snackbar.make(
                    binding.getRoot(),
                    message,
                    Snackbar.LENGTH_LONG
            ).show();

            delayedExit();
        });

        viewModel.getXdccFailure().observe(this, xdccFailure -> {
            if (xdccFailure == null) return;

            String message;

            switch (xdccFailure) {
                case ConnectionLost:
                    message = "Connection to remote server lost, buffering has stopped.";
                    break;
                case TimedOut:
                    message = "Remote server didn't respond, please try another bot.";
                    delayedExit();
                    break;
                case UnknownHost:
                    message = "Couldn't find the remote host, please try another bot.";
                    delayedExit();
                    break;

                default:
                    message = "There was a general I/O exception. Check your internet connection, and/or app permissions.";
                    delayedExit();
                    break;
            }

            AnalyticsClient.onError("xdcc_failure", result.getContents(), xdccFailure.name());

            Snackbar.make(
                    binding.videoBackground,
                    message,
                    Snackbar.LENGTH_LONG
            ).show();

            if (!isPlaying) delayedExit();
        });

        // Handle state
        viewModel.getProgress().observe(this, progressSpeedPair -> {
            if (progressSpeedPair == null) return;

            if (isPlaying){
                binding.mainPlayer.setCacheProgress(progressSpeedPair.first);
                binding.mainPlayer.setDownloadSpeed(progressSpeedPair.second);
                return;
            }

            binding.initialDownloadProgress.setProgress(progressSpeedPair.first);
            binding.downloadSpeed.setText(progressSpeedPair.second);
            binding.downloadStatus.setText(getResources().getString(R.string.downloading_buffer));
        });

        viewModel.getDownloadFile().observe(this, file -> {
            if (file == null) {
                finish();
                return;
            }

            if (isDestroyed()) return;

            if (spark != null) spark.stopAnimation();
            binding.getRoot().setBackgroundColor(Color.BLACK);
            binding.initialDownloadProgress.setVisibility(View.GONE);
            binding.downloadSpeed.setVisibility(View.GONE);
            binding.downloadStatus.setVisibility(View.GONE);
            binding.loadingBanner.hideBanner();

            audioFocusController.requestFocus();
            binding.mainPlayer.play(file);
            binding.mainPlayer.setVisibility(View.VISIBLE);
            isPlaying = true;
        });

        Logger.info("Observers set.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IntentFilter filter = new IntentFilter("PIP_PLAY_PAUSE_PLAYER");
            ContextCompat.registerReceiver(this, broadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            SeenEpisode currentSeenEpisode = Data.getRepositories()
                    .getSeenAnimeRepository()
                    .getSeenEpisodeDao()
                    .getEpisodeWith(
                            Math.toIntExact(animeEpisodeManager.getAnime().getAniListAnime().id),
                            animeEpisodeManager.getEpisode().getNumber()
                    );

            int currentPosition = currentSeenEpisode.episode.currentPosition;

            if (currentPosition == 0 || currentPosition == -1) return;

            Logger.info(
                    "Setting tick from seen episode, timestamp {}, episodeKitsuId {}",
                    currentSeenEpisode.episode.currentPosition,
                    currentSeenEpisode.episode.kitsuId
            );

            binding.mainPlayer.getPlayerBar().setTickTimestamp(currentPosition);
        });

        viewModel.startDownload(this, result);
        Logger.info("Download started, now it's all up to the ViewModel.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPlaying) spark.startAnimation();
    }

    @Override
    protected void onUserLeaveHint() {
        MediaPlayer mMediaPlayer = binding.mainPlayer.getMediaPlayer();
        if (mMediaPlayer == null || !isPlaying) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && mMediaPlayer.isPlaying()
                && !isInPictureInPictureMode()
        ) {
            binding.mainPlayer.getPlayerBar().forceHide();
            enterPictureInPictureMode(makeParams(true));
        } else {
            mMediaPlayer.pause();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private PictureInPictureParams makeParams(boolean isPlaying) {
        List<RemoteAction> remoteActions = new ArrayList<>();

        Intent rewindIntent = new Intent("PIP_PLAY_PAUSE_PLAYER");
        rewindIntent.setPackage(getPackageName());
        rewindIntent.putExtra("action", BundleUtils.PictureInPictureAction.REWIND_TEN.name());

        // Rewind
        remoteActions.add(
            new RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_pip_rewind),
                "Fast Rewind",
                "Rewind by 10s",
                PendingIntent.getBroadcast(
                        this,
                        2,
                        rewindIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
            )
        );

        // Pause - Resume action
        Intent repauseIntent = new Intent("PIP_PLAY_PAUSE_PLAYER");
        repauseIntent.setPackage(getPackageName());
        repauseIntent.putExtra("action", BundleUtils.PictureInPictureAction.PAUSE_OR_RESUME.name());

        remoteActions.add(
            new RemoteAction(
                isPlaying ? Icon.createWithResource(this, R.drawable.ic_pip_pause) : Icon.createWithResource(this, R.drawable.ic_pip_play),
                "Pause / Resume",
                "Pause, or resume playback",
                PendingIntent.getBroadcast(
                        this,
                        isPlaying ? 0 : 1,
                        repauseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
            )
        );

        // Forward
        Intent forwardIntent = new Intent("PIP_PLAY_PAUSE_PLAYER");
        forwardIntent.setPackage(getPackageName());
        forwardIntent.putExtra("action", BundleUtils.PictureInPictureAction.FORWARD_TEN.name());

        remoteActions.add(
                new RemoteAction(
                        Icon.createWithResource(this, R.drawable.ic_pip_forward),
                        "Fast Forward",
                        "Forward by 10s",
                        PendingIntent.getBroadcast(
                                this,
                                3,
                                forwardIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        )
                )
        );

        return new PictureInPictureParams.Builder()
                .setActions(remoteActions)
                .build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayer mMediaPlayer = binding.mainPlayer.getMediaPlayer();
        if (mMediaPlayer == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode()) {
            audioFocusController.abandonFocus();
            mMediaPlayer.pause();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            audioFocusController.abandonFocus();
            mMediaPlayer.pause();
        }

        saveCurrentProgress();
    }

    private void saveCurrentProgress() {
        MediaPlayer mMediaPlayer = binding.mainPlayer.getMediaPlayer();

        if (mMediaPlayer == null || !isPlaying || animeEpisodeManager == null || mMediaPlayer.getTime() == -1) return;

        Logger.debug("Saving episode progress");
        animeEpisodeManager.saveProgress(
                (int) mMediaPlayer.getTime(),
                (int) mMediaPlayer.getLength()
        );

        Logger.debug("Episode progress saved");
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
            saveCurrentProgress();
        }

        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isPlaying) spark.stopAnimation();
        if (binding.mainPlayer.getMediaPlayer() != null) {
            audioFocusController.abandonFocus();
            binding.mainPlayer.getMediaPlayer().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mainPlayer.destroy();
        viewModel.destroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void delayedExit(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::finish, 5000);
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
            if (!isPlaying) return true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) return true;

            binding.mainPlayer.getPlayerBar().show();

            return super.onSingleTapConfirmed(event);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
            if (!isPlaying) return true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) return true;

            PlayerSkipView skipView = binding.mainPlayer.getSkipManager();

            if (skipView == null) return true;

            int eventX = (int)event.getX();
            int eventY = (int)event.getY();
            int height = binding.getRoot().getHeight();
            int width = binding.getRoot().getWidth();
            int halfWidth = width / 2;

            if (eventX > halfWidth) {
                skipView.skipAhead();
            } else {
                skipView.skipBack();
            }

            return super.onDoubleTap(event);
        }
    }

    @Override
    public void finish() {
        if (viewModel != null) viewModel.destroy();

        super.finish();
    }
}
