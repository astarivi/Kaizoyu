package com.astarivi.kaizoyu.gui.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.astarivi.kaizoyu.core.adapters.tab.TabFragment;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.databinding.FragmentLibraryBinding;
import com.astarivi.kaizoyu.gui.library.watching.SharedLibraryActivity;
import com.astarivi.kaizoyu.utils.Utils;


public class LibraryFragment extends TabFragment {
    FragmentLibraryBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.getRoot(),
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    if (getContext() == null) return windowInsets;

                    v.setPadding(
                            0,
                            insets.top + (int) Utils.convertDpToPixel(8, requireContext()),
                            0,
                            insets.bottom + (int) Utils.convertDpToPixel(8, requireContext())
                    );

                    return windowInsets;
                }
        );

        binding.currentlyWatching.setOnClickListener(v -> {
            if (getActivity() == null) return;

            Intent intent = new Intent(requireActivity(), SharedLibraryActivity.class);
            intent.putExtra("local_type", ModelType.LocalAnime.FAVORITE.name());
            startActivity(intent);
        });
    }

    @Override
    public void onTabReselected() {

    }
}