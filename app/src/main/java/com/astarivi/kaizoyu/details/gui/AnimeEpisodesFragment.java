package com.astarivi.kaizoyu.details.gui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizolib.kitsu.exception.ParsingException;
import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.models.anime.SeasonalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.models.base.EpisodeBasicInfo;
import com.astarivi.kaizoyu.core.search.SearchEnhancer;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.databinding.ComponentSuggestionChipBinding;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.details.gui.recycler.EpisodesRecyclerAdapter;
import com.astarivi.kaizoyu.gui.adapters.BackInterceptAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.astarivi.kaizoyu.video.VideoPlayerActivity;

import org.tinylog.Logger;

import java.util.Locale;


public class AnimeEpisodesFragment extends Fragment implements BackInterceptAdapter {
    private FragmentAnimeEpisodesBinding binding;
    private AnimeEpisodesViewModelV2 viewModel;
    private EpisodesRecyclerAdapter adapter;
    private AnimeBasicInfo anime;
    private EpisodeBasicInfo episode;
    private SearchEnhancer searchEnhancer = null;

    public void scrollTop() {
        if (binding == null) return;
        binding.animeEpisodesRecycler.smoothScrollToPosition(0);

        if (viewModel == null) return;
        viewModel.reload();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnimeEpisodesBinding.inflate(inflater, container, false);

        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments();
        } else if (savedInstanceState != null && anime == null) {
            bundle = savedInstanceState;
        } else {
            Logger.error("Couldn't initialize bundle at AnimeEpisodesFragment");
            bundle = null;
        }

        assert bundle != null;

        String type = bundle.getString("type");

        AnimeBasicInfo.AnimeType animeType;

        try {
            animeType = AnimeBasicInfo.AnimeType.valueOf(type);
        } catch(IllegalArgumentException e) {
            Logger.error("Invalid anime type {} for this bundle", type);
            animeType = AnimeBasicInfo.AnimeType.REMOTE;
        }

        anime = Utils.getAnimeFromBundle(bundle, animeType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            searchEnhancer = getArguments().getParcelable("search_enhancer", SearchEnhancer.class);
        } else {
            searchEnhancer = getArguments().getParcelable("search_enhancer");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // region Basic Init
        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);
        binding.linearRoot.getLayoutTransition().setAnimateParentHierarchy(false);
        binding.episodeSelectorChips.getLayoutTransition().setAnimateParentHierarchy(false);

        binding.backToTopFab.setOnClickListener(v ->
                binding.animeEpisodesRecycler.smoothScrollToPosition(0)
        );

        binding.episodeSelectorScroll.setVisibility(View.GONE);

        // Anime object
        viewModel = new ViewModelProvider(this).get(AnimeEpisodesViewModelV2.class);
        // endregion

        // region Reminder dialog
        final boolean scheduleReminder = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("episodes_reminder", true);

        if (!scheduleReminder) {
            binding.episodeCardButton.setVisibility(View.GONE);
        }

        binding.scheduleHideTip.setOnClickListener(v -> {
            binding.episodeCardButton.setVisibility(View.GONE);

            ExtendedProperties appConfig = Data.getProperties(Data.CONFIGURATION.APP);
            appConfig.setBooleanProperty("episodes_reminder", false);
            appConfig.save();
        });
        // endregion

        // region Recycler View
        RecyclerView recyclerView = binding.animeEpisodesRecycler;
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new EpisodesRecyclerAdapter();
        adapter.setListener(this::searchEpisode);
        adapter.setAnime(anime);
        recyclerView.setAdapter(adapter);
        // endregion

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

        viewModel.getExceptionHandler().observe(getViewLifecycleOwner(), error -> {
            switch(error) {
                case NoResultsException:
                    Toast.makeText(
                            getContext(),
                            R.string.no_results_error,
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
                case NetworkConnectionException:
                case NoResponseException:
                    Toast.makeText(
                            getContext(),
                            R.string.network_connection_error,
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
                case ParsingException:
                case Generic:
                    Toast.makeText(
                            getContext(),
                            R.string.parsing_error,
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
            }

            binding.getRoot().postDelayed(() -> {
                    try {
                        binding.loadingBar.setVisibility(View.GONE);
                    } catch(Exception ignored) {
                    }
                },
                2000
            );
        });

        Threading.instant(() -> {
            final int animeLength;

            // Get anime length
            if (
                    (searchEnhancer == null || searchEnhancer.episode == null)
                            && anime instanceof SeasonalAnime
                            && ((SeasonalAnime) anime).getCurrentEpisode() > 0
            ) {
                animeLength = ((SeasonalAnime) anime).getCurrentEpisode();
            } else {
                // Dumb workaround lol
                int fetchResult;

                try {
                    fetchResult = KitsuPublic.episodeCount(
                            anime.getKitsuId()
                    );
                } catch (KitsuException e) {
                    Utils.makeToastRegardless(
                            getContext(),
                            R.string.network_connection_error,
                            Toast.LENGTH_SHORT
                    );
                    fetchResult = 0;
                } catch (ParsingException e) {
                    Utils.makeToastRegardless(
                            getContext(),
                            R.string.parsing_error,
                            Toast.LENGTH_SHORT
                    );
                    fetchResult = 0;
                }

                animeLength = fetchResult;
            }

            binding.getRoot().post(() -> {
                if (animeLength > 20) {
                    populateChips(animeLength);
                }

                viewModel.initialize(
                        anime,
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
        outState.putParcelable("search_enhancer", searchEnhancer);
        outState.putParcelable("anime", anime);
        outState.putString("type", anime.getType().name());
    }

    @Override
    public boolean shouldFragmentInterceptBack() {
        if (isDetached()) return false;
        ((AnimeDetailsActivity) requireActivity()).setCurrentFragment(0);
        return true;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        ((AnimeDetailsActivity) requireActivity()).changePagerInteractivity(!menuVisible);
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
                    viewModel.setPage(finalI);
                }
            });
        }
    }

    private void searchEpisode(@Nullable EpisodeBasicInfo episode) {
        if (episode == null) return;
        this.episode = episode;

        viewModel.searchEpisodeAndDisplayResults(
                episode,
                anime,
                searchEnhancer,
                binding,
                requireActivity(),
                this::playResult
        );
    }

    private void playResult(Result result) {
        AnimeDetailsActivity activity = (AnimeDetailsActivity) requireActivity();

        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, VideoPlayerActivity.class.getName());
        intent.putExtra("result", result);
        intent.putExtra("anime", anime);
        intent.putExtra("type", anime.getType().name());
        intent.putExtra("episode", episode);
        intent.putExtra("episode_type", episode.getType().name());
        binding.getRoot().post(() -> activity.startActivity(intent));
    }
}