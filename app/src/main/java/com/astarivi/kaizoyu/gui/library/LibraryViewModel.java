package com.astarivi.kaizoyu.gui.library;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeDao;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeWithSeenAnime;
import com.astarivi.kaizoyu.databinding.FragmentLibraryBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class LibraryViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<LocalAnime>> animeList = new MutableLiveData<>();

    public MutableLiveData<ArrayList<LocalAnime>> getAnimeList() {
        return animeList;
    }

    public void fetchFavorites(@NotNull FragmentLibraryBinding binding) {
        binding.emptyLibraryPopup.setVisibility(View.GONE);
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.libraryContents.setVisibility(View.INVISIBLE);

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            FavoriteAnimeDao favDao = Data.getRepositories()
                    .getFavoriteAnimeRepository()
                    .getAnimeDao();

            List<FavoriteAnimeWithSeenAnime> favAnimeList = favDao.getRelation();

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
