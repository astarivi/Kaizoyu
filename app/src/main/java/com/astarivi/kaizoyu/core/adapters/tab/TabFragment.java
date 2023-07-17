package com.astarivi.kaizoyu.core.adapters.tab;

import androidx.fragment.app.Fragment;


public abstract class TabFragment extends Fragment {
    public abstract void onTabReselected();
    public boolean shouldFragmentInterceptBack() {
        return false;
    }
}
