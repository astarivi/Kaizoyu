package com.astarivi.kaizoyu.core.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.databinding.ItemAnimeBinding;

import java.util.ArrayList;
import java.util.List;


public abstract class AnimeRecyclerAdapter<VH extends AnimeViewHolder<A>, A extends Anime> extends RecyclerView.Adapter<VH> {
    protected final ArrayList<A> items = new ArrayList<>();
    protected AnimeViewHolder.ItemClickListener<A> itemClickListener;

    protected AnimeRecyclerAdapter(AnimeViewHolder.ItemClickListener<A> itemClickListener) {
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
        final A anime = getItemFromPosition(position);
        holder.setAnime(anime);
        holder.removeFavorite();
        holder.binding.animeTitle.setText(anime.getDisplayTitle());

        LinearLayout icons = holder.binding.infoIcons;

        for (int i = 0; i < icons.getChildCount(); i++) {
            icons.getChildAt(i).setVisibility(View.VISIBLE);
        }

        onBindViewHolderStarted(holder, position, anime);

        holder.fetchImages();
        holder.checkFavorite();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected ItemAnimeBinding inflateView(ViewGroup parent) {
        return ItemAnimeBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void replaceData(List<A> anime) {
        items.clear();
        items.addAll(anime);
    }

    private A getItemFromPosition(int position) {
        return items.get(position);
    }
}
