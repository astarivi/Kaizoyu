package com.astarivi.kaizoyu.gui.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.astarivi.kaizoyu.gui.home.HomeFragment;
import com.astarivi.kaizoyu.gui.library.LibraryFragment;
import com.astarivi.kaizoyu.gui.schedule.ScheduleFragment;
import com.astarivi.kaizoyu.gui.settings.SettingsFragment;


public class TabAdapter extends FragmentStateAdapter {
    public TabAdapter(AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ScheduleFragment();
            case 2:
                return new LibraryFragment();
            default:
                return new SettingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
