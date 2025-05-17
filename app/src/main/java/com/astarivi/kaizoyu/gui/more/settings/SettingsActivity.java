package com.astarivi.kaizoyu.gui.more.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.adapters.modal.GenericModalBottomSheet;
import com.astarivi.kaizoyu.core.adapters.modal.ModalOption;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.core.theme.Theme;
import com.astarivi.kaizoyu.core.threading.workers.EpisodePeriodicWorker;
import com.astarivi.kaizoyu.core.threading.workers.UpdatePeriodicWorker;
import com.astarivi.kaizoyu.databinding.ActivitySettingsBinding;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Translation;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.io.File;


public class SettingsActivity extends AppCompatActivityTheme {
    private ActivitySettingsBinding binding;
    private int nightTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.settingsMainContainer.getLayoutTransition().setAnimateParentHierarchy(false);

        binding.internalToolbar.setNavigationOnClickListener(v -> finish());

        WindowCompatUtils.setWindowFullScreen(getWindow());

        binding.statusBarScrim.setBackgroundColor(
                Colors.getSemiTransparentStatusBar(
                        binding.getRoot(),
                        R.attr.colorSurfaceVariant
                )
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.statusBarScrim,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = insets.top;
                    v.setLayoutParams(params);

                    return windowInsets;
                }
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.settingsScrollContainer,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    v.setPadding(
                            0,
                            (int) Utils.convertDpToPixel(4, this),
                            0,
                            insets.bottom + (int) Utils.convertDpToPixel(4, this)
                    );

