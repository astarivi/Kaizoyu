package com.astarivi.kaizoyu.details.gui.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.models.episode.RemoteEpisode;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesItemBinding;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;


public class EpisodesRecyclerAdapter extends RecyclerView.Adapter<SharedEpisodeViewHolder> {
    @Setter
    private Consumer<RemoteEpisode> listener;
    private final ArrayList<RemoteEpisode> items = new ArrayList<>();
    @Getter
    private final ArrayList<SharedEpisodeViewHolder> holders = new ArrayList<>();
    @Setter
    private AnimeBasicInfo anime;

    @NonNull
    @NotNull
    @Override
    public SharedEpisodeViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        FragmentAnimeEpisodesItemBinding binding = FragmentAnimeEpisodesItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new SharedEpisodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SharedEpisodeViewHolder holder, int position) {
        final RemoteEpisode episode = getEpisodeFromPosition(position);
        holder.setEpisode(episode);
        holder.setAnime(() -> anime);
        holder.setListener(listener);

        holder.binding.episodeNumberCard.setText(
                String.valueOf(
                        episode.getNumber()
                )
        );

        String title = episode.getPreferredTitle();
        holder.binding.episodeTitle.setText(title != null ? title : "");

        holder.checkSeen();

        holders.add(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void checkSeenAgain() {
        for (SharedEpisodeViewHolder holder : getHolders()) {
            Data.getTemporarySwitches().setPendingSeenEpisodeStateRefresh(false);
            holder.checkSeen();
        }
    }

    public void replaceData(Set<RemoteEpisode> episodes) {
        items.clear();
        items.addAll(episodes);
    }

    private RemoteEpisode getEpisodeFromPosition(int position) {
        return items.get(position);
    }

    public void destroy() {
        items.clear();
        holders.clear();
    }
}
