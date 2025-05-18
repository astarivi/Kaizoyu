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
        int offset = (page - 1) * params.getLoadSize();

        SearchParams search = new SearchParams()
                .setTitle(this.search)
                .setLimit(params.getLoadSize())
                .setOffset(offset);

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
                    offset + params.getLoadSize() >= results.count() ? null : page + 1
            );
        });
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, RemoteAnime> pagingState) {
        return 1;
    }
}
