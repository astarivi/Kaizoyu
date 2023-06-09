package com.astarivi.kaizoyu.search;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.KitsuSearchParams;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.databinding.ActivitySearchBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class SearchViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Anime>> results = new MutableLiveData<>();
    private Future searchingFuture = null;
    private boolean isSearchActive = false;

    public MutableLiveData<ArrayList<Anime>> getResults() {
        return results;
    }

    public boolean hasSearch() {
        return results.getValue() != null;
    }

    public boolean hasOptedOutOfSearch() {
        return !isSearchActive;
    }

    public void optOutOfSearch() {
        isSearchActive = false;
    }

    public boolean checkIfHasSearchAndCancel() {
        if (searchingFuture != null && !searchingFuture.isDone()) {
            searchingFuture.cancel(true);
            return true;
        }

        return false;
    }

    public void searchAnime(
            @NotNull String search,
            @NotNull ActivitySearchBinding binding,
            @NotNull Context context
    ) {
        if (searchingFuture != null && !searchingFuture.isDone()) {
            Toast.makeText(
                    context,
                    context.getString(R.string.anime_busy),
                    Toast.LENGTH_SHORT
            ).show(
            );
            return;
        }

        binding.searchResults.setVisibility(View.GONE);
        binding.noResultsPrompt.setVisibility(View.GONE);
        binding.loadingBar.setVisibility(View.VISIBLE);

        searchingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            Kitsu kitsu = new Kitsu(
                    Data.getUserHttpClient()
            );

            List<KitsuAnime> searchResults = kitsu.searchAnime(
                    new KitsuSearchParams(
                    ).setTitle(
                            search
                    ).setLimit(
                            20
                    )
            );

            isSearchActive = true;

            if (searchResults == null || searchResults.isEmpty()) {
                results.postValue(null);
                return;
            }

            results.postValue(
                    new Anime.BulkAnimeBuilder(searchResults).build()
            );
        });
    }
}
