package com.astarivi.kaizoyu.details.gui.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.astarivi.kaizoyu.details.gui.AnimeEpisodesFragment;
import com.astarivi.kaizoyu.details.gui.AnimeInfoFragment;


public class DetailsTabAdapter extends FragmentStateAdapter {
    private final Bundle bundle;

    public DetailsTabAdapter(AppCompatActivity activity, Bundle arguments) {
        super(activity);
        this.bundle = arguments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        if (position == 0) {
            fragment = new AnimeInfoFragment();
        } else {
            fragment = new AnimeEpisodesFragment();
        }

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
