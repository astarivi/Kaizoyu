package com.astarivi.kaizoyu.gui.library.watching;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeWithSeenAnime;
import com.astarivi.kaizoyu.databinding.ActivitySharedLibraryBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;


@Getter
public class SharedLibraryViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<LocalAnime>> animeList = new MutableLiveData<>();

    public void fetchFavorites(@NotNull ActivitySharedLibraryBinding binding, ModelType.LocalAnime type) {
        binding.emptyLibraryPopup.setVisibility(View.GONE);
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.libraryContents.setVisibility(View.INVISIBLE);

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            FavoriteAnimeDao favDao = Data.getRepositories()
                    .getAnimeStorageRepository()
                    .getFavoriteAnimeDao();

            List<FavoriteAnimeWithSeenAnime> favAnimeList = favDao.getSpecificRelation(type.getValue());

            if (favAnimeList.isEmpty()) {
                animeList.postValue(null);
                return;
            }

            Threading.submitTask(Threading.TASK.INSTANT, () ->
                animeList.postValue(
                        new LocalAnime.BulkFavoriteLocalAnimeBuilder(favAnimeList)
                            .build()
                )
            );
        });
    }
}
