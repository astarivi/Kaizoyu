package com.astarivi.kaizoyu.gui.home;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.astarivi.kaizolib.ann.ANN;
import com.astarivi.kaizolib.ann.model.ANNItem;
import com.astarivi.kaizolib.kitsuv2.public_api.SearchParams;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.databinding.FragmentHomeBinding;
import com.astarivi.kaizoyu.gui.home.recycler.recommendations.HomeFuturePagingSource;
import com.astarivi.kaizoyu.utils.Threading;

import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.Future;

import kotlinx.coroutines.CoroutineScope;
import lombok.Getter;


public class HomeViewModel extends ViewModel {
    private final MutableLiveData<SearchParams> queryLiveData = new MutableLiveData<>();
    @Getter
    private final LiveData<PagingData<RemoteAnime>> containers;
    @Getter
    private final MutableLiveData<List<ANNItem>> news = new MutableLiveData<>();
    private Future<?> rssFuture = null;

    public HomeViewModel() {
        containers = Transformations.switchMap(queryLiveData, query -> {
            CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
            PagingConfig pagingConfig = new PagingConfig(20, 20, true, 20);

            Pager<Integer, RemoteAnime> pager = new Pager<>(
                    pagingConfig,
                    () -> new HomeFuturePagingSource(query)
            );

            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), viewModelScope);
        });
    }

    public void initialLoad(SearchParams params) {
        fetchNews();
        search(params);
    }

    public void search(SearchParams searchParams) {
        queryLiveData.postValue(searchParams);
    }

    public void reload(FragmentHomeBinding binding) {
        if (rssFuture != null && !rssFuture.isDone()) return;
        binding.newsLoading.setVisibility(View.VISIBLE);
        binding.newsHeader.setVisibility(View.VISIBLE);

        fetchNews();
    }

    private void fetchNews() {
        rssFuture = Threading.instant(() -> {
            try {
                news.postValue(
                        ANN.getANNFeed()
                );
            } catch (Exception e) {
                news.postValue(null);
                Logger.error(e);
            }
        });
    }
}
