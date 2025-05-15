package com.astarivi.kaizoyu.gui.home;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.ann.ANN;
import com.astarivi.kaizolib.ann.model.ANNItem;
import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizolib.kitsuv2.public_api.SearchParams;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.databinding.FragmentHomeBinding;
import com.astarivi.kaizoyu.gui.home.recycler.recommendations.MainCategoryContainer;
import com.astarivi.kaizoyu.utils.Threading;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import lombok.Getter;


public class HomeViewModel extends ViewModel {
    @Getter
    private final MutableLiveData<ArrayList<MainCategoryContainer>> containers = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<List<ANNItem>> news = new MutableLiveData<>();
    private Future<?> reloadFuture = null;
    private Future<?> rssFuture = null;

    public boolean reloadHome(FragmentHomeBinding binding) {
        if (reloadFuture != null && !reloadFuture.isDone()) return false;
        if (rssFuture != null && !rssFuture.isDone()) return false;
        binding.newsRecycler.setVisibility(View.INVISIBLE);
        binding.itemsLayout.setVisibility(View.INVISIBLE);
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.newsLoading.setVisibility(View.VISIBLE);
        binding.newsHeader.setVisibility(View.VISIBLE);
        binding.noResultsMessage.setVisibility(View.GONE);

        containers.postValue(new ArrayList<>());

        fetchHome();

        return true;
    }

    private void fetchHome() {
        rssFuture = Threading.submitTask(Threading.TASK.INSTANT,() -> {
            try {
                news.postValue(
                        ANN.getANNFeed()
                );
            } catch (Exception e) {
                news.postValue(null);
                Logger.error(e);
            }
        });

        reloadFuture = Threading.submitTask(
                Threading.TASK.INSTANT,
                () -> {
                    fetchCategory(
                            R.string.popular_anime,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    )
                    );

                    fetchCategory(
                            R.string.home_beloved,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "-favoritesCount"
                                    )
                    );

                    fetchCategory(
                            R.string.home_seinen,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "filter[categories]",
                                            "seinen"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "-favoritesCount"
                                    )
                    );

                    fetchCategory(
                            R.string.popular_airing,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "filter[subtype]",
                                            "TV"
                                    ).
                                    setCustomParameter(
                                            "filter[status]",
                                            "current"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    )
                    );

                    fetchCategory(
                            R.string.popular_upcoming,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "filter[subtype]",
                                            "TV"
                                    ).
                                    setCustomParameter(
                                            "filter[status]",
                                            "upcoming"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    )
                    );

                    fetchCategory(
                            R.string.trash_anime,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "filter[subtype]",
                                            "TV"
                                    ).
                                    setCustomParameter(
                                            "filter[status]",
                                            "finished"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "averageRating"
                                    )
                    );

                    fetchCategory(
                            R.string.shoujo_anime,
                            new SearchParams().
                                    setLimit(
                                            15
                                    ).
                                    setCustomParameter(
                                            "filter[categories]",
                                            "shoujo"
                                    ).
                                    setCustomParameter(
                                            "filter[subtype]",
                                            "TV"
                                    ).
                                    setCustomParameter(
                                            "filter[status]",
                                            "finished"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    )
                    );

                    fetchCategory(
                            R.string.shounen_anime,
                            new SearchParams().
                                    setLimit(
                                            20
                                    ).
                                    setCustomParameter(
                                            "filter[categories]",
                                            "shounen"
                                    ).
                                    setCustomParameter(
                                            "filter[subtype]",
                                            "TV"
                                    ).
                                    setCustomParameter(
                                            "filter[status]",
                                            "finished"
                                    ).
                                    setCustomParameter(
                                            "sort",
                                            "popularityRank"
                                    )
                    );

                    @Nullable ArrayList<MainCategoryContainer> items = containers.getValue();

                    if (items == null || items.isEmpty()) {
                        containers.postValue(null);
                    }
        });
    }

    private void fetchCategory(@StringRes int titleResourceId, SearchParams query) {
        List<KitsuAnime> anime;
        try {
            anime = KitsuPublic.advancedSearch(query);
        } catch (Exception e) {
            Logger.debug(e);
            return;
        }

        List<RemoteAnime> fetchedAnime = AnimeMapper.bulkRemoteFromKitsu(anime);
        // Free it up, although, this is mostly useless.
        anime.clear();
        addItemToMutable(new MainCategoryContainer(
                titleResourceId,
                fetchedAnime
        ));
    }

    private synchronized void addItemToMutable(MainCategoryContainer item) {
        @Nullable ArrayList<MainCategoryContainer> items = containers.getValue();

        if (items == null) {
            ArrayList<MainCategoryContainer> newItems = new ArrayList<>();
            newItems.add(item);
            containers.postValue(newItems);
            return;
        }

        items.add(item);
        containers.postValue(items);
    }
}
