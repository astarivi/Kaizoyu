package com.astarivi.kaizoyu.core.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.astarivi.kaizolib.common.util.StringPair;
import com.astarivi.kaizoyu.databinding.BottomSheetGenericBinding;
import com.astarivi.kaizoyu.databinding.ItemSheetGenericBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class GenericModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "GenericModalBottomSheet";
    private BottomSheetGenericBinding binding;
    private final ResultListener listener;
    private final StringPair[] options;
    private final String title;

    public GenericModalBottomSheet() {
        listener = null;
        options = null;
        title = null;
    }

    public GenericModalBottomSheet(String title, StringPair[] options, ResultListener l) {
        listener = l;
        this.options = options;
        this.title = title;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetGenericBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (listener == null || options == null || title == null)
            throw new IllegalStateException("No listener, options or title set for this element");

        binding.options.removeAllViews();
        binding.title.setText(title);

        int index = 0;

        for (StringPair option : options) {
            ItemSheetGenericBinding reBinding = ItemSheetGenericBinding.inflate(
                    getLayoutInflater(),
                    binding.options,
                    true
            );

            reBinding.itemTitle.setText(option.getName());
            reBinding.itemDescription.setText(option.getValue());

            int finalIndex = index;
            reBinding.getRoot().setOnClickListener(v -> {
                dismiss();
                listener.onOptionSelected(finalIndex);
            });

            index++;
        }
    }

    public interface ResultListener {
        void onOptionSelected(int index);
    }
}
