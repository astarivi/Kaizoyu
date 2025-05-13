package com.astarivi.kaizoyu.search;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.databinding.ActivitySearchBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Future;

import lombok.Getter;


public class SearchViewModel extends ViewModel {
    @Getter
    private final MutableLiveData<List<RemoteAnime>> results = new MutableLiveData<>();
    private Future<?> searchingFuture = null;
    private boolean isSearchActive = false;

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
            ).show();
            return;
        }

        binding.searchResults.setVisibility(View.GONE);
        binding.noResultsPrompt.setVisibility(View.GONE);
        binding.loadingBar.setVisibility(View.VISIBLE);

        searchingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            List<KitsuAnime> searchResults;
            try {
                searchResults = KitsuPublic.search(search);
            } catch (KitsuException | ParsingError e) {
                if (e instanceof ParsingError) {
                    Utils.makeToastRegardless(
                            context,
                            R.string.parsing_error,
                            Toast.LENGTH_SHORT
                    );
                    return;
                }

                Utils.makeToastRegardless(
                        context,
                        R.string.network_connection_error,
                        Toast.LENGTH_SHORT
                );
                return;
            }

            if (searchResults.isEmpty()) {
                results.postValue(null);
                return;
            }

            isSearchActive = true;

            results.postValue(
                    AnimeMapper.bulkRemoteFromKitsu(searchResults)
            );
        });
    }
}
