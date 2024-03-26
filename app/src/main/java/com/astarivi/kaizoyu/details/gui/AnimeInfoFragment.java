package com.astarivi.kaizoyu.details.gui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.databinding.FragmentAnimeInfoBinding;
import com.astarivi.kaizoyu.databinding.ItemChipCategoryBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;


public class AnimeInfoFragment extends Fragment {
    private FragmentAnimeInfoBinding binding;
    private AnimeInfoViewModel viewModel;
    private Anime anime;

    public AnimeInfoFragment() {
    }

    public void scrollTop() {
        binding.getRoot().smoothScrollTo(0, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnimeInfoBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            anime = Utils.getAnimeFromBundle(getArguments(), ModelType.Anime.BASE);
        }

        if (savedInstanceState != null && anime == null){
            anime = Utils.getAnimeFromBundle(savedInstanceState, ModelType.Anime.BASE);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AnimeInfoViewModel.class);

        AniListAnime aniListAnime = anime.getAniListAnime();

        binding.categoriesContainer.setVisibility(View.GONE);

        binding.animeSynopsis.setText(aniListAnime.description);

        AniListAnime.Titles titles = aniListAnime.title;

        if (titles.english != null) {
            binding.titleUs.setVisibility(View.VISIBLE);
            binding.animeTitleUs.setText(titles.english);
            binding.titleUs.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titles.english)
            );
        }

        if (titles.romaji != null) {
            binding.titleEnJp.setVisibility(View.VISIBLE);
            binding.animeTitleEnjp.setText(titles.romaji);
            binding.titleEnJp.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titles.romaji)
            );
        }

        if (titles.japanese != null) {
            binding.titleJp.setVisibility(View.VISIBLE);
            binding.animeTitleJp.setText(titles.japanese);
            binding.titleJp.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titles.japanese)
            );
        }

        if (
                aniListAnime.trailer != null &&
                aniListAnime.trailer.id != null &&
                (aniListAnime.trailer.site == null || aniListAnime.trailer.site.equals("youtube"))
        ) {
            YouTubePlayerView youTubePlayerView = binding.youtubePlayer;
            getLifecycle().addObserver(youTubePlayerView);

            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(
                            aniListAnime.trailer.id,
                            0
                    );
                }
            });
        } else {
            binding.trailerCard.setVisibility(View.GONE);
        }

        if (aniListAnime.genres != null && !aniListAnime.genres.isEmpty()) {
            this.makeCategoryChips(aniListAnime.genres);
        }

        viewModel.initialize(anime);
    }

    private void makeCategoryChips(List<String> categories) {
        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            ArrayList<View> chipViews = new ArrayList<>();

            LayoutInflater layoutInflater = getLayoutInflater();

            for (String category : categories) {
                ItemChipCategoryBinding chipBinding = ItemChipCategoryBinding.inflate(
                        layoutInflater,
                        null,
                        false
                );

                chipBinding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(
                        Colors.getColorFromString(category, 0.6F, 0.3F)
                ));

                chipBinding.getRoot().setText(category);

                chipViews.add(chipBinding.getRoot());
            }

            if (chipViews.isEmpty()) return;

            binding.getRoot().post(() -> {
                try {
                    for (View chip : chipViews) {
                        binding.categoriesContainer.setVisibility(View.VISIBLE);

                        binding.categoriesChips.addView(chip);
                    }
                } catch(Exception e) {
                    Logger.error("Error while posting categories at show details");
                    Logger.error(e);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        binding.youtubePlayer.release();
        if (viewModel != null) viewModel.destroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("anime", anime);
    }
}