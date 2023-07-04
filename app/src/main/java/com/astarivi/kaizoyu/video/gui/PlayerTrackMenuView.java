package com.astarivi.kaizoyu.video.gui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.databinding.PlayerBinding;
import com.astarivi.kaizoyu.databinding.PlayerTrackItemBinding;
import com.astarivi.kaizoyu.databinding.PlayerTrackMenuBinding;

import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;


public class PlayerTrackMenuView extends LinearLayout {
    private PlayerTrackMenuBinding binding;
    private PlayerTrackItemBinding selectedAudio;
    private String selectedAudioId;
    private PlayerTrackItemBinding selectedSubtitles;
    private String selectedSubtitlesId;
    private LayoutInflater inflater;

    private boolean shouldResume = false;

    // region Constructors
    public PlayerTrackMenuView(Context context) {
        super(context);
        this.inflateView(context);
    }

    public PlayerTrackMenuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.inflateView(context);
    }

    public PlayerTrackMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.inflateView(context);
    }

    public PlayerTrackMenuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.inflateView(context);
    }
    // endregion

    private void inflateView(Context context) {
        inflater = LayoutInflater.from(context);

        binding = PlayerTrackMenuBinding.inflate(
                inflater,
                this,
                true
        );
    }

    public void initialize(MediaPlayer mediaPlayer, boolean shouldResume) {
        this.shouldResume = shouldResume;

        this.addTracks(
                mediaPlayer.getSelectedTrack(IMedia.Track.Type.Audio),
                mediaPlayer.getSelectedTrack(IMedia.Track.Type.Text),
                mediaPlayer.getTracks(IMedia.Track.Type.Audio),
                mediaPlayer.getTracks(IMedia.Track.Type.Text)
        );

        binding.closePopupBtn.setOnClickListener(v -> close(mediaPlayer));
    }

    private void addTracks(IMedia.Track currentAudioTrack, IMedia.Track currentSubtitleTrack,
                           IMedia.Track[] audioTracks, IMedia.Track[] subtitleTracks) {
        final LinearLayout audioList = binding.audioList;
        final LinearLayout subtitlesList = binding.subtitlesList;

        for (IMedia.Track track : audioTracks) {
            final PlayerTrackItemBinding itemBinding = PlayerTrackItemBinding.inflate(
                    inflater,
                    this,
                    false
            );

            itemBinding.itemText.setText(
                    String.format(
                            "%s %s",
                            track.language,
                            track.name
                    ).trim(
                    )
            );

            itemBinding.rootLayout.setOnClickListener(v -> onAudioItemClick(track.id, itemBinding));

            if (currentAudioTrack != null && track.id.equals(currentAudioTrack.id)) {
                itemBinding.itemCheck.setVisibility(VISIBLE);
                selectedAudioId = track.id;
                selectedAudio = itemBinding;
                audioList.addView(itemBinding.rootLayout, 0);
                continue;
            }

            audioList.addView(itemBinding.rootLayout);
        }

        if (subtitleTracks != null) {
            for (IMedia.Track track : subtitleTracks) {
                final PlayerTrackItemBinding itemBinding = PlayerTrackItemBinding.inflate(
                        inflater,
                        this,
                        false
                );

                itemBinding.itemText.setText(
                        String.format(
                                "%s %s",
                                track.language,
                                track.name
                        ).trim(
                        )
                );

                itemBinding.rootLayout.setOnClickListener(v -> onSubtitlesItemClick(track.id, itemBinding));

                if (currentSubtitleTrack != null && track.id.equals(currentSubtitleTrack.id)) {
                    itemBinding.itemCheck.setVisibility(VISIBLE);
                    selectedSubtitlesId = track.id;
                    selectedSubtitles = itemBinding;
                    subtitlesList.addView(itemBinding.rootLayout, 0);
                    continue;
                }

                subtitlesList.addView(itemBinding.rootLayout);
            }
        }

        // Create options to disable tracks

        final PlayerTrackItemBinding noSubtitlesBinding = PlayerTrackItemBinding.inflate(
                inflater,
                this,
                false
        );

        noSubtitlesBinding.itemText.setText(
                R.string.no_subtitles_choice
        );

        noSubtitlesBinding.rootLayout.setOnClickListener(v -> onSubtitlesItemClick(null, noSubtitlesBinding));

        if (selectedSubtitles == null) {
            selectedSubtitles = noSubtitlesBinding;
            selectedSubtitlesId = null;
            noSubtitlesBinding.itemCheck.setVisibility(VISIBLE);
            subtitlesList.addView(noSubtitlesBinding.rootLayout, 0);
        } else {
            subtitlesList.addView(noSubtitlesBinding.rootLayout);
        }
    }

    private void onAudioItemClick(String id, PlayerTrackItemBinding binding) {
        if (selectedAudio != null) {
            selectedAudio.itemCheck.setVisibility(INVISIBLE);
        }
        selectedAudio = binding;
        selectedAudio.itemCheck.setVisibility(VISIBLE);
        selectedAudioId = id;
    }

    private void onSubtitlesItemClick(String id, PlayerTrackItemBinding binding) {
        if (selectedSubtitles != null) {
            selectedSubtitles.itemCheck.setVisibility(INVISIBLE);
        }
        selectedSubtitles = binding;
        selectedSubtitles.itemCheck.setVisibility(VISIBLE);
        selectedSubtitlesId = id;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        this.exit();
    }

    private void close(MediaPlayer mediaPlayer){
        if (selectedAudioId == null) {
            mediaPlayer.unselectTrackType(IMedia.Track.Type.Audio);
        } else {
            mediaPlayer.unselectTrackType(IMedia.Track.Type.Audio);
            mediaPlayer.selectTrack(selectedAudioId);
        }

        if (selectedSubtitlesId == null) {
            mediaPlayer.unselectTrackType(IMedia.Track.Type.Text);
        } else {
            mediaPlayer.selectTrack(selectedSubtitlesId);
        }

        if (shouldResume) mediaPlayer.play();

        this.exit();
    }

    private void exit() {
        // Self destruction go brr
        this.removeAllViews();
        if (this.getParent() != null) {
            ((ViewGroup) this.getParent()).removeView(this);
        }
        binding = null;
        selectedAudio = null;
        selectedAudioId = null;
        selectedSubtitles = null;
        selectedSubtitlesId = null;
        inflater = null;
    }

    public PlayerTrackMenuBinding getBinding() {
        return this.binding;
    }

    public static void show(
            Context context,
            PlayerBinding playerBinding,
            MediaPlayer mediaPlayer
    ) {
        boolean shouldResume = mediaPlayer.isPlaying();

        mediaPlayer.pause();

        final PlayerTrackMenuView view = new PlayerTrackMenuView(context);

        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout = playerBinding.playerBackground;

        view.initialize(mediaPlayer, shouldResume);
        view.setId(View.generateViewId());

        layout.addView(
                view,
                layout.getChildCount()
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
}