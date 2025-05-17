package com.astarivi.kaizoyu.gui.home.recycler.recommendations;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.databinding.FragmentHomeItemBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.google.android.material.carousel.CarouselLayoutManager;

import java.util.ArrayList;


public class HomeMainRecyclerAdapter extends RecyclerView.Adapter<HomeMainRecyclerAdapter.HomeMainViewHolder> {
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private final Consumer<RemoteAnime> itemClickListener;
    private final ArrayList<MainCategoryContainer> items = new ArrayList<>();

    public HomeMainRecyclerAdapter(Consumer<RemoteAnime> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HomeMainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentHomeItemBinding binding = FragmentHomeItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new HomeMainViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMainViewHolder holder, int position) {
        MainCategoryContainer container = getItemFromPosition(position);

        final RecyclerView childRecycler = holder.binding.homeItemsRecycler;

        holder.binding.homeItemsTitle.setText(
                container.getVerboseTitle(
                        holder.binding.getRoot().getContext()
                )
        );

        HomeRecyclerAdapter childAdapter = new HomeRecyclerAdapter();
        childAdapter.setItemClickListener(itemClickListener);
        childAdapter.replaceData(container.getAnime());

        if (Data.isDeviceLowSpec()) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    childRecycler.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            );

            layoutManager.setInitialPrefetchItemCount(
                    container.getAnime().size()
            );
            childRecycler.setLayoutManager(layoutManager);
        } else {
            childRecycler.setLayoutManager(new CarouselLayoutManager());
        }

        childRecycler.setAdapter(childAdapter);
        childRecycler.setRecycledViewPool(viewPool);
    }

    public MainCategoryContainer getItemFromPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public synchronized void replaceData(ArrayList<MainCategoryContainer> item) {
        items.clear();
        items.addAll(item);
        notifyItemInserted(item.size() - 1);
    }

    public synchronized void clearData() {
        items.clear();
        notifyDataSetChanged();
    }

    public static class HomeMainViewHolder extends RecyclerView.ViewHolder {
        public FragmentHomeItemBinding binding;

        public HomeMainViewHolder(@NonNull FragmentHomeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
