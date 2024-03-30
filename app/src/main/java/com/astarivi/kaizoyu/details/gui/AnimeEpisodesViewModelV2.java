package com.astarivi.kaizoyu.details.gui;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.kitsu.exception.KitsuExceptionManager;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.search.AssistedResultSearcher;
import com.astarivi.kaizoyu.core.search.Organizer;
import com.astarivi.kaizoyu.core.search.SearchEnhancer;
import com.astarivi.kaizoyu.core.video.VideoQuality;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;

import lombok.Getter;


public class AnimeEpisodesViewModelV2 extends ViewModel {
    @Getter
    private final MutableLiveData<TreeSet<Episode>> episodes = new MutableLiveData<>(null);
    @Getter
    private final MutableLiveData<KitsuExceptionManager.FailureCode> exceptionHandler = new MutableLiveData<>();
    @Getter
    private int currentPage = -1;
    private Future<?> fetchingFuture = null;
    private Anime anime = null;
    private int episodeCount = -1;
    private boolean isSearching = false;

    public void initialize(Anime anime, int episodeCount) {
        if (currentPage != -1 || (fetchingFuture != null && !fetchingFuture.isDone())) return;
        this.episodeCount = episodeCount;
        this.anime = anime;

        fetchPage(1);
    }

    public void reload() {
        if (episodeCount == -1 || anime == null || isFetching()) {
            return;
        }

        episodes.postValue(null);

        fetchPage(currentPage);
    }

    public void setPage(int page) {
        // Already there, or not initialized yet.
        if (currentPage == page || episodeCount == -1 || anime == null) {
            return;
        }

        episodes.postValue(null);

        fetchPage(page);
    }

    public boolean isFetching() {
        return fetchingFuture != null && !fetchingFuture.isDone();
    }

    private void fetchPage(int page) {
        fetchingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            int[] pagination = Utils.paginateNumber(page, episodeCount, 20);

            if (Thread.interrupted()) {
                return;
            }

            // TODO: Implement this with AniList
//            List<KitsuEpisode> kitsuEpisodes;
//            try {
//                kitsuEpisodes = kitsu.getEpisodesRange(
//                        this.animeId,
//                        pagination[0],
//                        pagination[1],
//                        episodeCount
//                );
//            } catch (Exception e) {
//                exceptionHandler.postValue(KitsuExceptionManager.getFailureCode(e));
//                episodes.postValue(null);
//                return;
//            }
//
//            if (episodeCount <= 20) {
//                if (episodeCount > kitsuEpisodes.size()) episodeCount = kitsuEpisodes.size();
//                if (episodeCount < kitsuEpisodes.size()) kitsuEpisodes = kitsuEpisodes.subList(0, episodeCount);
//            }
//
//            currentPage = page;
//
//            episodes.postValue(
//                    new Episode.BulkEpisodeBuilder(kitsuEpisodes, this.animeId).build()
//            );

            // Build dummy episodes
            TreeSet<Episode> eps = new TreeSet<>();

            for (int i = pagination[0]; i < pagination[1] + 1; i++) {
                if (Thread.interrupted()) return;

                eps.add(
                        new Episode(
                                Math.toIntExact(anime.getAniListAnime().id),
                                i,
                                0
                        )
                );
            }

            currentPage = page;

            episodes.postValue(eps);
        });
    }

    public void searchEpisodeAndDisplayResults(Episode episode,
                                               Anime anime,
                                               @Nullable SearchEnhancer searchEnhancer,
                                               FragmentAnimeEpisodesBinding binding,
                                               FragmentActivity context,
                                               AnimeEpisodesModalBottomSheet.ResultListener listener) {
        if (isSearching) {
            Toast.makeText(
                    context,
                    context.getString(R.string.episode_busy),
                    Toast.LENGTH_SHORT
            ).show(
            );
            return;
        }

        isSearching = true;

        Threading.submitTask(Threading.TASK.INSTANT,() -> {
                List<Result> results = AssistedResultSearcher.searchEpisode(
                        searchEnhancer,
                        episode.getAnimeId(),
                        anime.getDefaultTitle(),
                        episode.getNumber()
                );

                if (results == null) {
                    binding.getRoot().post(() -> Toast.makeText(
                            context,
                            context.getString(R.string.episode_no_results),
                            Toast.LENGTH_SHORT
                    ).show(
                    ));
                    isSearching = false;
                    return;
                }

                TreeMap<VideoQuality, List<Result>> organizedResults = Organizer.organizeResultsByQuality(results);

                results.clear();

                binding.getRoot().post(() -> {
                    AnimeEpisodesModalBottomSheet modalBottomSheet = new AnimeEpisodesModalBottomSheet(organizedResults, listener);
                    modalBottomSheet.show(context.getSupportFragmentManager(), AnimeEpisodesModalBottomSheet.TAG);
                });
                isSearching = false;
        });
    }
}
