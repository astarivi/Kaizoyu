package com.astarivi.kaizoyu.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.search.recycler.SearchFuturePagingSource;

import org.jetbrains.annotations.NotNull;

import kotlinx.coroutines.CoroutineScope;
import lombok.Getter;


public class SearchViewModel extends ViewModel {
    @Getter
    private final LiveData<PagingData<RemoteAnime>> results;
    private final MutableLiveData<String> queryLiveData = new MutableLiveData<>();
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

    public SearchViewModel() {
        results = Transformations.switchMap(queryLiveData, query -> {
            CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
            PagingConfig pagingConfig = new PagingConfig(20, 20, true, 20);

            Pager<Integer, RemoteAnime> pager = new Pager<>(
                    pagingConfig,
                    () -> new SearchFuturePagingSource(query)
            );

            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), viewModelScope);
        });
    }

    public void searchAnime(@NotNull String search) {
        isSearchActive = true;
        queryLiveData.postValue(search);
    }
}
