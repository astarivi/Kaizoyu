package com.astarivi.kaizoyu.details.gui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.kitsu.KitsuRelations;
import com.astarivi.kaizolib.kitsu.model.KitsuCategory;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import java.util.List;
import java.util.concurrent.Future;

import lombok.Getter;


public class AnimeInfoViewModel extends ViewModel {
    @Getter
    private final MutableLiveData<List<KitsuCategory>> categories = new MutableLiveData<>();
    private Future<?> fetchingFuture = null;

    public void initialize(Anime anime) {
        final int kitsuId = Integer.parseInt(anime.getKitsuAnime().id);

        fetchingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            UserHttpClient userHttpClient = Data.getUserHttpClient();

            KitsuRelations relations = new KitsuRelations(userHttpClient);

            try {
                categories.postValue(relations.getKitsuCategories(kitsuId));
            // Relations aren't critical, they're optional pieces of data.
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    protected void onCleared() {
        destroy();

        super.onCleared();
    }

    public void destroy() {
        if (fetchingFuture != null && !fetchingFuture.isDone()) {
            fetchingFuture.cancel(true);
        }
    }
}
