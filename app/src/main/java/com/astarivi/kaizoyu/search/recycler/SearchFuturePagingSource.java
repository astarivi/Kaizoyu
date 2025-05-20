package com.astarivi.kaizoyu.search.recycler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.astarivi.kaizolib.kitsuv2.model.RawResults;
import com.astarivi.kaizolib.kitsuv2.public_api.KitsuPublic;
import com.astarivi.kaizolib.kitsuv2.public_api.SearchParams;
import com.astarivi.kaizoyu.core.models.anime.AnimeMapper;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.utils.Threading;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class SearchFuturePagingSource extends ListenableFuturePagingSource<Integer, RemoteAnime> {
    private final String search;

    @NonNull
    @Override
    public ListenableFuture<LoadResult<Integer, RemoteAnime>> loadFuture(@NonNull LoadParams<Integer> params) {
        int page = Objects.requireNonNullElse(params.getKey(), 1);

        SearchParams search = new SearchParams()
                .setTitle(this.search)
                .setPageSize(params.getLoadSize())
                .setPageNumber(page);

        return Threading.guava(() -> {
            RawResults results;
            try {
                results = KitsuPublic.rawSearch(search);
            } catch (Exception e) {
                return new LoadResult.Error<>(e);
            }

            return new LoadResult.Page<>(
                    AnimeMapper.bulkRemoteFromKitsu(results.anime()),
                    page == 1 ? null : page - 1,
                    (long) page * params.getLoadSize() >= results.count() ? null : page + 1
            );
        });
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, RemoteAnime> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) return null;

        LoadResult.Page<Integer, RemoteAnime> anchorPage = state.closestPageToPosition(anchorPosition);
        if (anchorPage == null) return null;

        if (anchorPage.getPrevKey() != null) {
            return anchorPage.getPrevKey() + 1;
        } else if (anchorPage.getNextKey() != null) {
            return anchorPage.getNextKey() - 1;
        } else {
            return 1;
        }
    }
}
