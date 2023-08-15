package com.astarivi.kaizoyu.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.storage.database.data.search.SearchHistory;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivitySearchBinding;
import com.astarivi.kaizoyu.databinding.FragmentSearchSuggestionBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.search.recycler.SearchRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivityTheme {
    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private SearchRecyclerAdapter adapter;
    private LinearLayoutManager recyclerLayoutManager;
    private boolean isInsideSearchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);

        binding.searchResults.setVisibility(View.GONE);

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Mixed
        binding.noResultsPrompt.setVisibility(View.GONE);

        // RecyclerView
        RecyclerView recyclerView = binding.searchResults;
        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new SearchRecyclerAdapter(anime -> {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.BASE.name());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getResults().observe(this, results -> {
            if (viewModel.hasOptedOutOfSearch()) {
                optOutOfSearch();
                return;
            }

            if (results == null) {
                binding.noResultsPrompt.setVisibility(View.VISIBLE);
                binding.loadingBar.setVisibility(View.GONE);
                binding.searchResults.setVisibility(View.GONE);
                return;
            }

            recyclerLayoutManager.scrollToPosition(0);
            binding.loadingBar.setVisibility(View.GONE);
            binding.searchResults.setVisibility(View.VISIBLE);

            adapter.replaceData(results);
            adapter.notifyDataSetChanged();
        });

        SearchBar searchBar = binding.searchBar;
        SearchView searchView = binding.searchView;

//        isInsideSearchView = false;

        // Absolutely horrendous code
        searchView.addTransitionListener((searchView1, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                isInsideSearchView = true;
                return;
            }

            if (newState == SearchView.TransitionState.SHOWN) {
                displaySearchHistory();
                isInsideSearchView = true;
                return;
            }

            if (newState == SearchView.TransitionState.HIDING) {
                isInsideSearchView = false;
            }
        });

        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != 0) return false;

            searchBar.setText(searchView.getText());
            searchView.hide();

            if (searchView.getText() != null) {
                String search = searchView.getText().toString();

                if (search.equals("")) {
                    optOutOfSearch();
                    return false;
                }

                if (search.length() < 3) {
                    Toast.makeText(
                            this,
                            String.format(
                                    getString(R.string.short_search),
                                    3
                            ),
                            Toast.LENGTH_SHORT
                    ).show(
                    );
                    return false;
                }

                Data.getRepositories()
                        .getSearchHistoryRepository()
                        .saveAsync(search);

                viewModel.searchAnime(search, binding, this);
            }

            return false;
        });

        if (getIntent().getBooleanExtra("openSearch", false)) {
            binding.searchView.show();
            binding.searchView.requestFocusAndShowKeyboard();
            binding.searchView.clearText();
            getIntent().removeExtra("openSearch");
        }
    }

    private void displaySearchHistory() {
        LinearLayout searchSuggestions = binding.searchSuggestions;

        // Solves weird Android 9 bug
        for (int i = 0; i < searchSuggestions.getChildCount(); i++) {
            final View child = searchSuggestions.getChildAt(i);
            child.setOnClickListener(null);
        }

        searchSuggestions.removeAllViews();

        if (KaizoyuApplication.application == null) {
            AnalyticsClient.logBreadcrumb("application_null_search_history");
            return;
        }

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            List<SearchHistory> searchHistoryList = Data.getRepositories()
                    .getSearchHistoryRepository().getAll();

            Threading.submitTask(Threading.TASK.INSTANT, () -> {
                ArrayList<FragmentSearchSuggestionBinding> suggestionBindings = new ArrayList<>();

                for (SearchHistory searchHistory : searchHistoryList) {
                    FragmentSearchSuggestionBinding searchSuggestionBinding = FragmentSearchSuggestionBinding.inflate(
                            getLayoutInflater(),
                            binding.searchSuggestions,
                            false
                    );

                    searchSuggestionBinding.itemHistory.setVisibility(View.VISIBLE);
                    searchSuggestionBinding.itemText.setText(searchHistory.searchTerm);
                    searchSuggestionBinding.rootLayout.setOnClickListener(v -> {
                        Data.getRepositories()
                                .getSearchHistoryRepository()
                                .bumpUpAsync(searchHistory);
                        doProgrammaticSearch(searchHistory.searchTerm);
                    });

                    suggestionBindings.add(
                            searchSuggestionBinding
                    );
                }

                binding.searchSuggestions.post(() -> {
                    if (!isInsideSearchView) {
                        suggestionBindings.clear();
                        return;
                    }
                    for (FragmentSearchSuggestionBinding sBinding : suggestionBindings) {
                        binding.searchSuggestions.addView(sBinding.getRoot());
                    }
                    suggestionBindings.clear();
                });

                // Release memory pointer
                searchHistoryList.clear();
            });
        });
    }

    private void doProgrammaticSearch(String search) {
        binding.searchView.setText(search);
        binding.searchView.hide();
        binding.searchBar.setText(search);
        viewModel.searchAnime(search, binding, this);
    }

    @Override
    public void onBackPressed() {
        if (isInsideSearchView) {
            binding.searchView.hide();
            isInsideSearchView = false;

            return;
        }

        // Look at that indentation, damn
        if (
                (
                    viewModel != null && viewModel.checkIfHasSearchAndCancel()
                )
                ||
                (
                    binding.searchResults.getVisibility() == View.VISIBLE &&
                    viewModel != null && viewModel.hasSearch()
                )
                ||
                (
                    binding.noResultsPrompt.getVisibility() == View.VISIBLE
                )
        ) {
            optOutOfSearch();
            return;
        }

        super.onBackPressed();
    }

    private void optOutOfSearch() {
        binding.searchResults.smoothScrollToPosition(0);
        binding.loadingBar.setVisibility(View.GONE);
        binding.noResultsPrompt.setVisibility(View.GONE);
        binding.searchResults.setVisibility(View.GONE);
        binding.searchBar.setText("");
        binding.searchView.setText("");
        binding.searchAppBar.setExpanded(true, true);
        if (viewModel != null) viewModel.optOutOfSearch();
    }
}
