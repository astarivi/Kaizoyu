package com.astarivi.kaizoyu.gui.settings;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
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

import java.io.File;
import java.text.ParseException;


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

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(@NonNull View root, Bundle savedState) {
        binding.updateSettingDescription.setText(
                String.format(getString(R.string.updatecheck_description), UpdateManager.VERSION)
        );

        binding.discordServer.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/Yy6BphADFc")))
        );

        binding.openLogs.setOnClickListener(v -> {

            File logFile = new File (requireActivity().getFilesDir(), "log.txt");

            if (!logFile.exists()) return;

            startActivity(
                    Intent.createChooser(
                        new Intent(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                        requireContext(),
                                        "com.astarivi.kaizoyu.fileprovider",
                                        logFile
                                ))
                                .setType("text/plain"),
                        "Share logs"
                    )
            );
        });

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
        binding.strictModeValue.setOnCheckedChangeListener(this::triggerSave);

        binding.openLicensesActivity.setOnClickListener(view -> {
            if (getActivity() == null) return;

            Intent intent = new Intent(requireActivity(), LicensesActivity.class);
            startActivity(intent);
        });

        binding.clearCacheTrigger.setOnClickListener(view -> {
            Utils.clearCache();
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

                        ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

                        if (result == UpdaterModalBottomSheet.Result.UPDATE_NOW) {
                            appProperties.setBooleanProperty("skip_version", false);
                            Intent intent = new Intent(getContext(), UpdaterActivity.class);
                            intent.putExtra("latestUpdate", update);
                            startActivity(intent);
                        }

                        if (result == UpdaterModalBottomSheet.Result.NEVER) {
                            appProperties.setProperty("skip_version", update.version);
                        }

                        appProperties.save();
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
        ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);

        config.setIntProperty(
                "night_theme",
                nightTheme
        );

        config.setBooleanProperty(
                "analytics",
                binding.analyticsValue.isChecked()
        );

        config.setBooleanProperty(
                "show_ipv6",
                binding.ipv6SorcesValue.isChecked()
        );

        config.setBooleanProperty(
                "prefer_english",
                binding.preferEnglishValue.isChecked()
        );

        config.setBooleanProperty(
                "auto_favorite",
                binding.autoFavoriteValue.isChecked()
        );

        config.setBooleanProperty(
                "advanced_search",
                binding.advancedSearch.isChecked()
        );

        config.setBooleanProperty(
                "strict_mode",
                binding.strictModeValue.isChecked()
        );

        config.save();

        Data.reloadProperties();
    }

    private void loadSettings() {
        ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);

        // Translated Values
        TextView nightThemeText = binding.nightThemeValue;
        nightTheme = config.getIntProperty("night_theme", 0);

        nightThemeText.setText(
                Translation.getNightThemeTranslation(
                        nightTheme,
                        getContext()
                )
        );

        // Switches
        binding.analyticsValue.setChecked(
                config.getBooleanProperty("analytics", true)
        );

        binding.strictModeValue.setChecked(
                config.getBooleanProperty("strict_mode", false)
        );

        binding.preferEnglishValue.setChecked(
                config.getBooleanProperty("prefer_english", true)
        );

        binding.autoFavoriteValue.setChecked(
                config.getBooleanProperty("auto_favorite", false)
        );

        binding.themeValue.setText(
                Theme.getCurrentTheme().getTitle(requireContext())
        );

        binding.advancedSearch.setChecked(
                config.getBooleanProperty("advanced_search", false)
        );

        // IPv6 stuff
        MaterialSwitch ipv6Sources = binding.ipv6SorcesValue;
        ipv6Sources.setChecked(
                config.getBooleanProperty("show_ipv6", false)
        );

        ipv6Sources.setEnabled(false);

        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            boolean ipv6Capable = Utils.isIPv6Capable();
            ipv6Sources.post(() -> setIPv6Capability(ipv6Capable));
        });
    }

    private void setIPv6Capability(boolean isIpv6Capable) {
        if (!isIpv6Capable) {
            ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);

            config.setBooleanProperty(
                    "show_ipv6",
                    false
            );

            config.save();

            try {
                binding.textView5.setOnClickListener(
                        v -> Toast.makeText(getContext(), getString(R.string.ipv6_toast), Toast.LENGTH_SHORT).show()
                );
            } catch (NullPointerException ignored) {
                // This view doesn't exist anymore or is not in focus
            }

            return;
        }

        try {
            binding.ipv6SorcesValue.setEnabled(true);
        } catch (NullPointerException ignored) {
            // This view doesn't exist anymore or is not in focus
        }
    }
    // endregion
}