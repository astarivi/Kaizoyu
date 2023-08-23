package com.astarivi.kaizoyu.details.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizolib.kitsu.model.KitsuCategory;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.databinding.FragmentAnimeInfoBinding;
import com.astarivi.kaizoyu.utils.Utils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

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

        KitsuAnime kitsuAnime = anime.getKitsuAnime();

        binding.categoriesContainer.setVisibility(View.GONE);

        binding.animeSynopsis.setText(kitsuAnime.attributes.synopsis);

        KitsuAnime.KitsuAnimeTitles titles = kitsuAnime.attributes.titles;

        if (titles.en != null) {
            binding.titleUs.setVisibility(View.VISIBLE);
            binding.animeTitleUs.setText(titles.en);
            binding.titleUs.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titles.en)
            );
        }

        if (titles.en_jp != null) {
            binding.titleEnJp.setVisibility(View.VISIBLE);
            binding.animeTitleEnjp.setText(titles.en_jp);
            binding.titleEnJp.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titles.en_jp)
            );
        }

        if (titles.ja_jp != null) {
            binding.titleJp.setVisibility(View.VISIBLE);
            binding.animeTitleJp.setText(titles.ja_jp);
            binding.titleJp.setOnLongClickListener(v ->
                    Utils.copyToClipboard(getActivity(), "Anime title", titles.ja_jp)
            );
        }

        if (kitsuAnime.attributes.youtubeVideoId != null) {
            YouTubePlayerView youTubePlayerView = binding.youtubePlayer;
            getLifecycle().addObserver(youTubePlayerView);

            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(kitsuAnime.attributes.youtubeVideoId, 0);
                }
            });
        } else {
            binding.trailerCard.setVisibility(View.GONE);
        }

        viewModel.getCategories().observe(getViewLifecycleOwner(), this::makeCategoryChips);

        viewModel.initialize(anime);
    }

    private void makeCategoryChips(List<KitsuCategory> categories) {

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