package com.astarivi.kaizoyu.details.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.video.VideoParser;
import com.astarivi.kaizoyu.core.video.VideoQuality;
import com.astarivi.kaizoyu.databinding.BottomSheetEpisodesBinding;
import com.astarivi.kaizoyu.databinding.ComponentSuggestionChipBinding;
import com.astarivi.kaizoyu.databinding.ItemEpisodeBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Future;


public class AnimeEpisodesModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "EpisodesBottomModalSheet";
    private BottomSheetEpisodesBinding binding;
    private final TreeMap<VideoQuality, List<Result>> episodes;
    private final ResultListener resultListener;
    private Future reOrderFuture = null;

    public AnimeEpisodesModalBottomSheet() {
        resultListener = null;
        episodes = null;
    }

    public AnimeEpisodesModalBottomSheet(TreeMap<VideoQuality, List<Result>> ep, ResultListener resultListener) {
        episodes = ep;
        this.resultListener = resultListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEpisodesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Should never happen, but lets make Java happy
        if (episodes == null) return;

        ArrayList<VideoQuality> availableQualitySet = new ArrayList<>(episodes.keySet());

        reOrder(availableQualitySet.get(0));

        boolean firstDone = false;

        for (VideoQuality videoQuality : availableQualitySet) {
            ComponentSuggestionChipBinding chipBinding = ComponentSuggestionChipBinding.inflate(
                    getLayoutInflater(),
                    binding.videoQualityChips,
                    true
            );

            if (!firstDone) {
                chipBinding.getRoot().setChecked(true);
                firstDone = true;
            }

            chipBinding.getRoot().setText(videoQuality.toString());
            chipBinding.getRoot().setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (reOrderFuture != null && !reOrderFuture.isDone()) {
                    return;
                }

                if (isChecked) {
                    reOrder(videoQuality);
                    disableChips(buttonView);
                } else {
                    buttonView.setChecked(true);
                }
            });
        }
    }

    private void disableChips(CompoundButton activeButton) {
        for (int i = 0; i < binding.videoQualityChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.videoQualityChips.getChildAt(i);
            chip.setChecked(false);
        }

        activeButton.setChecked(true);
    }

    private void reOrder(VideoQuality quality) {
        if (episodes == null) return;

        binding.filteredEpisodesList.removeAllViews();
        binding.loadingBar.setVisibility(View.VISIBLE);

        reOrderFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            ArrayList<View> resultingViews = new ArrayList<>();

            List<Result> listResults = episodes.get(quality);

            if (listResults == null) return;

            for (Result result : listResults) {
                ItemEpisodeBinding reBinding = ItemEpisodeBinding.inflate(
                        getLayoutInflater(),
                        null,
                        false
                );

                reBinding.episodeBot.setText(result.getBotName());
                reBinding.episodeName.setText(result.getCleanedFilename());
                reBinding.episodeSize.setText(result.getNiblResult().size);

                boolean isPreferredBot = VideoParser.isPreferredBot(result.getBotName());

                if (isPreferredBot) {
                    reBinding.getRoot().setStrokeColor(ContextCompat.getColor(requireContext(), R.color.branding_accent));
                }

                reBinding.getRoot().setOnClickListener(v -> {
                    dismiss();
                    if (resultListener != null) resultListener.onResultSelected(result);
                });

                if (isPreferredBot) {
                    resultingViews.add(0, reBinding.getRoot());
                } else {
                    resultingViews.add(reBinding.getRoot());
                }
            }

            binding.getRoot().post(() -> {
                binding.loadingBar.setVisibility(View.GONE);

                for (View resultingView : resultingViews) {
                    binding.filteredEpisodesList.addView(resultingView);
                }
            });
        });
    }

    public interface ResultListener {
        void onResultSelected(Result result);
    }
}
