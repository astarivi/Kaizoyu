package com.astarivi.kaizoyu.gui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizolib.kitsu.Kitsu;
import com.astarivi.kaizolib.kitsu.KitsuSearchParams;
import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.databinding.FragmentHomeBinding;
import com.astarivi.kaizoyu.databinding.FragmentHomeItemBinding;
import com.astarivi.kaizoyu.gui.home.recycler.HomeRecyclerAdapter;
import com.astarivi.kaizoyu.gui.home.recycler.HomeRecyclerContainer;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import java.util.ArrayList;
import java.util.List;


public class HomeViewModel extends ViewModel {
    List<HomeRecyclerContainer> containers;

    public void initializeItem(@NonNull FragmentHomeItemBinding binding,
                               @NonNull FragmentHomeBinding rootBinding,
                               KitsuSearchParams params,
                               HomeRecyclerAdapter.ItemClickListener listener) {
        if (containers == null) containers = new ArrayList<>();

        Threading.submitTask(
                Threading.TASK.INSTANT,
                () -> {
                    UserHttpClient httpClient = Data.getUserHttpClient();
                    Kitsu kitsu = new Kitsu(httpClient);
                    List<KitsuAnime> anime = kitsu.searchAnime(params);

                    if (anime == null || anime.isEmpty()) return;

                    List<Anime> animeBase = new Anime.BulkAnimeBuilder(anime).build();
                    anime.clear();

                    HomeRecyclerContainer homeRecyclerContainer = new HomeRecyclerContainer(
                            animeBase
                    );

                    rootBinding.itemsLayout.post(() -> {
                        homeRecyclerContainer.initialize(
                            binding.homeItemsRecycler,
                            listener
                        );

                        containers.add(
                                homeRecyclerContainer
                        );

                        rootBinding.itemsLayout.addView(binding.getRoot());
                    });
                }
        );
    }
}
