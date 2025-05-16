package com.astarivi.kaizoyu.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.util.Consumer;
import androidx.core.view.WindowInsetsCompat;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.BottomSheetUpdaterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class UpdaterModalBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "UpdaterBottomModalSheet";
    private BottomSheetUpdaterBinding binding;
    private final Consumer<Result> listener;
    private final UpdateManager.AppUpdate update;

    public UpdaterModalBottomSheet() {
        listener = null;
        update = null;
    }

    public UpdaterModalBottomSheet(UpdateManager.AppUpdate latestUpdate, Consumer<Result> listener) {
        this.listener = listener;
        this.update = latestUpdate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetUpdaterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (update == null) return;

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.getRoot(),
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    if (getContext() == null) return windowInsets;

                    v.setPadding(
                            0,
                            0,
                            0,
                            insets.bottom
                    );

                    return windowInsets;
        });

        binding.versionJump.setText(
                String.format(
                        getString(R.string.update_version_change),
                        UpdateManager.VERSION,
                        update.getVersion()
                )
        );

        if (update.getBody() == null || update.getBody().isEmpty()) {
            binding.description.setText(
                getString(R.string.update_no_description)
            );
        } else {
            binding.description.setText(
                    update.getBody()
            );
        }

        binding.neverInstall.setOnClickListener(v -> {
            if (listener == null) return;
            dismiss();
            listener.accept(Result.NEVER);
        });

        binding.laterInstall.setOnClickListener(v -> {
            if (listener == null) return;
            dismiss();
            listener.accept(Result.SKIP);
        });

        binding.nowInstall.setOnClickListener(v -> {
            if (listener == null) return;
            dismiss();
            listener.accept(Result.UPDATE_NOW);
        });
    }

    public enum Result {
        UPDATE_NOW,
        SKIP,
        NEVER
    }
}
