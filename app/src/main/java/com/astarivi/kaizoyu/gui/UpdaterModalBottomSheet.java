package com.astarivi.kaizoyu.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.BottomSheetUpdaterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


// TODO: Check if immersive mode hasn't messed this up.
public class UpdaterModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "UpdaterBottomModalSheet";
    private BottomSheetUpdaterBinding binding;
    private final ResultListener listener;
    private final UpdateManager.LatestUpdate update;

    public UpdaterModalBottomSheet() {
        listener = null;
        update = null;
    }

    public UpdaterModalBottomSheet(UpdateManager.LatestUpdate latestUpdate, ResultListener listener) {
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

        ViewCompat.setOnApplyWindowInsetsListener(
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
                        update.version
                )
        );

        if (update.body == null || update.body.equals("")) {
            binding.description.setText(
                getString(R.string.update_no_description)
            );
        } else {
            binding.description.setText(
                    update.body
            );
        }

        binding.neverInstall.setOnClickListener(v -> {
            if (listener == null) return;
            dismiss();
            listener.onResult(Result.NEVER, update);
        });

        binding.laterInstall.setOnClickListener(v -> {
            if (listener == null) return;
            dismiss();
            listener.onResult(Result.SKIP, update);
        });

        binding.nowInstall.setOnClickListener(v -> {
            if (listener == null) return;
            dismiss();
            listener.onResult(Result.UPDATE_NOW, update);
        });
    }

    public enum Result {
        UPDATE_NOW,
        SKIP,
        NEVER
    }

    public interface ResultListener {
        void onResult(Result result, UpdateManager.LatestUpdate update);
    }
}
