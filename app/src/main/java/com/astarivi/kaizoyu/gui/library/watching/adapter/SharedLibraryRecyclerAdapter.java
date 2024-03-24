package com.astarivi.kaizoyu.gui.library.watching.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.core.adapters.AnimeRecyclerAdapter;
import com.astarivi.kaizoyu.core.adapters.AnimeViewHolder;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.utils.Translation;


public class SharedLibraryRecyclerAdapter extends AnimeRecyclerAdapter<AnimeViewHolder<LocalAnime>, LocalAnime> {

    public SharedLibraryRecyclerAdapter(AnimeViewHolder.ItemClickListener<LocalAnime> itemClickListener) {
        super(itemClickListener);
    }

    @Override
    public AnimeViewHolder<LocalAnime> onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType) {
        return new AnimeViewHolder<>(inflateView(parent), false);
    }

    @Override
    public void onBindViewHolderStarted(@NonNull AnimeViewHolder<LocalAnime> holder, int position, LocalAnime localAnime) {
        String animeStartDate = localAnime.getAniListAnime().startDate.getDateAsQuarters();

        if (animeStartDate != null && !animeStartDate.isEmpty()) {
            holder.binding.launchDate.setText(
                    animeStartDate
            );
        } else {
            holder.binding.launchDate.setVisibility(View.GONE);
        }

        holder.binding.subtype.setText(
                Translation.getSubTypeTranslation(
                        localAnime.getAniListAnime().subtype,
                        holder.binding.getRoot().getContext()
                )
        );

        holder.binding.schedule.setVisibility(View.GONE);
    }
}
