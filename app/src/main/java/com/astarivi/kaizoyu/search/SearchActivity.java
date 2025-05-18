package com.astarivi.kaizoyu.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.storage.database.repo.SearchHistoryRepo;
import com.astarivi.kaizoyu.core.storage.database.tables.search_history.SearchHistory;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivitySearchBinding;
import com.astarivi.kaizoyu.databinding.FragmentSearchSuggestionBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.search.recycler.SearchPagingAdapter;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivityTheme {
    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private SearchPagingAdapter adapter;
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
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new SearchPagingAdapter(anime -> {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
            intent.putExtra("anime", anime);
            intent.putExtra("type", AnimeBasicInfo.AnimeType.REMOTE.name());
            startActivity(intent);
        });

        adapter.addLoadStateListener(loadState -> {
            LoadState refresh = loadState.getRefresh();
            if (refresh instanceof LoadState.Loading) {
                binding.searchResults.setVisibility(View.GONE);
                binding.noResultsPrompt.setVisibility(View.GONE);
                binding.loadingBar.setVisibility(View.VISIBLE);
            } else if (refresh instanceof LoadState.Error) {
                binding.noResultsPrompt.setVisibility(View.GONE);
                binding.loadingBar.setVisibility(View.GONE);
                binding.searchResults.setVisibility(View.GONE);

                Utils.makeToastRegardless(
                        SearchActivity.this,
                        R.string.parsing_error,
                        Toast.LENGTH_SHORT
                );
            } else if (refresh instanceof LoadState.NotLoading) {
                if (adapter.getItemCount() == 0) {
                    binding.noResultsPrompt.setVisibility(View.VISIBLE);
                    binding.loadingBar.setVisibility(View.GONE);
                    binding.searchResults.setVisibility(View.GONE);
                } else {
                    binding.loadingBar.setVisibility(View.GONE);
                    binding.noResultsPrompt.setVisibility(View.GONE);
                    binding.searchResults.setVisibility(View.VISIBLE);
                }
            }

            return kotlin.Unit.INSTANCE;
        });

        recyclerView.setAdapter(adapter);

        viewModel.getResults().observe(this, results -> {
            if (viewModel.hasOptedOutOfSearch()) {
                optOutOfSearch();
                return;
            }

            adapter.submitData(getLifecycle(), results);
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

                if (search.isEmpty()) {
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

                SearchHistoryRepo.saveAsync(search);
                viewModel.searchAnime(search);
            }

            return false;
        });

        if (getIntent().getBooleanExtra("openSearch", false)) {
            binding.searchView.show();
            binding.searchView.requestFocusAndShowKeyboard();
            binding.searchView.clearText();
            getIntent().removeExtra("openSearch");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isInsideSearchView) {
                    binding.searchView.hide();
                    isInsideSearchView = false;
                    return;
                }

                if (
                        (binding.searchResults.getVisibility() == View.VISIBLE && viewModel != null && viewModel.hasSearch()) ||
                                (binding.noResultsPrompt.getVisibility() == View.VISIBLE)
                ) {
                    optOutOfSearch();
                    return;
                }

                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
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

        Threading.database(() -> {
            List<SearchHistory> searchHistoryList = SearchHistoryRepo.getAll();

            Threading.instant(() -> {
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
                        SearchHistoryRepo.bumpUpAsync(searchHistory);
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
        viewModel.searchAnime(search);
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
