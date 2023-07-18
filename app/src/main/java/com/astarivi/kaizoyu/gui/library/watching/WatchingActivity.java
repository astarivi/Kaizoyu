package com.astarivi.kaizoyu.gui.library.watching;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivityWatchingBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.library.watching.adapter.WatchingRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;


public class WatchingActivity extends AppCompatActivityTheme {
    private ActivityWatchingBinding binding;
    private WatchingViewModel viewModel;
    private WatchingRecyclerAdapter adapter;

    public WatchingActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWatchingBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WatchingViewModel.class);

        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);

        // RecyclerView
        RecyclerView recyclerView = binding.libraryContents;
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        adapter = new WatchingRecyclerAdapter(anime -> {
            Intent intent = new Intent(this, AnimeDetailsActivity.class);
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.LOCAL.name());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        viewModel.getAnimeList().observe(this, localAnime -> {
            if (localAnime == null) {
                binding.emptyLibraryPopup.setVisibility(View.VISIBLE);
                binding.loadingBar.setVisibility(View.GONE);
                binding.libraryContents.setVisibility(View.INVISIBLE);
                return;
            }

            manager.scrollToPosition(0);
            binding.loadingBar.setVisibility(View.GONE);
            binding.libraryContents.setVisibility(View.VISIBLE);

            adapter.replaceData(localAnime);
            adapter.notifyDataSetChanged();
        });

        viewModel.fetchFavorites(binding);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel == null || adapter == null || binding == null) return;

        checkForRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.clear();
    }

    private void checkForRefresh() {
        final Data.TemporarySwitches switches = Data.getTemporarySwitches();

        if (switches.isPendingFavoritesRefresh()) {
            switches.setPendingFavoritesRefresh(false);

            viewModel.fetchFavorites(binding);
        }
    }
}