                    return windowInsets;
                }
        );

        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.internalToolbar,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                    v.setPadding(0, insets.top, 0, 0);

                    return windowInsets;
                }
        );

        binding.developerSection.setVisibility(View.GONE);

        binding.openLogs.setOnClickListener(v -> {
            File logFile = new File (getFilesDir(), "log.txt");

            if (!logFile.exists()) {
                Toast.makeText(this, getString(R.string.dev_logs_toast), Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(
                    Intent.createChooser(
                            new Intent(Intent.ACTION_SEND)
                                    .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                            this,
                                            getString(R.string.provider_authority),
                                            logFile
                                    ))
                                    .setType("text/plain"),
                            "Share logs"
                    )
            );
        });

        binding.miscTitle.setOnLongClickListener(v -> {
            ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);
            config.setBooleanProperty("developer_menu", true);
            binding.developerSection.setVisibility(View.VISIBLE);
            config.save();

            Toast.makeText(this, getString(R.string.dev_section_toast), Toast.LENGTH_SHORT).show();
            return true;
        });

        binding.hideDevMenu.setOnClickListener(v -> {
            ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);
            config.setBooleanProperty("developer_menu", false);
            binding.developerSection.setVisibility(View.GONE);
            config.save();
        });

        loadSettings();

        // Set click listeners
        binding.nightThemeTrigger.setOnClickListener(this::showNightThemePopup);

        binding.analyticsValue.setOnCheckedChangeListener(this::triggerSave);

        MaterialSwitch ipv6Sources = binding.ipv6SorcesValue;
        ipv6Sources.setOnCheckedChangeListener(this::triggerSave);

        MaterialSwitch preferEnglishTitles = binding.preferEnglishValue;
        preferEnglishTitles.setOnCheckedChangeListener(this::triggerSave);

        binding.autoUpdateValue.setOnCheckedChangeListener(this::triggerSave);
        binding.autoFavoriteValue.setOnCheckedChangeListener(this::triggerSave);
        binding.advancedSearch.setOnCheckedChangeListener(this::triggerSave);
        binding.strictModeValue.setOnCheckedChangeListener(this::triggerSave);
        binding.autoMoveValue.setOnCheckedChangeListener(this::triggerSave);
        binding.xdccValue.setOnCheckedChangeListener(this::triggerSave);
        binding.gdprValue.setOnCheckedChangeListener((v, val) -> {
            StartAppSDK.setUserConsent (
                    this,
                    "pas",
                    System.currentTimeMillis(),
                    val
            );

            this.saveSettings();
        });

        binding.themeTrigger.setOnClickListener(v -> {
            ThemeSelectionModalBottomSheet modalBottomSheet = new ThemeSelectionModalBottomSheet(theme -> {
                AnalyticsClient.logBreadcrumb("Theme changed to " + theme.getTitle(this));

                Theme.setTheme(theme, this);

                Context ctx = getApplicationContext();
                PackageManager pm = ctx.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(ctx.getPackageName());
                if (intent == null) return;
                ComponentName componentName = intent.getComponent();
                if (componentName == null) return;
                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                ctx.startActivity(mainIntent);
                Runtime.getRuntime().exit(0);
            });
            modalBottomSheet.show(getSupportFragmentManager(), ThemeSelectionModalBottomSheet.TAG);
        });

        binding.testTask.setOnClickListener(v -> {
            GenericModalBottomSheet modalBottomSheet = new GenericModalBottomSheet(
                    getString(R.string.dev_test_task_title),
                    new ModalOption[]{
                            new ModalOption(
                                    UpdatePeriodicWorker.class.getSimpleName(),
                                    UpdatePeriodicWorker.class.getName()
                            ),
                            new ModalOption(
                                    EpisodePeriodicWorker.class.getSimpleName(),
                                    EpisodePeriodicWorker.class.getName()
                            )
                    },
                    (index, wasHighlighted) -> {
                        OneTimeWorkRequest request;

                        switch (index) {
                            case 0:
                                request = new OneTimeWorkRequest.Builder(
                                        UpdatePeriodicWorker.class
                                ).build();
                                break;
                            case 1:
                            default:
                                request = new OneTimeWorkRequest.Builder(
                                        EpisodePeriodicWorker.class
                                ).build();
                                break;
                        }

                        WorkManager.getInstance(
                                getApplicationContext()
                        ).enqueueUniqueWork(
                                "testing_worker",
                                ExistingWorkPolicy.REPLACE,
                                request
                        );
                    }
            );

            modalBottomSheet.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
        });
    }

    private void showNightThemePopup(View v) {
        GenericModalBottomSheet genericModal = new GenericModalBottomSheet(
                getString(R.string.night_theme_context),
                new ModalOption[]{
                        new ModalOption(
                                Translation.getNightThemeTranslation(
                                        0,
                                        this
                                ),
                                getString(R.string.night_theme_default_d),
                                nightTheme == 0
                        ),
                        new ModalOption(
                                Translation.getNightThemeTranslation(
                                        1,
                                        this
                                ),
                                getString(R.string.night_theme_day_d),
                                nightTheme == 1
                        ),
                        new ModalOption(
                                Translation.getNightThemeTranslation(
                                        2,
                                        this
                                ),
                                getString(R.string.night_theme_night_d),
                                nightTheme == 2
                        )
                },
                (index, wasHighlighted) -> {
                    // Already selected
                    if (wasHighlighted) return;

                    binding.nightThemeValue.setText(
                            Translation.getNightThemeTranslation(
                                    index,
                                    this
                            )
                    );
                    nightTheme = index;
                    saveSettings();
                }
        );

        genericModal.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
    }

    private void triggerSave(View v, boolean value) {
        saveSettings();
    }

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
                "autoupdate",
                binding.autoUpdateValue.isChecked()
        );

        config.setBooleanProperty(
                "use_xdcc",
                binding.xdccValue.isChecked()
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
                "auto_move",
                binding.autoMoveValue.isChecked()
        );

        config.setBooleanProperty(
                "advanced_search",
                binding.advancedSearch.isChecked()
        );

        config.setBooleanProperty(
                "strict_mode",
                binding.strictModeValue.isChecked()
        );

        config.setBooleanProperty(
                "gdpr_consent",
                binding.gdprValue.isChecked()
        );

        StartAppSDK.setUserConsent(
                this,
                "pas",
                System.currentTimeMillis(),
                binding.gdprValue.isChecked()
        );

        config.save();

        Data.reloadProperties();
    }

    private void loadSettings() {
        ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);

        // Hidden sections
        if (config.getBooleanProperty("developer_menu", false)) {
            binding.developerSection.setVisibility(View.VISIBLE);
        }

        // Translated Values
        TextView nightThemeText = binding.nightThemeValue;
        nightTheme = config.getIntProperty("night_theme", 0);

        nightThemeText.setText(
                Translation.getNightThemeTranslation(
                        nightTheme,
                        this
                )
        );

        // Switches
        binding.xdccValue.setChecked(
                config.getBooleanProperty("use_xdcc", false)
        );

        binding.autoUpdateValue.setChecked(
                config.getBooleanProperty("autoupdate", true)
        );

        binding.analyticsValue.setChecked(
                config.getBooleanProperty("analytics", false)
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

        binding.autoMoveValue.setChecked(
                config.getBooleanProperty("auto_move", false)
        );

        binding.themeValue.setText(
                Theme.getCurrentTheme().getTitle(this)
        );

        binding.advancedSearch.setChecked(
                config.getBooleanProperty("advanced_search", false)
        );

        binding.gdprValue.setChecked(
                config.getBooleanProperty("gdpr_consent", false)
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
                        v -> Toast.makeText(this, getString(R.string.ipv6_toast), Toast.LENGTH_SHORT).show()
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
}
