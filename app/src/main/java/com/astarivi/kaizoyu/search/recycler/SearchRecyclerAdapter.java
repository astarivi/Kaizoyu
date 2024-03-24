package com.astarivi.kaizoyu.search.recycler;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.core.adapters.AnimeRecyclerAdapter;
import com.astarivi.kaizoyu.core.adapters.AnimeViewHolder;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.utils.Translation;


public class SearchRecyclerAdapter extends AnimeRecyclerAdapter<AnimeViewHolder<Anime>, Anime> {

    public SearchRecyclerAdapter(AnimeViewHolder.ItemClickListener<Anime> itemClickListener) {
        super(itemClickListener);
    }

    @Override
    public AnimeViewHolder<Anime> onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType) {
        return new AnimeViewHolder<>(inflateView(parent), true);
    }

    @Override
    public void onBindViewHolderStarted(@NonNull AnimeViewHolder<Anime> holder, int position, Anime anime) {
        String animeStartDate = anime.getAniListAnime().startDate.getDateAsQuarters();

        if (animeStartDate != null && !animeStartDate.isEmpty()) {
            holder.binding.launchDate.setText(
                    animeStartDate
            );
        } else {
            holder.binding.launchDate.setVisibility(View.GONE);
        }

        holder.binding.subtype.setText(
                Translation.getSubTypeTranslation(
                        anime.getAniListAnime().subtype,
                        holder.binding.getRoot().getContext()
                )
        );

        holder.binding.schedule.setVisibility(View.GONE);
    }
}
