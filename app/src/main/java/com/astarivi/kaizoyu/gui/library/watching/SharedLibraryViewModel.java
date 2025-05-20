package com.astarivi.kaizoyu.gui.library.watching;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.PagingLiveData;

import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.LocalAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
import com.astarivi.kaizoyu.core.storage.database.repo.SavedShowRepo;
import com.astarivi.kaizoyu.core.storage.database.tables.saved_anime.SavedAnime;
import com.astarivi.kaizoyu.core.threading.ThreadingAssistant;

import kotlinx.coroutines.CoroutineScope;
import lombok.Getter;



public class SharedLibraryViewModel extends ViewModel {
    @Getter
    private final LiveData<PagingData<LocalAnime>> results;
    private final MutableLiveData<AnimeBasicInfo.LocalList> queryType = new MutableLiveData<>();

    public SharedLibraryViewModel() {
        results = Transformations.switchMap(queryType, query -> {
            CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
            PagingConfig pagingConfig = new PagingConfig(20, 20, true, 20);

            Pager<Integer, SavedAnime> pager = new Pager<>(
                    pagingConfig,
                    () -> SavedShowRepo.getAnimeDao().pageAllByType(query.getValue())
            );

            LiveData<PagingData<LocalAnime>> liveData = Transformations.map(
                    PagingLiveData.getLiveData(pager),
                    pagingData -> PagingDataTransforms.map(
                            pagingData,
                            ThreadingAssistant.getInstance().getGuavaExecutor(),
                            AnimeMapper::localFromSaved
                    )
            );

            return PagingLiveData.cachedIn(liveData, viewModelScope);
        });
    }

    public void fetchFavorites(AnimeBasicInfo.LocalList type) {
        queryType.postValue(type);
    }
}
