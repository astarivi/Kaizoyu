package com.astarivi.kaizoyu.fullsearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.database.data.search.SearchHistory;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivityFullsearchBinding;
import com.astarivi.kaizoyu.databinding.FragmentSearchSuggestionBinding;
import com.astarivi.kaizoyu.fullsearch.recycler.AdvancedRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.video.VideoPlayerActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;


// Is this a copy of SearchActivity? yes.
public class FullSearchActivity extends AppCompatActivityTheme {
    private ActivityFullsearchBinding binding;
    private FullSearchViewModel viewModel;
    private AdvancedRecyclerAdapter adapter;
    private LinearLayoutManager recyclerLayoutManager;
    private boolean isInsideSearchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullsearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);

        viewModel = new ViewModelProvider(this).get(FullSearchViewModel.class);

        binding.noResultsPrompt.setVisibility(View.GONE);

        RecyclerView recyclerView = binding.searchResults;
        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new AdvancedRecyclerAdapter(result -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("result", result);
            intent.putExtra("isAdvancedMode", true);
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
                binding.searchResults.setVisibility(View.INVISIBLE);
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

        searchView.addTransitionListener((searchView1, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                displaySearchHistory();
                isInsideSearchView = true;
                return;
            }

            if (newState == SearchView.TransitionState.SHOWN) {
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

                if (search.length() < 4) {
                    Toast.makeText(
                            this,
                            String.format(
                                    getString(R.string.short_search),
                                    4
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

        // Popup dialog

        ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

        if (appProperties.getBooleanProperty("advanced_search_reminder", true)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.advanced_search_dialog_title))
                    .setMessage(getString(R.string.advanced_search_dialog_description))
                    .setPositiveButton(getString(R.string.advanced_search_dialog_accept), (dialog, which) -> {
                        AnalyticsClient.logBreadcrumb("advanced_search_first_time");

                        Toast.makeText(
                                this,
                                getString(R.string.advanced_search_dialog_finish),
                                Toast.LENGTH_SHORT
                        ).show(
                        );

                        appProperties.setBooleanProperty("advanced_search_reminder", false);
                        appProperties.save();
                    })
                    .show();
        }
    }

    private void displaySearchHistory() {
        LinearLayout searchSuggestions = binding.searchSuggestions;

        for (int i = 0; i < searchSuggestions.getChildCount(); i++) {
            final View child = searchSuggestions.getChildAt(i);
            child.setOnClickListener(null);
        }

        searchSuggestions.removeAllViews();

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
        binding.searchAppBar.setExpanded(true);
        if (viewModel != null) viewModel.optOutOfSearch();
    }
}
