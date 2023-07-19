package com.astarivi.kaizoyu.core.adapters.modal;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.databinding.BottomSheetGenericBinding;
import com.astarivi.kaizoyu.databinding.ItemSheetGenericBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class GenericModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "GenericModalBottomSheet";
    private BottomSheetGenericBinding binding;
    private final ResultListener listener;
    private CancelListener cancelListener;
    private final ModalOption[] options;
    private final String title;

    public GenericModalBottomSheet() {
        listener = null;
        options = null;
        title = null;
    }

    public GenericModalBottomSheet(String title, ModalOption[] options, ResultListener l) {
        listener = l;
        this.options = options;
        this.title = title;
    }

    public void setCancelListener(CancelListener listener) {
        cancelListener = listener;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        if (cancelListener != null) cancelListener.onCancel();
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

        for (ModalOption option : options) {
            ItemSheetGenericBinding reBinding = ItemSheetGenericBinding.inflate(
                    getLayoutInflater(),
                    binding.options,
                    true
            );

            reBinding.itemTitle.setText(option.getName());
            reBinding.itemDescription.setText(option.getValue());

            if (option.shouldHighlight()) {
                reBinding.getRoot().setStrokeColor(ContextCompat.getColor(requireContext(), R.color.branding_alternate));
            }

            int finalIndex = index;
            reBinding.getRoot().setOnClickListener(v -> {
                dismiss();
                listener.onOptionSelected(finalIndex, option.shouldHighlight());
            });

            index++;
        }
    }

    public interface CancelListener {
        void onCancel();
    }

    public interface ResultListener {
        void onOptionSelected(int index, boolean wasHighlighted);
    }
}
