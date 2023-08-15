package com.astarivi.kaizoyu.gui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.databinding.FragmentHomeBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.fullsearch.FullSearchActivity;
import com.astarivi.kaizoyu.gui.home.recycler.news.NewsRecyclerAdapter;
import com.astarivi.kaizoyu.gui.home.recycler.recommendations.HomeMainRecyclerAdapter;
import com.astarivi.kaizoyu.gui.home.recycler.recommendations.HomeRecyclerAdapter;
import com.astarivi.kaizoyu.gui.more.settings.SettingsActivity;
import com.astarivi.kaizoyu.search.SearchActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Utils;


public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;

    public HomeFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean showAdvancedSearch = Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("advanced_search", false);

        binding.mainSearchBar.getMenu().clear();

        if (!showAdvancedSearch) return;

        binding.mainSearchBar.inflateMenu(R.menu.search_bar_menu);
        binding.mainSearchBar.setOnMenuItemClickListener(item -> {
            // There's only one item, so no need to check. If another item is added, change this.
            if (getActivity() == null) return false;
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, FullSearchActivity.class.getName());
            intent.putExtra("openSearch", true);
            startActivity(intent);
            return true;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.header.getLayoutTransition().setAnimateParentHierarchy(false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.appBar,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                    v.setPadding(0, insets.top, 0, 0);

                    return windowInsets;
                }
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.itemsLayout,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    if (getContext() == null) return windowInsets;

                    v.setPadding(
                            0,
                            (int) Utils.convertDpToPixel(6, requireContext()),
                            0,
                            insets.bottom + (int) Utils.convertDpToPixel(8, requireContext())
                    );

                    return windowInsets;
                }
        );

        binding.settingsButton.bringToFront();

        binding.settingsButton.setOnClickListener(v -> {
            if (getActivity() == null) return;

            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, SettingsActivity.class.getName());
            startActivity(intent);
        });

        binding.mainSearchBar.setOnClickListener(v -> {
            if (getActivity() == null) return;

            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, SearchActivity.class.getName());
            intent.putExtra("openSearch", true);

            startActivity(intent);
        });

        HomeRecyclerAdapter.ItemClickListener listener = anime -> {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.BASE.name());
            startActivity(intent);
        };

        RecyclerView recyclerView = binding.itemsLayout;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(false);
        HomeMainRecyclerAdapter adapter = new HomeMainRecyclerAdapter(listener);

        recyclerView.setAdapter(adapter);

        viewModel.getContainers().observe(getViewLifecycleOwner(), (containers) -> {
            if (containers == null) {
                binding.noResultsMessage.setVisibility(View.VISIBLE);
                binding.loadingBar.setVisibility(View.GONE);
                adapter.clearData();
                return;
            }

            if (containers.isEmpty()) return;

            binding.loadingBar.setVisibility(View.GONE);
            binding.itemsLayout.setVisibility(View.VISIBLE);
            adapter.replaceData(containers);
        });

        RecyclerView newsRecycler = binding.newsRecycler;
        newsRecycler.setLayoutManager(
            new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        );

        newsRecycler.setHasFixedSize(false);

        NewsRecyclerAdapter newsAdapter =  new NewsRecyclerAdapter(article ->
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(article.getLink())))
        );

        newsRecycler.setAdapter(newsAdapter);

        viewModel.getNews().observe(getViewLifecycleOwner(), (news) -> {
            if (news == null) {
                binding.newsHeader.setVisibility(View.GONE);
                return;
            }

            if (news.isEmpty()) return;

            binding.newsLoading.setVisibility(View.GONE);

            binding.newsRecycler.setVisibility(View.VISIBLE);
            newsAdapter.replaceData(news);
            binding.newsRecycler.smoothScrollToPosition(0);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            adapter.clearData();
            binding.swipeRefresh.setRefreshing(false);
            viewModel.reloadHome(binding);
        });

        viewModel.reloadHome(binding);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}