package com.astarivi.kaizoyu.gui.library.watching.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.core.adapters.AnimeRecyclerAdapter;
import com.astarivi.kaizoyu.core.adapters.AnimeViewHolder;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.utils.Translation;
import com.astarivi.kaizoyu.utils.Utils;


public class WatchingRecyclerAdapter extends AnimeRecyclerAdapter<AnimeViewHolder<LocalAnime>, LocalAnime> {

    public WatchingRecyclerAdapter(AnimeViewHolder.ItemClickListener<LocalAnime> itemClickListener) {
        super(itemClickListener);
    }

    @Override
    public AnimeViewHolder<LocalAnime> onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType) {
        return new AnimeViewHolder<>(inflateView(parent), false);
    }

    @Override
    public void onBindViewHolderStarted(@NonNull AnimeViewHolder<LocalAnime> holder, int position, LocalAnime localAnime) {
        String animeStartDate = localAnime.getKitsuAnime().attributes.startDate;

        if (animeStartDate != null && !animeStartDate.equals("")) {
            holder.binding.launchDate.setText(
                    Utils.getDateAsQuarters(animeStartDate)
            );
        } else {
            holder.binding.launchDate.setVisibility(View.GONE);
        }

        holder.binding.subtype.setText(
                Translation.getSubTypeTranslation(
                        localAnime.getKitsuAnime().attributes.subtype,
                        holder.binding.getRoot().getContext()
                )
        );

        holder.binding.schedule.setVisibility(View.GONE);
    }
}
