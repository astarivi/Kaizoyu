package com.astarivi.kaizoyu.video;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.ViewModelProvider;

import com.astarivi.kaizolib.xdcc.base.XDCCDownloadListener;
import com.astarivi.kaizolib.xdcc.base.XDCCFailure;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.storage.AnimeEpisodeManager;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivityVideoPlayerBinding;
import com.astarivi.kaizoyu.video.gui.PlayerBarView;
import com.astarivi.kaizoyu.video.gui.PlayerTrackMenuView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Nullable;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.util.ArrayList;

import io.github.tonnyl.spark.Spark;


// FIXME: This whole activity is modernized ugly ass legacy code.
public class VideoPlayerActivity extends AppCompatActivityTheme {
    private Spark spark;
    private ActivityVideoPlayerBinding binding;
    private VideoPlayerViewModel viewModel;
    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private File downloadFile;
    private boolean isPlaying = false;
    private GestureDetectorCompat gestureDetector;
    private @Nullable AnimeEpisodeManager animeEpisodeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoPlayerBinding.inflate(this.getLayoutInflater());
        setContentView(binding.getRoot());

        downloadFile = getCacheDir();
        if (savedInstanceState != null) {
            String filePath = savedInstanceState.getString("file");
            if (filePath == null) return;

            downloadFile = new File(filePath);
            isPlaying = savedInstanceState.getBoolean("isPlaying");
            if (isPlaying) startPlayer();
        }

        // Bundle verification
        final Bundle bundle = getIntent().getExtras();

        if (bundle == null) return;

        final Result result = VideoPlayerUtils.getResultFromBundle(bundle);
        if (result == null) {
            finish();
            return;
        }

        // If we're operating in basic mode, decode from bundle.
        animeEpisodeManager = new AnimeEpisodeManager.Builder(bundle)
                .build();

        // This operation mode is not possible. We need show and episode data for basic operation.
        // Advanced operation only needs the Result object, but this mode must be explicitly declared.
        if (animeEpisodeManager == null && !bundle.getBoolean("isAdvancedMode", false)) {
            finish();
            return;
        }

        // Hide stuff
        binding.videoFrame.requestFocus();
        binding.videoFrame.setVisibility(View.INVISIBLE);
        binding.playerBar.setVisibility(View.INVISIBLE);
        binding.darkOverlay.setVisibility(View.INVISIBLE);
        binding.playerInfoBar.setVisibility(View.INVISIBLE);

        // Main progress bars
        binding.initialDownloadProgress.setIndeterminate(false);
        binding.initialDownloadProgress.setProgress(0);
        binding.initialDownloadProgress.setMax(10);

        // Player info bar
        binding.topBackButton.setOnClickListener(v -> finish());

        if (animeEpisodeManager != null) {
            binding.animeTitle.setText(animeEpisodeManager.getAnimeTitle());
            binding.episodeTitle.setText(animeEpisodeManager.getEpisodeTitle(this));
        } else {
            binding.animeTitle.setText(result.getCleanedFilename());
            binding.episodeTitle.setText(getString(R.string.advanced_mode_title));
        }

        binding.playerBar.setSubtitlesOnClickListener(v -> showLanguageSelector());

        viewModel = new ViewModelProvider(this).get(VideoPlayerViewModel.class);
        if (!viewModel.hasStarted()){
            viewModel.initialize(result.getXDCCCommand(), getCacheDir());
        }

        viewModel.setIrcOnFailureListener(f -> {
            //TODO Error handling
            String message;

            switch (f) {
                case NoQuickRetry:
                    message = "This provider doesn't support quick retry, please try again later (150 seconds max).";
                    break;
                case TimeOut:
                    message = "Connection to IRC has timed out. Check your internet connection and retry later.";
                    break;
                case UnknownHost:
                    message = "The IRC handshake server couldn't be reached. Check your internet connection.";
                    break;
                default:
                    message = "There was a general I/O exception. Check your internet connection, and/or app permissions.";
                    break;
            }

            AnalyticsClient.onError("irc_failure", result.getContents(), f.name());

            Snackbar.make(
                    binding.videoBackground,
                    message,
                    Snackbar.LENGTH_LONG
            ).show();

            delayedExit();
        });

