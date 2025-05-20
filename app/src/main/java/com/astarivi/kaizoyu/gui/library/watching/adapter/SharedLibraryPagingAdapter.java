package com.astarivi.kaizoyu.gui.library.watching.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.astarivi.kaizoyu.core.adapters.AnimePagingAdapter;
import com.astarivi.kaizoyu.core.adapters.AnimeViewHolder;
import com.astarivi.kaizoyu.core.models.anime.LocalAnime;
import com.astarivi.kaizoyu.utils.Translation;


public class SharedLibraryPagingAdapter extends AnimePagingAdapter<AnimeViewHolder<LocalAnime>, LocalAnime> {
    public SharedLibraryPagingAdapter(Consumer<LocalAnime> itemClickListener) {
        super(itemClickListener);
    }

    @Override
    public AnimeViewHolder<LocalAnime> onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType) {
        return new AnimeViewHolder<>(inflateView(parent), false);
    }

    @Override
    public void onBindViewHolderStarted(@NonNull AnimeViewHolder<LocalAnime> holder, int position, LocalAnime localAnime) {
        holder.binding.launchDate.setVisibility(View.GONE);

        assert localAnime.getSubtype() != null;
        holder.binding.subtype.setText(
                Translation.getSubTypeTranslation(
                        localAnime.getSubtype(),
                        holder.binding.getRoot().getContext()
                )
        );

        holder.binding.schedule.setVisibility(View.GONE);
    }
}
