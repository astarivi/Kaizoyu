package com.astarivi.kaizoyu.gui.settings;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.theme.Theme;
import com.astarivi.kaizoyu.databinding.BottomSheetAppThemeBinding;
import com.astarivi.kaizoyu.databinding.ItemThemeBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class ThemeSelectionModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "ThemeModalBottomSheet";
    private BottomSheetAppThemeBinding binding;
    private final ResultListener listener;

    public ThemeSelectionModalBottomSheet() {
        listener = null;
    }

    public ThemeSelectionModalBottomSheet(ResultListener l) {
        listener = l;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAppThemeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.themeList.removeAllViews();
        binding.loadingBar.setVisibility(View.VISIBLE);

        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            ArrayList<View> resultingViews = new ArrayList<>();

            Theme currentTheme = Theme.getCurrentTheme();

            for (Theme theme : Theme.values()) {
                // No support for dynamic colors before Android 12
                if (theme == Theme.DYNAMIC_COLORS && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    continue;
                }

                ItemThemeBinding reBinding = ItemThemeBinding.inflate(
                        getLayoutInflater(),
                        null,
                        false
                );

                Context context = getContext();

                // If it has died already, let it go.
                if (context == null) return;

                reBinding.themeTitle.setText(
                        theme.getTitle(context)
                );

                reBinding.themeDescription.setText(
                        theme.getDescription(context)
                );

                boolean isThemeDisplayed = theme == currentTheme;

                reBinding.getRoot().setOnClickListener(v -> {
                    dismiss();
                    // Nothing to do, really
                    if (isThemeDisplayed) return;
                    // Call daddy back with results
                    if (listener != null) listener.onThemeSelected(theme);
                });

                if (isThemeDisplayed) {
                    reBinding.getRoot().setStrokeColor(ContextCompat.getColor(context, R.color.branding_alternate));
                    resultingViews.add(0, reBinding.getRoot());
                } else {
                    resultingViews.add(reBinding.getRoot());
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.getRoot().post(() -> {
                    binding.loadingBar.setVisibility(View.GONE);

                    for (View resultingView : resultingViews) {
                        binding.themeList.addView(resultingView);
                    }
                });
            } else {
                Utils.runOnUiThread(() -> {
                    binding.loadingBar.setVisibility(View.GONE);

                    for (View resultingView : resultingViews) {
                        binding.themeList.addView(resultingView);
                    }
                });
            }
        });
    }

    public interface ResultListener {
        void onThemeSelected(Theme theme);
    }
}
