package com.astarivi.kaizoyu.gui.home;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.anilist.AniList;
import com.astarivi.kaizolib.anilist.AniListQuery;
import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.rss.RssFetcher;
import com.astarivi.kaizoyu.databinding.FragmentHomeBinding;
import com.astarivi.kaizoyu.gui.home.recycler.recommendations.MainCategoryContainer;
import com.astarivi.kaizoyu.utils.Threading;
import com.rometools.rome.feed.synd.SyndEntry;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import lombok.Getter;


public class HomeViewModel extends ViewModel {
    @Getter
    private final MutableLiveData<ArrayList<MainCategoryContainer>> containers = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<List<SyndEntry>> news = new MutableLiveData<>();
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
                        RssFetcher.getANNFeed()
                );
            } catch (Exception e) {
                news.postValue(null);
                Logger.error(e);
            }
        });

        reloadFuture = Threading.submitTask(Threading.TASK.INSTANT,() -> {
            fetchCategory(
                    R.string.trending_anime,
                    AniList.sortedBy(AniList.TYPE.TRENDING)
            );

            if (Thread.interrupted()) return;

            fetchCategory(
                    R.string.popular_anime,
                    AniList.sortedBy(AniList.TYPE.POPULARITY)
            );

            if (Thread.interrupted()) return;

            fetchCategory(
                    R.string.best_score_anime,
                    AniList.sortedBy(AniList.TYPE.SCORE)
            );

            if (Thread.interrupted()) return;

            fetchCategory(
                    R.string.home_beloved,
                    AniList.sortedBy(AniList.TYPE.FAVOURITES)
            );

            @Nullable ArrayList<MainCategoryContainer> items = containers.getValue();

            // Check if nothing succeeded.
            if (items == null || items.isEmpty()) {
                containers.postValue(null);
            }
        });
    }

    private void fetchCategory(@StringRes int titleResourceId, AniListQuery.Paged query) {
        List<AniListAnime> anime;
        try {
            anime = query.next();
        } catch (Exception e) {
            Logger.debug(e);
            return;
        }

        List<Anime> fetchedAnime = new Anime.BulkAnimeBuilder(anime).build();
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
