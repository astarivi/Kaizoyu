package com.astarivi.kaizoyu.gui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astarivi.kaizoyu.core.adapters.tab.TabFragment;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.databinding.FragmentScheduleBinding;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.schedule.recycler.ScheduleRecyclerAdapter;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Translation;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.tabs.TabLayout;

import org.tinylog.Logger;

import java.time.DayOfWeek;
import java.util.ArrayList;


public class ScheduleFragment extends TabFragment {
    private ScheduleViewModel viewModel;
    private FragmentScheduleBinding binding;
    private ScheduleRecyclerAdapter adapter;
    private LinearLayoutManager manager;
    private ArrayList<DayOfWeek> dowValues;

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
        binding.dowTabs.getLayoutTransition().setAnimateParentHierarchy(false);

        ViewCompat.setOnApplyWindowInsetsListener(
                binding.scheduleAnimeRecycler,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    if (getContext() == null) return windowInsets;

                    v.setPadding(
                            0,
                            (int) Utils.convertDpToPixel(4, requireContext()),
                            0,
                            insets.bottom + (int) Utils.convertDpToPixel(4, requireContext())
                    );

                    return windowInsets;
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                binding.dowTabs,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                    v.setPadding(0, insets.top, 0, 0);

                    return windowInsets;
                }
        );

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            viewModel.reloadSchedule(binding);
        });

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHalfHeightDp = (int) (displayMetrics.heightPixels / displayMetrics.density);

        // Make swipe refresh half of the screen height
        binding.swipeRefresh.setDistanceToTriggerSync(screenHalfHeightDp);

        binding.dowTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                shouldTabsBeInteractive(false);

                try {
                    displayDaySchedule(dowValues.get(tab.getPosition()));
                } catch(IndexOutOfBoundsException e) {
                    Logger.error("Schedule day tab index out of bounds");
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                binding.scheduleAnimeRecycler.smoothScrollToPosition(0);
            }
        });

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

        if (
                !Data.getProperties(Data.CONFIGURATION.APP)
                        .getBooleanProperty("schedule_reminder", true)
        ) {
            binding.episodeCardButton.setVisibility(View.GONE);
        }

        binding.scheduleHideTip.setOnClickListener(v -> {
            binding.episodeCardButton.setVisibility(View.GONE);
            ExtendedProperties appConfig = Data.getProperties(Data.CONFIGURATION.APP);
            appConfig.setBooleanProperty("schedule_reminder", false);
            appConfig.save();
        });

        viewModel.getAnimeFromSchedule().observe(getViewLifecycleOwner(), seasonalAnimeList -> {
            shouldTabsBeInteractive(true);
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
            binding.dowTabs.setVisibility(View.VISIBLE);
        });

        viewModel.reloadSchedule(binding);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.clear();
    }

    private void displaySchedule(ArrayList<DayOfWeek> daysOfWeek) {
        dowValues = daysOfWeek;
        binding.dowTabs.removeAllTabs();
        viewModel.showDaySchedule(daysOfWeek.get(0));

        TabLayout tabs = binding.dowTabs;

        for (DayOfWeek dow : daysOfWeek) {
            TabLayout.Tab tab = tabs.newTab();

            tab.setText(
                    Translation.getLocalizedDow(dow, getContext())
            );

            tabs.addTab(tab);
        }
    }

    private void shouldTabsBeInteractive(boolean value) {
        for (int i = 0; i < binding.dowTabs.getTabCount(); i++) {
            final TabLayout.Tab tab = binding.dowTabs.getTabAt(i);
            if (tab == null) continue;
            tab.view.setEnabled(value);
        }
    }

    private void displayDaySchedule(DayOfWeek day) {
        if (viewModel == null) return;

        viewModel.showDaySchedule(day);
    }

    @Override
    public void onTabReselected() {
        binding.scheduleAnimeRecycler.smoothScrollToPosition(0);
    }
}