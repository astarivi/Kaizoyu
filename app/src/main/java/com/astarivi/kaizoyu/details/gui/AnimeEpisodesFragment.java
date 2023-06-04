package com.astarivi.kaizoyu.details.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.databinding.ComponentSuggestionChipBinding;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.details.gui.recycler.EpisodesRecyclerAdapter;
import com.astarivi.kaizoyu.gui.adapters.BackInterceptAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.astarivi.kaizoyu.video.VideoPlayerActivity;
import com.google.android.material.chip.Chip;

import java.util.Locale;


public class AnimeEpisodesFragment extends Fragment implements BackInterceptAdapter {
    private FragmentAnimeEpisodesBinding binding;
    private AnimeEpisodesViewModelV2 viewModel;
    private EpisodesRecyclerAdapter adapter;
    private Anime anime;
    private Episode episode;

    public void scrollTop() {
        if (binding == null) return;
        binding.animeEpisodesRecycler.smoothScrollToPosition(0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnimeEpisodesBinding.inflate(inflater, container, false);

        // Anime object

        if (getArguments() != null) {
            anime = (Anime) Utils.getAnimeFromBundle(getArguments(), ModelType.Anime.BASE);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Anime object
        viewModel = new ViewModelProvider(this).get(AnimeEpisodesViewModelV2.class);

        // Reminder dialog

        String scheduleReminder = Data.getProperties(Data.CONFIGURATION.APP)
                .getProperty("episodes_reminder", "true");

        if (!Boolean.parseBoolean(scheduleReminder)) {
            binding.episodeCardButton.setVisibility(View.GONE);
        }

        binding.scheduleHideTip.setOnClickListener(v -> {
            binding.episodeCardButton.setVisibility(View.GONE);

            Data.getProperties(Data.CONFIGURATION.APP).setProperty("episodes_reminder", "false");
        });

        // Mixed

        binding.episodeSelectorScroll.setVisibility(View.GONE);
        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);

        // Recycler View

        RecyclerView recyclerView = binding.animeEpisodesRecycler;
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new EpisodesRecyclerAdapter();
        adapter.setItemClickListener(this::searchEpisode);
        adapter.setAnime(anime);
        recyclerView.setAdapter(adapter);

        // ViewModel shenanigans

        viewModel.getEpisodes().observe(getViewLifecycleOwner(), episodes -> {
            if (episodes == null) {
                binding.loadingBar.setVisibility(View.VISIBLE);
                binding.animeEpisodesRecycler.setVisibility(View.INVISIBLE);
                recyclerLayoutManager.scrollToPosition(0);
                return;
            }

            binding.loadingBar.setVisibility(View.GONE);
            binding.animeEpisodesRecycler.setVisibility(View.VISIBLE);

            adapter.replaceData(episodes);
            adapter.notifyDataSetChanged();
        });

        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            KitsuAnime kitsuAnime = anime.getKitsuAnime();
            int kitsuId = Integer.parseInt(kitsuAnime.id);

            Kitsu kitsu = new Kitsu(
                    Data.getUserHttpClient()
            );

            int animeLength = kitsu.getAnimeEpisodesLength(
                    kitsuId
            );

            binding.getRoot().post(() -> {
                if (animeLength > 20) {
                    populateChips(animeLength);
                }

                viewModel.initialize(
                        kitsuId,
                        animeLength
                );
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapter == null) return;

        if (Data.getTemporarySwitches().isPendingSeenEpisodeStateRefresh()) {
            adapter.checkSeenAgain();

            ((AnimeDetailsActivity) requireActivity()).triggerFavoriteRefresh();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean shouldFragmentInterceptBack() {
        ((AnimeDetailsActivity) requireActivity()).setCurrentFragment(0);
        return true;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        ((AnimeDetailsActivity) requireActivity()).changePagerInteractivity(!menuVisible);
    }

    private void disableChips(CompoundButton activeButton) {
        for (int i = 0; i < binding.episodeSelectorChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.episodeSelectorChips.getChildAt(i);
            chip.setChecked(false);
        }

        activeButton.setChecked(true);
    }

    private void populateChips(int animeLength) {
        binding.episodeSelectorScroll.setVisibility(View.VISIBLE);

        int totalPages = (int) Math.ceil((double) animeLength / 20);
        int currentPage = viewModel.getCurrentPage() != -1 ? viewModel.getCurrentPage() : 1;

        for (int i = 1; i < totalPages + 1; i++) {
            ComponentSuggestionChipBinding chipBinding = ComponentSuggestionChipBinding.inflate(
                    getLayoutInflater(),
                    binding.episodeSelectorChips,
                    true
            );

            if (i == currentPage) {
                chipBinding.getRoot().setChecked(true);
            }

            chipBinding.getRoot().setTextSize(12F);

            int[] pageValues = Utils.paginateNumber(i, animeLength, 20);

            chipBinding.getRoot().setText(
                    String.format(Locale.ENGLISH, "%d - %d", pageValues[0], pageValues[1])
            );

            int finalI = i;
            chipBinding.getRoot().setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (viewModel.isFetching()) {
                    return;
                }

                if (isChecked) {
                    showPage(finalI);
                    disableChips(buttonView);
                } else {
                    buttonView.setChecked(true);
                }
            });
        }
    }

    private void showPage(int page) {
        viewModel.setPage(page);
    }

    private void searchEpisode(@Nullable Episode episode) {
        if (episode == null) return;
        this.episode = episode;

        viewModel.searchEpisodeAndDisplayResults(
                episode,
                anime,
                binding,
                requireActivity(),
                this::playResult
        );
    }

    private void playResult(Result result) {
        AnimeDetailsActivity activity = (AnimeDetailsActivity) requireActivity();

        Intent intent = new Intent(activity, VideoPlayerActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("anime", anime);
        intent.putExtra("type", ModelType.Anime.BASE.name());
        intent.putExtra("episode", episode);
        binding.getRoot().post(() -> activity.startActivity(intent));
    }
}