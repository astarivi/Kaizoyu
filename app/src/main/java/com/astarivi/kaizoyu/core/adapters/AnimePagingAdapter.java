package com.astarivi.kaizoyu.core.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.databinding.ItemAnimeBinding;

import org.tinylog.Logger;

public abstract class AnimePagingAdapter<VH extends AnimeViewHolder<A>, A extends AnimeBasicInfo> extends PagingDataAdapter<A, VH> {
    protected Consumer<A> itemClickListener;

    public AnimePagingAdapter(Consumer<A> itemClickListener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull A oldItem, @NonNull A newItem) {
                return oldItem.getKitsuId() == newItem.getKitsuId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull A oldItem, @NonNull A newItem) {
                return this.areItemsTheSame(oldItem, newItem);
            }
        });
        this.itemClickListener = itemClickListener;
    }

    public AnimePagingAdapter(@NonNull DiffUtil.ItemCallback<A> diffCallback, Consumer<A> itemClickListener) {
        super(diffCallback);
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final VH viewHolder = onCreateViewHolderStarted(parent, viewType);

        viewHolder.setItemClickListener(itemClickListener);

        return viewHolder;
    }

    public abstract VH onCreateViewHolderStarted(@NonNull ViewGroup parent, int viewType);

    public abstract void onBindViewHolderStarted(@NonNull VH holder, int position, A baseAnime);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final A anime = getItem(position);
        if (anime == null) {
            Logger.error("Null anime at abstract anime paged adapter");
            return;
        }

        holder.setAnime(anime);
        holder.removeFavorite();
        holder.binding.animeTitle.setText(anime.getPreferredTitle());

        LinearLayout icons = holder.binding.infoIcons;

        for (int i = 0; i < icons.getChildCount(); i++) {
            icons.getChildAt(i).setVisibility(View.VISIBLE);
        }

        onBindViewHolderStarted(holder, position, anime);

        holder.fetchImages();
        holder.checkFavorite();
    }

    protected ItemAnimeBinding inflateView(ViewGroup parent) {
        return ItemAnimeBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
    }
}
