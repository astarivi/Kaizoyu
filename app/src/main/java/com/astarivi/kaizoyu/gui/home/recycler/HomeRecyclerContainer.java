package com.astarivi.kaizoyu.gui.home.recycler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.Anime;
import com.google.android.material.carousel.CarouselLayoutManager;

import java.util.List;


public class HomeRecyclerContainer {
    private final HomeRecyclerAdapter adapter;
    private final List<Anime> animeBase;

    public HomeRecyclerContainer(List<Anime> animeBase) {
        this.adapter = new HomeRecyclerAdapter();
        this.animeBase = animeBase;
    }

    public HomeRecyclerAdapter getAdapter() {
        return this.adapter;
    }

    public void initialize(@NonNull RecyclerView recyclerView, HomeRecyclerAdapter.ItemClickListener clickListener) {
        recyclerView.setLayoutManager(new CarouselLayoutManager());
        recyclerView.setAdapter(adapter);

        adapter.replaceData(animeBase);
        adapter.setItemClickListener(clickListener);
        adapter.notifyItemRangeInserted(0, animeBase.size());
    }
}
