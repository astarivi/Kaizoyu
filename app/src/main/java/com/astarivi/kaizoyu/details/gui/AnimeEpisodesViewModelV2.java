package com.astarivi.kaizoyu.details.gui;

import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.exception.KitsuExceptionManager;
import com.astarivi.kaizolib.kitsu.model.KitsuEpisode;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.Episode;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.search.AssistedResultSearcher;
import com.astarivi.kaizoyu.core.search.Organizer;
import com.astarivi.kaizoyu.core.video.VideoQuality;
import com.astarivi.kaizoyu.databinding.FragmentAnimeEpisodesBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;


public class AnimeEpisodesViewModelV2 extends ViewModel {
    private final MutableLiveData<TreeSet<Episode>> episodes = new MutableLiveData<>(null);
    private final MutableLiveData<KitsuExceptionManager.FailureCode> exceptionHandler = new MutableLiveData<>();
    private int currentPage = -1;
    private Future<?> fetchingFuture = null;
    private int animeId = -1;
    private int episodeCount = -1;
    private boolean isSearching = false;

    public MutableLiveData<TreeSet<Episode>> getEpisodes() {
        return episodes;
    }

    public MutableLiveData<KitsuExceptionManager.FailureCode> getExceptionHandler() {
        return exceptionHandler;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void initialize(int animeId, int episodeCount) {
        if (currentPage != -1 || (fetchingFuture != null && !fetchingFuture.isDone())) return;
        this.episodeCount = episodeCount;
        this.animeId = animeId;

        fetchPage(1);
    }

    public void reload() {
        if (episodeCount == -1 || animeId == -1 || isFetching()) {
            return;
        }

        episodes.postValue(null);

        fetchPage(currentPage);
    }

    public void setPage(int page) {
        // Already there, or not initialized yet.
        if (currentPage == page || episodeCount == -1 || animeId == -1) {
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
            Kitsu kitsu = new Kitsu(
                    Data.getUserHttpClient()
            );

            int[] pagination = Utils.paginateNumber(page, episodeCount, 20);

            List<KitsuEpisode> kitsuEpisodes;
            try {
                kitsuEpisodes = kitsu.getEpisodesRange(
                        this.animeId,
                        pagination[0],
                        pagination[1],
                        episodeCount
                );
            } catch (Exception e) {
                exceptionHandler.postValue(KitsuExceptionManager.getFailureCode(e));
                episodes.postValue(null);
                return;
            }

            if (episodeCount <= 20) {
                if (episodeCount > kitsuEpisodes.size()) episodeCount = kitsuEpisodes.size();
                if (episodeCount < kitsuEpisodes.size()) kitsuEpisodes = kitsuEpisodes.subList(0, episodeCount);
            }

            currentPage = page;

            episodes.postValue(
                    new Episode.BulkEpisodeBuilder(kitsuEpisodes, this.animeId).build()
            );
        });
    }

    public void searchEpisodeAndDisplayResults(@NotNull Episode episode,
                                               @NotNull Anime anime,
                                               @NotNull FragmentAnimeEpisodesBinding binding,
                                               @NotNull FragmentActivity context,
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

        Threading.submitTask(
                Threading.TASK.INSTANT,
                () -> {
                    List<Result> results = new AssistedResultSearcher().searchEpisode(
                            episode.getAnimeId(),
                            anime.getDefaultTitle(),
                            episode.getKitsuEpisode().attributes.number
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
                }
        );
    }
}
