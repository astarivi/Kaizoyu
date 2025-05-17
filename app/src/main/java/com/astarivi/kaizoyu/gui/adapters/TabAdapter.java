package com.astarivi.kaizoyu.gui.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.astarivi.kaizoyu.gui.home.HomeFragment;
import com.astarivi.kaizoyu.gui.library.LibraryFragment;
import com.astarivi.kaizoyu.gui.more.MoreFragment;
import com.astarivi.kaizoyu.gui.schedule.ScheduleFragment;


public class TabAdapter extends FragmentStateAdapter {
    public TabAdapter(AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new HomeFragment();
            case 1 -> new ScheduleFragment();
            case 2 -> new LibraryFragment();
            default -> new MoreFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
