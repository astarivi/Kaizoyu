package com.astarivi.kaizoyu.details.gui.recycler;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.models.episode.RemoteEpisode;
import com.astarivi.kaizoyu.core.storage.database.repo.SavedShowRepo;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_episode.SavedEpisode;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesItemBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.Setter;


public class SharedEpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public final FragmentAnimeEpisodesItemBinding binding;
    private RemoteEpisode episode;
    @Setter
    private Supplier<AnimeBasicInfo> anime;
    @Setter
    private Consumer<RemoteEpisode> listener;

    public SharedEpisodeViewHolder(@NonNull @NotNull FragmentAnimeEpisodesItemBinding binding) {
        super(binding.getRoot());

        binding.getRoot().setOnClickListener(this);
        binding.getRoot().setOnLongClickListener(this);
        this.binding = binding;
    }

    public void setEpisode(RemoteEpisode episode) {
        this.episode = episode;

        resetSeenStatus();
    }

    private void resetSeenStatus() {
        binding.getRoot().setCardBackgroundColor(
                MaterialColors.getColor(binding.getRoot().getContext(), R.attr.colorSecondaryContainer, Color.BLUE)
        );

        binding.episodeProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (listener == null) {
            Logger.debug("Listener for items at episodes recycler was null.");
            return;
        }

        listener.accept(this.episode);
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
                        episode.getNumber()
                )
        );

        builder.setItems(options, (dialog, index) -> {
            if (index == 0) {
                if (episode.getLength() <= 0) {
                    episode.setLength(24);
                }

                SavedShowRepo.saveEpisodeAsync(
                        anime.get(),
                        episode,
                        (int) TimeUnit.MINUTES.toMillis(
                                episode.getLength()
                        ),
                        (result) -> this.checkSeen()
                );
            } else {
                Threading.database(() -> {
                    SavedShowRepo.getEpisodeDao().deleteByOwnKitsuId(
                            episode.getKitsuId()
                    );

                    Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(true);

                    binding.getRoot().post(this::checkSeen);
                });
            }
        });

        builder.show();

        return true;
    }

    private void markSeen(@NonNull SavedEpisode seenEpisode) {
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
        Threading.database(() -> {
            SavedEpisode savedEpisode = SavedShowRepo.getEpisodeDao().getByOwnKitsuId(episode.getKitsuId());

            if (savedEpisode != null) {
                binding.getRoot().post(() -> markSeen(savedEpisode));
            }
        });
    }
}
