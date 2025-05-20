package com.astarivi.kaizoyu.gui.home.recycler.recommendations;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.astarivi.kaizoyu.core.adapters.AnimePagingAdapter;
import com.astarivi.kaizoyu.core.adapters.AnimeViewHolder;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.utils.Translation;
import com.astarivi.kaizoyu.utils.Utils;


public class HomePagingAdapter extends AnimePagingAdapter<AnimeViewHolder<RemoteAnime>, RemoteAnime> {
    public HomePagingAdapter(Consumer<RemoteAnime> itemClickListener) {
        super(itemClickListener);
    }

    @Override
    public AnimeViewHolder<RemoteAnime> onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType) {
        return new AnimeViewHolder<>(inflateView(parent), true);
    }

    @Override
    public void onBindViewHolderStarted(@NonNull AnimeViewHolder<RemoteAnime> holder, int position, RemoteAnime anime) {
        String animeStartDate = anime.getInternal().attributes.startDate;

        if (animeStartDate != null && !animeStartDate.isEmpty()) {
            holder.binding.launchDate.setText(
                    Utils.getDateAsQuarters(animeStartDate)
            );
        } else {
            holder.binding.launchDate.setVisibility(View.GONE);
        }

        holder.binding.subtype.setText(
                Translation.getSubTypeTranslation(
                        anime.getInternal().attributes.subtype,
                        holder.binding.getRoot().getContext()
                )
        );

        holder.binding.schedule.setVisibility(View.GONE);
    }
}
