package com.astarivi.kaizoyu.details.gui;

import androidx.lifecycle.ViewModel;

import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;


// Unused, for now.
public class AnimeInfoViewModel extends ViewModel {
//    @Getter
//    private final MutableLiveData<List<KitsuCategory>> categories = new MutableLiveData<>();
//    private Future<?> fetchingFuture = null;

    // FIXME: Enable this
    public void initialize(AnimeBasicInfo anime) {
//        final int kitsuId = Integer.parseInt(anime.getAniListAnime().id);
//
//        fetchingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
//            UserHttpClient userHttpClient = Data.getUserHttpClient();
//
//            KitsuRelations relations = new KitsuRelations(userHttpClient);
//
//            try {
//                categories.postValue(relations.getKitsuCategories(kitsuId));
//            // Relations aren't critical, they're optional pieces of data.
//            } catch (Exception ignored) {
//            }
//        });
    }

    @Override
    protected void onCleared() {
        destroy();

        super.onCleared();
    }

    public void destroy() {
//        if (fetchingFuture != null && !fetchingFuture.isDone()) {
//            fetchingFuture.cancel(true);
//        }
    }
}
