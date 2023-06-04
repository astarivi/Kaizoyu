package com.astarivi.kaizoyu.details.gui.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.astarivi.kaizoyu.details.gui.AnimeEpisodesFragment;
import com.astarivi.kaizoyu.details.gui.AnimeInfoFragment;

import java.util.Arrays;
import java.util.List;


public class DetailsTabAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList;

    public DetailsTabAdapter(AppCompatActivity activity, Bundle arguments) {
        super(activity);

        fragmentList = Arrays.asList(
                new AnimeInfoFragment(),
                new AnimeEpisodesFragment()
        );

        for (Fragment fg : fragmentList) {
            fg.setArguments(arguments);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    // Why is this duplicated?, check kaizoyu.gui.adapters.TabAdapter
    public Fragment getFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
