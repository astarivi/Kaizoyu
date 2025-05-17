package com.astarivi.kaizoyu.gui.library.watching;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.LocalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.storage.database.repo.SavedShowRepo;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;
import com.astarivi.kaizoyu.databinding.ActivitySharedLibraryBinding;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import lombok.Getter;


@Getter
public class SharedLibraryViewModel extends ViewModel {
    private final MutableLiveData<List<LocalAnime>> animeList = new MutableLiveData<>();

    public void fetchFavorites(@NotNull ActivitySharedLibraryBinding binding, AnimeBasicInfo.LocalList type) {
        binding.emptyLibraryPopup.setVisibility(View.GONE);
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.libraryContents.setVisibility(View.INVISIBLE);

        Threading.database(() -> {
            List<SavedAnime> savedAnime = SavedShowRepo.getAnimeDao().getAllByType(
                    type.getValue()
            );

            if (savedAnime.isEmpty()) {
                animeList.postValue(null);
                return;
            }

            Threading.instant(() ->
                animeList.postValue(
                        AnimeMapper.bulkLocalFromSaved(savedAnime)
                )
            );
        });
    }
}
