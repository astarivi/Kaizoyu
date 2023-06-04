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
    private final Fragment[] fragmentList;

    public TabAdapter(AppCompatActivity activity) {
        super(activity);

        fragmentList = new Fragment[] {
                new HomeFragment(),
                new ScheduleFragment(),
                new LibraryFragment(),
                new SettingsFragment()
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList[position];
    }

    // Why is this duplicated?, because my implementation of the original method is stupid
    // and I may change it later. Why is this method stupid?, because I am stupid.
    public Fragment getFragment(int position) {
        return fragmentList[position];
    }

    @Override
    public int getItemCount() {
        return fragmentList.length;
    }
}
