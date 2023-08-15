package com.astarivi.kaizoyu.details.gui.recycler;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeWithEpisodes;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisode;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesItemBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SharedEpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public final FragmentAnimeEpisodesItemBinding binding;
    private SharedEpisodeItemClickListener listener;
    private Episode episode;
    private Anime anime;
    private List<SeenEpisode> seenEpisodes;

    public SharedEpisodeViewHolder(@NonNull @NotNull FragmentAnimeEpisodesItemBinding binding) {
        super(binding.getRoot());

        binding.getRoot().setOnClickListener(this);
        binding.getRoot().setOnLongClickListener(this);
        this.binding = binding;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;

        resetSeenStatus();
    }

    private void resetSeenStatus() {
        binding.getRoot().setCardBackgroundColor(
                MaterialColors.getColor(binding.getRoot().getContext(), R.attr.colorSecondaryContainer, Color.BLUE)
        );

        binding.episodeProgress.setVisibility(View.INVISIBLE);
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public void setListener(SharedEpisodeItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener == null) {
            Logger.debug("Listener for items at episodes recycler was null.");
            return;
        }

        listener.onItemClick(this.episode);
    }

    @Override
    public boolean onLongClick(@NonNull View v) {
        Context context = v.getContext();

        if (context == null) return false;

        final String[] options = new String[]{
                context.getString(R.string.episode_long_press_watched),
                context.getString(R.string.episode_long_press_unwatch)
        };

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                context
        );

        builder.setTitle(
                String.format(
                        Locale.ENGLISH,
                        context.getString(R.string.episode_long_press_title),
                        episode.getKitsuEpisode().attributes.number
                )
        );

        builder.setItems(options, (dialog, index) -> {
            if (index == 0) {
                KitsuEpisode.KitsuEpisodeAttributes attributes = episode.getKitsuEpisode().attributes;

                if (attributes.length == null || attributes.length <= 0) {
                    attributes.length = 24;
                }

                Data.getRepositories()
                        .getSeenAnimeRepository()
                        .saveSeenEpisodeAsync(
                                anime,
                                episode,
                                (int) TimeUnit.MINUTES.toMillis(
                                        attributes.length != null ? attributes.length : 24
                                ),
                                this::triggerSeenRefresh
                        );
            } else {
                Data.getRepositories()
                        .getSeenAnimeRepository()
                        .deleteSeenEpisodeAsync(
                                anime,
                                episode,
                                () -> {
                                    triggerSeenRefresh();
                                    resetSeenStatus();
                                }
                        );
            }
        });

        builder.show();

        return true;
    }

    private void markSeen(SeenEpisode seenEpisode) {
        binding.getRoot().setCardBackgroundColor(
                MaterialColors.getColor(binding.getRoot().getContext(), R.attr.colorPrimaryContainer, Color.BLUE)
        );

        if (seenEpisode.episode.length == 0 || seenEpisode.episode.currentPosition == 0) {
            return;
        }

        float approxRuntime = TimeUnit.MINUTES.toMillis(seenEpisode.episode.length);
        int runtimeDiff = Math.round((seenEpisode.episode.currentPosition / approxRuntime) * 100.0F);

        // Clamp
        int percentage = Math.max(
                0,
                Math.min(
                        100,
                        runtimeDiff
                )
        );

        binding.episodeProgress.setVisibility(View.VISIBLE);
        binding.episodeProgress.setProgressCompat(percentage, true);
    }

    public void checkSeen() {
        if (seenEpisodes == null) {
            triggerSeenRefresh();
            return;
        }

        if (seenEpisodes.isEmpty()) {
            return;
        }

        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            for (SeenEpisode seenEpisode : seenEpisodes) {
                if (seenEpisode.episode.episodeNumber == episode.getKitsuEpisode().attributes.number) {
                    binding.getRoot().post(() -> markSeen(seenEpisode));
                }
            }
        });
    }

    public void triggerSeenRefresh() {
        Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(false);

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            SeenAnimeWithEpisodes seenAnimeWithEpisodes = Data.getRepositories()
                    .getSeenAnimeRepository()
                    .getSeenAnimeDao()
                    .getRelationFromKitsuId(
                            episode.getAnimeId()
                    );

            if (seenAnimeWithEpisodes == null) {
                seenEpisodes = new ArrayList<>();
                return;
            }

            seenEpisodes = seenAnimeWithEpisodes.episodes;

            checkSeen();
        });
    }
}
