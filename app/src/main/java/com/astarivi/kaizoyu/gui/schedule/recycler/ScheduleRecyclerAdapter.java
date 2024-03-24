package com.astarivi.kaizoyu.gui.schedule.recycler;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.core.adapters.AnimeRecyclerAdapter;
import com.astarivi.kaizoyu.core.adapters.AnimeViewHolder;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.astarivi.kaizoyu.utils.Translation;


public class ScheduleRecyclerAdapter extends AnimeRecyclerAdapter<AnimeViewHolder<SeasonalAnime>, SeasonalAnime> {

    public ScheduleRecyclerAdapter(AnimeViewHolder.ItemClickListener<SeasonalAnime> itemClickListener) {
        super(itemClickListener);
    }

    @Override
    public AnimeViewHolder<SeasonalAnime> onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType) {
        return new AnimeViewHolder<>(inflateView(parent), true);
    }

    @Override
    public void onBindViewHolderStarted(@NonNull AnimeViewHolder<SeasonalAnime> holder, int position, SeasonalAnime seasonalAnime) {
        holder.binding.launchDate.setVisibility(View.GONE);

        holder.binding.subtype.setText(
                Translation.getSubTypeTranslation(
                        seasonalAnime.getAniListAnime().subtype,
                        holder.binding.getRoot().getContext()
                )
        );

        holder.binding.schedule.setText(
                seasonalAnime.getEmissionTime()
        );

    }
}
