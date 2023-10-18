package com.astarivi.kaizoyu.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.databinding.BottomSheetNotificationBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class NotificationModalBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "NotificationBottomModalSheet";
    private BottomSheetNotificationBinding binding;
    private Callback callback = null;

    public NotificationModalBottomSheet() {
    }
    public NotificationModalBottomSheet(Callback c) {
        callback = c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.denyButton.setOnClickListener(v -> dismiss());
        binding.confirmButton.setOnClickListener(v -> {
            dismiss();
            if (callback != null) callback.onSuccess();
        });
    }

    public interface Callback {
        void onSuccess();
    }
}
