package com.astarivi.kaizoyu.details.gui.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;


public class EpisodesRecyclerAdapter extends RecyclerView.Adapter<SharedEpisodeViewHolder> {
    @Setter
    private SharedEpisodeItemClickListener itemClickListener;
    private final List<Episode> items = new ArrayList<>();
    @Getter
    private final List<SharedEpisodeViewHolder> holders = new ArrayList<>();

    @Setter
    private Anime anime;

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
        final Episode episode = getEpisodeFromPosition(position);
        holder.setEpisode(episode);
        holder.setAnime(anime);
        holder.setListener(itemClickListener);

        holder.binding.episodeNumberCard.setText(
                String.valueOf(
                        episode.getNumber()
                )
        );

//        holder.binding.episodeTitle.setText(episode.getDefaultTitle());
        holder.checkSeen();

        holders.add(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void checkSeenAgain() {
        for (SharedEpisodeViewHolder holder : getHolders()) {
            holder.triggerSeenRefresh();
        }
    }

    public void replaceData(TreeSet<Episode> episodes) {
        items.clear();
        items.addAll(episodes);
    }

    private Episode getEpisodeFromPosition(int position) {
        return items.get(position);
    }

    public void destroy() {
        items.clear();
        holders.clear();
    }
}
