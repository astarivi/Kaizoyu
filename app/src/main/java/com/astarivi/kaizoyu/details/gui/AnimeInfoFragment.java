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

import com.astarivi.kaizolib.kitsuv2.model.KitsuAnime;
import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;
import com.astarivi.kaizoyu.core.models.base.AnimeBasicInfo;
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
    private AnimeBasicInfo anime;

    public AnimeInfoFragment() {
    }

    public void scrollTop() {
        binding.getRoot().smoothScrollTo(0, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnimeInfoBinding.inflate(inflater, container, false);

        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments();
        } else if (savedInstanceState != null && anime == null) {
            bundle = savedInstanceState;
        } else {
            Logger.error("Couldn't initialize bundle at AnimeInfoFragment");
            bundle = null;
        }

        assert bundle != null;

        String type = bundle.getString("type");

        AnimeBasicInfo.AnimeType animeType;

        try {
            animeType = AnimeBasicInfo.AnimeType.valueOf(type);
        } catch(IllegalArgumentException e) {
            Logger.error("Invalid anime type {} for this bundle", type);
            animeType = AnimeBasicInfo.AnimeType.REMOTE;
        }

        anime = Utils.getAnimeFromBundle(bundle, animeType);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AnimeInfoViewModel.class);

        binding.categoriesContainer.setVisibility(View.GONE);
        binding.trailerCard.setVisibility(View.GONE);

        binding.animeSynopsis.setText(anime.getSynopsis());

        if (anime.getTitleEn() != null) {
            String titleEn = anime.getTitleEn();
            binding.titleUs.setVisibility(View.VISIBLE);
            binding.animeTitleUs.setText(titleEn);
            binding.titleUs.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titleEn)
            );
        }

        if (anime.getTitleEnJp() != null) {
            String titleEnJp = anime.getTitleEnJp();
            binding.titleEnJp.setVisibility(View.VISIBLE);
            binding.animeTitleEnjp.setText(titleEnJp);
            binding.titleEnJp.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titleEnJp)
            );
        }

        if (anime.getTitleJp() != null) {
            String titleJp = anime.getTitleJp();
            binding.titleJp.setVisibility(View.VISIBLE);
            binding.animeTitleJp.setText(titleJp);
            binding.titleJp.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titleJp)
            );
        }

        if (anime instanceof RemoteAnime remoteAnime) {
            KitsuAnime kitsuAnime = remoteAnime.getInternal();

            if (kitsuAnime.attributes.youtubeVideoId != null) {
                YouTubePlayerView youTubePlayerView = binding.youtubePlayer;
                getLifecycle().addObserver(youTubePlayerView);

                youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        youTubePlayer.cueVideo(kitsuAnime.attributes.youtubeVideoId, 0);
                    }
                });

                binding.trailerCard.setVisibility(View.VISIBLE);
            }

            // TODO: Enable this.
//            if (aniListAnime.genres != null && !aniListAnime.genres.isEmpty()) {
//                this.makeCategoryChips(aniListAnime.genres);
//            }
        }

        viewModel.initialize(anime);
    }

    private void makeCategoryChips(List<String> categories) {
        Threading.instant(() -> {
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
        outState.putString("type", anime.getType().name());
    }
}