package com.astarivi.kaizoyu.gui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.databinding.BottomSheetWelcomeBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class WelcomeModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "WelcomeBottomModalSheet";
    private BottomSheetWelcomeBinding binding;
    private Callback callback = null;

    public WelcomeModalBottomSheet() {
    }

    public WelcomeModalBottomSheet(Callback c) {
        callback = c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ExtendedProperties appSettings = Data.getProperties(
                Data.CONFIGURATION.APP
        );

        binding.analyticsValue.setChecked(appSettings.getBooleanProperty("analytics", false));
        binding.analyticsValue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setBooleanProperty("analytics", isChecked);
            appSettings.save();

            Data.reloadProperties();
        });

        binding.updateCheck.setChecked(appSettings.getBooleanProperty("autoupdate", true));
        binding.updateCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setBooleanProperty("autoupdate", isChecked);
            appSettings.save();
        });

        binding.languageValue.setChecked(appSettings.getBooleanProperty("prefer_english", true));
        binding.languageValue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setBooleanProperty("prefer_english", isChecked);
            appSettings.save();
        });

        binding.autoFavValue.setChecked(appSettings.getBooleanProperty("auto_favorite", false));
        binding.autoFavValue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setBooleanProperty("auto_favorite", isChecked);
            appSettings.save();
        });

        binding.goButton.setOnClickListener(v ->
           dismiss()
        );
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (callback != null) callback.onDismiss();
    }

    public interface Callback {
        void onDismiss();
    }
}
