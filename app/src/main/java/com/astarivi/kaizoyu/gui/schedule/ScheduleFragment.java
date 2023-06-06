package com.astarivi.kaizoyu.gui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.databinding.ComponentSuggestionChipBinding;
import com.astarivi.kaizoyu.databinding.FragmentScheduleBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.schedule.recycler.ScheduleRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Translation;
import com.google.android.material.chip.Chip;

import java.time.DayOfWeek;
import java.util.ArrayList;


public class ScheduleFragment extends Fragment {
    private ScheduleViewModel viewModel;
    private FragmentScheduleBinding binding;
    private ScheduleRecyclerAdapter adapter;
    private LinearLayoutManager manager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View root, Bundle savedState) {
        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        binding.getRoot().getLayoutTransition().setAnimateParentHierarchy(false);
        binding.dowSelectorChips.getLayoutTransition().setAnimateParentHierarchy(false);

        // RecyclerView
        RecyclerView recyclerView = binding.scheduleAnimeRecycler;

        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        adapter = new ScheduleRecyclerAdapter(anime -> {
            Intent intent = new Intent(getActivity(), AnimeDetailsActivity.class);
            intent.putExtra("anime", anime);
            intent.putExtra("type", ModelType.Anime.SEASONAL.name());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        String scheduleReminder = Data.getProperties(Data.CONFIGURATION.APP)
                .getProperty("schedule_reminder", "true");

        if (!Boolean.parseBoolean(scheduleReminder)) {
            binding.episodeCardButton.setVisibility(View.GONE);
        }

        binding.scheduleHideTip.setOnClickListener(v -> {
            binding.episodeCardButton.setVisibility(View.GONE);

            Data.getProperties(Data.CONFIGURATION.APP).setProperty("schedule_reminder", "false");
            Data.saveProperties(Data.CONFIGURATION.APP);
        });

        viewModel.getAnimeFromSchedule().observe(getViewLifecycleOwner(), seasonalAnimeList -> {
            enableChips();
            binding.loadingBar.setVisibility(View.GONE);

            if (seasonalAnimeList == null) {
                binding.emptySchedulePopup.setVisibility(View.VISIBLE);
                binding.scheduleAnimeRecycler.setVisibility(View.INVISIBLE);
                return;
            }

            binding.scheduleAnimeRecycler.setVisibility(View.VISIBLE);

            manager.scrollToPosition(0);
            adapter.replaceData(seasonalAnimeList);
            adapter.notifyDataSetChanged();
        });

        viewModel.getAvailableDaysOfWeek().observe(getViewLifecycleOwner(), daysOfWeek -> {
            if (daysOfWeek == null) {
                binding.loadingBar.setVisibility(View.GONE);
                binding.emptySchedulePopup.setVisibility(View.VISIBLE);
                return;
            }

            displaySchedule(daysOfWeek);
            binding.dowSelectorScroll.setVisibility(View.VISIBLE);
        });

        viewModel.reloadSchedule(binding);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.clear();
    }

    private void displaySchedule(ArrayList<DayOfWeek> daysOfWeek) {
        viewModel.showDaySchedule(daysOfWeek.get(0));

        boolean firstDone = false;

        for (DayOfWeek dow : daysOfWeek) {
            ComponentSuggestionChipBinding chipBinding = ComponentSuggestionChipBinding.inflate(
                    getLayoutInflater(),
                    binding.dowSelectorChips,
                    true
            );

            if (!firstDone) {
                chipBinding.getRoot().setChecked(true);
                chipBinding.getRoot().setEnabled(false);
                firstDone = true;
            }

            chipBinding.getRoot().setText(
                    Translation.getLocalizedDow(dow, getContext())
            );

            chipBinding.getRoot().setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    disableChips();
                    displayDaySchedule(dow);
                }
            });
        }
    }

    private void disableChips() {
        for (int i = 0; i < binding.dowSelectorChips.getChildCount(); i++) {
            final Chip child = (Chip) binding.dowSelectorChips.getChildAt(i);
            child.setEnabled(false);
        }
    }

    private void enableChips() {
        for (int i = 0; i < binding.dowSelectorChips.getChildCount(); i++) {
            final Chip child = (Chip) binding.dowSelectorChips.getChildAt(i);
            if (child.isChecked()) continue;
            child.setEnabled(true);
        }
    }

    private void displayDaySchedule(DayOfWeek day) {
        if (viewModel == null) return;

        viewModel.showDaySchedule(day);
    }
}