        viewModel.setDccDownloadListener(new XDCCDownloadListener() {
            @Override
            public void onDownloadReadyToPlay(int progress, File df) {
                downloadFile = df;
                binding.getRoot().post(() -> startPlayer());
            }

            @Override
            public void onProgress(int progress, String speed) {
                // Blocking these methods will slow down the download speed.
                binding.getRoot().post(() -> {
                    if (isPlaying){
                        binding.playerBar.setDownloadSpeed(speed);
                        binding.playerBar.setCacheProgress(progress);
                        return;
                    }

                    binding.initialDownloadProgress.setProgress(progress);
                    binding.downloadSpeed.setText(speed);
                    binding.downloadStatus.setText(getResources().getString(R.string.downloading_buffer));
                });
            }

            @Override
            public void onFinished(File downloadFile) {
                binding.playerBar.post(() -> binding.playerBar.setCacheProgress(100));
            }

            @Override
            public void onError(XDCCFailure errorCode) {
                String message;

                switch (errorCode) {
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

                AnalyticsClient.onError("xdcc_failure", result.getContents(), errorCode.name());

                Snackbar.make(
                        binding.videoBackground,
                        message,
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });

        spark = new Spark.Builder()
                .setView(binding.videoBackground)
                .setDuration(4000)
                .setAnimList(Spark.ANIM_BLUE_PURPLE)
                .build();

        final ArrayList<String> args = new ArrayList<>();
        args.add("--file-caching=2000");
//        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);

        mMediaPlayer = new MediaPlayer(mLibVLC);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        viewModel.start();
        binding.downloadStatus.setText(getResources().getString(R.string.executing_irc_handshake));
        drawOnCutOut();

        gestureDetector = new GestureDetectorCompat(this, new PlayerGestureListener());

        binding.skipManager.initialize(binding.playerBar);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPlaying) spark.startAnimation(); // start animation on resume
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPlaying) spark.stopAnimation(); // stop animation on pause
        if (mMediaPlayer != null) mMediaPlayer.pause();
        if (isPlaying && mMediaPlayer != null && mMediaPlayer.getTime() != -1) saveSeenEpisode();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isPlaying) spark.stopAnimation();
        if (mMediaPlayer != null) mMediaPlayer.pause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hideSystemUI();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlaying", isPlaying);
        outState.putString("file", downloadFile.getAbsolutePath());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        } else {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.pause();
        mMediaPlayer.setMedia(null);
        mMediaPlayer.release();
        viewModel.stop();
        binding.playerBar.terminate();
    }

    private void drawOnCutOut(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            final WindowManager.LayoutParams wManager = getWindow().getAttributes();
            wManager.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUI(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) controller.hide(WindowInsets.Type.statusBars());
            if (controller != null) controller.hide(WindowInsets.Type.navigationBars());
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }

    private void delayedExit(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::finish, 5000);
    }

    private void showLanguageSelector() {
        boolean shouldResume = mMediaPlayer.isPlaying();

        mMediaPlayer.pause();

        final PlayerTrackMenuView view = new PlayerTrackMenuView(this);

        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout = binding.videoBackground;

        view.initialize(mMediaPlayer, shouldResume);
        view.setId(View.generateViewId());

        binding.videoBackground.addView(
                view,
                binding.videoBackground.getChildCount() - 1
        );

        int id = view.getId();
        set.clone(layout);
        set.connect(id, ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM, 0);
        set.connect(id, ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 0);
        set.connect(id, ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 0);
        set.connect(id, ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 0);
        set.constrainHeight(id, 0);
        set.constrainWidth(id, 0);
        set.applyTo(layout);
        view.requestLayout();
    }

    private void startPlayer(){
        if (isDestroyed()) return;

        PlayerBarView playerBar = binding.playerBar;
        if (spark != null) {
            spark.stopAnimation();
        }
        ConstraintLayout background = binding.videoBackground;
        background.setBackgroundColor(Color.BLACK);
        binding.initialDownloadProgress.setVisibility(View.GONE);
        binding.downloadSpeed.setVisibility(View.GONE);
        binding.downloadStatus.setVisibility(View.GONE);
        mMediaPlayer.attachViews(binding.videoFrame, null, true, true);
        playerBar.setMediaPlayer(mMediaPlayer, binding.playerInfoBar);
        final Media media = new Media(mLibVLC, Uri.fromFile(downloadFile));
        mMediaPlayer.setMedia(media);
        mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT);
        media.release();
        binding.videoFrame.setVisibility(View.VISIBLE);
        playerBar.initialize(binding.darkOverlay);
        playerBar.show();

        isPlaying = true;
        AnalyticsClient.logEvent("playing_anime");
    }

    private void saveSeenEpisode() {
        if (animeEpisodeManager == null) return;

        animeEpisodeManager.saveProgress(
                (int) mMediaPlayer.getTime(),
                (int) mMediaPlayer.getLength()
        );
    }

    public void doubleTap(int x, int y) {
        // FIXME: Modernize this
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int halfWidth = width / 2;

        if (x > halfWidth) {
            binding.skipManager.skipAhead();
        } else {
            binding.skipManager.skipBack();
        }
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (!isPlaying) return super.onSingleTapConfirmed(e);

            binding.playerBar.show();

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
            if (!isPlaying) return super.onDoubleTap(event);

            doubleTap(
                    (int)event.getX(),
                    (int)event.getY()
            );

            return super.onDoubleTap(event);
        }
    }
}