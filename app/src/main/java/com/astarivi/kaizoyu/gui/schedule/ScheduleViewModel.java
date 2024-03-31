package com.astarivi.kaizoyu.gui.schedule;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.anilist.exception.AniListException;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.models.SeasonalAnime;
import com.astarivi.kaizoyu.core.schedule.AssistedScheduleFetcher;
import com.astarivi.kaizoyu.databinding.FragmentScheduleBinding;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;

import lombok.Getter;


public class ScheduleViewModel extends ViewModel {
    @Getter
    private final MutableLiveData<Exception> processException = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<ArrayList<DayOfWeek>> availableDaysOfWeek = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<TreeSet<SeasonalAnime>> animeFromSchedule = new MutableLiveData<>();
    private final MutableLiveData<TreeMap<DayOfWeek, TreeSet<SeasonalAnime>>> schedule = new MutableLiveData<>(null);
    private Future<?> reloadFuture = null;

    public void reloadSchedule(@NotNull FragmentScheduleBinding binding) {
        if (reloadFuture != null && !reloadFuture.isDone()) return;

        binding.emptySchedulePopup.setVisibility(View.GONE);
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.scheduleAnimeRecycler.setVisibility(View.INVISIBLE);
        binding.dowTabs.setVisibility(View.GONE);

        reloadFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            TreeMap<DayOfWeek, TreeSet<SeasonalAnime>> fetchedSchedule;
            try {
                fetchedSchedule = AssistedScheduleFetcher.getSchedule();
            } catch (AniListException | IOException e) {
                processException.postValue(e);
                return;
            }

            schedule.postValue(fetchedSchedule);
            updateDayOfWeekChips(fetchedSchedule);
        });
    }

    @ThreadedOnly
    private void updateDayOfWeekChips(@Nullable TreeMap<DayOfWeek, TreeSet<SeasonalAnime>> fetchedSchedule) {
        if (fetchedSchedule == null) {
            availableDaysOfWeek.postValue(null);
            return;
        }

        ArrayList<DayOfWeek> availableDow = new ArrayList<>(fetchedSchedule.keySet());

        int todayIndex = availableDow.indexOf(
                LocalDate.now().getDayOfWeek()
        );

        if (todayIndex != -1  && todayIndex != 0) {
            Collections.rotate(availableDow, -todayIndex);
        }

        availableDaysOfWeek.postValue(availableDow);
    }

    public void showDaySchedule(@NotNull DayOfWeek day) {
        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            if (schedule.getValue() == null) return;

            animeFromSchedule.postValue(
                    schedule.getValue().get(day)
            );
        });
    }
}
