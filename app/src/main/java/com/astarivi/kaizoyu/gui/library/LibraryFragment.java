package com.astarivi.kaizoyu.gui.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.databinding.FragmentLibraryBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.library.adapter.LibraryRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;


public class LibraryFragment extends Fragment {
    private FragmentLibraryBinding binding;
    private LibraryViewModel viewModel;
    private LibraryRecyclerAdapter adapter;

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel == null || adapter == null || binding == null) return;

        checkForRefresh();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);

        // RecyclerView
        RecyclerView recyclerView = binding.libraryContents;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        adapter = new LibraryRecyclerAdapter(anime -> {
            Intent intent = new Intent(getActivity(), AnimeDetailsActivity.class);
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.LOCAL.name());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        viewModel.getAnimeList().observe(getViewLifecycleOwner(), localAnime -> {
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
    public void onDestroyView() {
        super.onDestroyView();
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