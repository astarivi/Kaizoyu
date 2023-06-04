package com.astarivi.kaizoyu.gui.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.astarivi.kaizoyu.MainActivity;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.theme.Theme;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.FragmentSettingsBinding;
import com.astarivi.kaizoyu.gui.UpdaterModalBottomSheet;
import com.astarivi.kaizoyu.licenses.LicensesActivity;
import com.astarivi.kaizoyu.updater.UpdaterActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Translation;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.ParseException;
import java.util.Properties;


public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private int nightTheme;

    // region Initialization
    public SettingsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    // endregion

    @Override
    public void onViewCreated(@NonNull View root, Bundle savedState) {
        loadSettings();

        // Set click listeners
        binding.nightThemeTrigger.setOnClickListener(this::showNightThemePopup);

        MaterialSwitch analytics = binding.analyticsValue;
        analytics.setOnCheckedChangeListener(this::triggerSave);

        MaterialSwitch ipv6Sources = binding.ipv6SorcesValue;
        ipv6Sources.setOnCheckedChangeListener(this::triggerSave);

        MaterialSwitch preferEnglishTitles = binding.preferEnglishValue;
        preferEnglishTitles.setOnCheckedChangeListener(this::triggerSave);

        binding.autoFavoriteValue.setOnCheckedChangeListener(this::triggerSave);
        binding.advancedSearch.setOnCheckedChangeListener(this::triggerSave);

        binding.openLicensesActivity.setOnClickListener(view -> {
            if (getActivity() == null) return;

            Intent intent = new Intent(requireActivity(), LicensesActivity.class);
            startActivity(intent);
        });

        binding.clearCacheTrigger.setOnClickListener(view -> {
            MainActivity.getInstance().getDataAssistant().clearCache();
            Toast.makeText(getContext(), getString(R.string.cache_toast), Toast.LENGTH_SHORT).show();
        });

        binding.clearSearchTrigger.setOnClickListener(v -> {
            Data.getRepositories().getSearchHistoryRepository().deleteAllAsync();
            Toast.makeText(getContext(), getString(R.string.history_toast), Toast.LENGTH_SHORT).show();
        });

        binding.themeTrigger.setOnClickListener(v -> {
            ThemeSelectionModalBottomSheet modalBottomSheet = new ThemeSelectionModalBottomSheet(theme -> {
                AnalyticsClient.logEvent("theme_changed", theme.getTitle(requireContext()));

                Theme.setTheme(theme, requireContext());

                Context ctx = requireActivity().getApplicationContext();
                PackageManager pm = ctx.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(ctx.getPackageName());
                if (intent == null) return;
                ComponentName componentName = intent.getComponent();
                if (componentName == null) return;
                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                ctx.startActivity(mainIntent);
                Runtime.getRuntime().exit(0);
            });
            modalBottomSheet.show(requireActivity().getSupportFragmentManager(), ThemeSelectionModalBottomSheet.TAG);
        });

        binding.updateAppTrigger.setOnClickListener(v ->
            Threading.submitTask(Threading.TASK.INSTANT, () -> {
                UpdateManager updateManager = new UpdateManager();

                UpdateManager.LatestUpdate latestUpdate;
                try {
                    latestUpdate = updateManager.getLatestUpdate();
                } catch (ParseException e) {
                    AnalyticsClient.onError(
                            "update_parse",
                            "Couldn't parse update from KaizoDelivery",
                            e
                    );
                    return;
                }

                if (latestUpdate == null) {
                    binding.getRoot().post(() -> Toast.makeText(getContext(), getString(R.string.update_already_latest), Toast.LENGTH_SHORT).show());
                    return;
                }

                binding.getRoot().post(() -> {
                    UpdaterModalBottomSheet modalBottomSheet = new UpdaterModalBottomSheet(latestUpdate, (result, update) -> {
                        if (result == UpdaterModalBottomSheet.Result.SKIP) return;

                        Properties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

                        if (result == UpdaterModalBottomSheet.Result.UPDATE_NOW) {
                            appProperties.setProperty("skip_version", "false");
                            Intent intent = new Intent(getContext(), UpdaterActivity.class);
                            intent.putExtra("latestUpdate", update);
                            startActivity(intent);
                        }

                        if (result == UpdaterModalBottomSheet.Result.NEVER) {
                            appProperties.setProperty("skip_version", update.version);
                        }

                        Data.saveProperties(Data.CONFIGURATION.APP);
                    });

                    modalBottomSheet.show(getParentFragmentManager(), UpdaterModalBottomSheet.TAG);
                });
            })
        );
    }

    // region Event listeners

    private void showNightThemePopup(View v) {
        final String night_theme_default = getString(R.string.night_theme_default);
        final String night_theme_day = getString(R.string.night_theme_day);
        final String night_theme_night = getString(R.string.night_theme_night);

        final String[] themes = {
                night_theme_default,
                night_theme_day,
                night_theme_night
        };

        TextView theme = binding.nightThemeValue;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.night_theme_context));
        builder.setItems(themes, (dialog, index) -> {
            nightTheme = index;
            theme.setText(themes[index]);
            saveSettings();
        });
        builder.show();
    }

    private void triggerSave(View v, boolean value) {
        saveSettings();
    }
    // endregion

    // region Configuration save and load
    private void saveSettings(){
        Properties config = Data.getProperties(Data.CONFIGURATION.APP);

        config.setProperty(
                "night_theme",
                Integer.toString(nightTheme)
        );

        MaterialSwitch analytics = binding.analyticsValue;
        config.setProperty(
                "analytics",
                String.valueOf(analytics.isChecked())
        );

        MaterialSwitch ipv6Sources = binding.ipv6SorcesValue;
        config.setProperty(
                "show_ipv6",
                String.valueOf(ipv6Sources.isChecked())
        );

        MaterialSwitch preferEnglishTitles = binding.preferEnglishValue;
        config.setProperty(
                "prefer_english",
                String.valueOf(preferEnglishTitles.isChecked())
        );

        config.setProperty(
                "auto_favorite",
                String.valueOf(
                        binding.autoFavoriteValue.isChecked()
                )
        );

        config.setProperty(
                "advanced_search",
                String.valueOf(
                        binding.advancedSearch.isChecked()
                )
        );

        Data.saveProperties(Data.CONFIGURATION.APP);
        Data.reloadProperties();
    }

    private void loadSettings() {
        Properties config = Data.getProperties(Data.CONFIGURATION.APP);

        // Translated Values
        TextView nightThemeText = binding.nightThemeValue;
        nightTheme = Integer.parseInt(config.getProperty("night_theme", "0"));

        nightThemeText.setText(
                Translation.getNightThemeTranslation(
                        nightTheme,
                        getContext()
                )
        );

        // Switches
        MaterialSwitch analytics = binding.analyticsValue;
        analytics.setChecked(
                Boolean.parseBoolean(config.getProperty("analytics", "true"))
        );

        MaterialSwitch ipv6Sources = binding.ipv6SorcesValue;
        ipv6Sources.setChecked(
                Boolean.parseBoolean(config.getProperty("show_ipv6", "false"))
        );

        MaterialSwitch preferEnglishTitles = binding.preferEnglishValue;
        preferEnglishTitles.setChecked(
                Boolean.parseBoolean(config.getProperty("prefer_english", "true"))
        );

        binding.autoFavoriteValue.setChecked(
                Boolean.parseBoolean(config.getProperty("auto_favorite", "false"))
        );

        binding.themeValue.setText(
                Theme.getCurrentTheme().getTitle(requireContext())
        );

        binding.advancedSearch.setChecked(
                Boolean.parseBoolean(config.getProperty("advanced_search", "false"))
        );

        ipv6Sources.setEnabled(false);
        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            boolean ipv6Capable = Utils.isIPv6Capable();
            ipv6Sources.post(() -> setIPv6Capability(ipv6Capable));
        });
    }

    private void setIPv6Capability(boolean isIpv6Capable) {
        if (!isIpv6Capable) {
            Properties config = Data.getProperties(Data.CONFIGURATION.APP);

            config.setProperty(
                    "show_ipv6",
                    "false"
            );

            Data.saveProperties(Data.CONFIGURATION.APP);

            binding.textView5.setOnClickListener(
                    v -> Toast.makeText(getContext(), getString(R.string.ipv6_toast), Toast.LENGTH_SHORT).show()
            );

            return;
        }

        binding.ipv6SorcesValue.setEnabled(true);
    }
    // endregion
}