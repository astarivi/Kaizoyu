package com.astarivi.kaizoyu;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.adapters.modal.GenericModalBottomSheet;
import com.astarivi.kaizoyu.core.adapters.modal.ModalOption;
import com.astarivi.kaizoyu.core.adapters.tab.TabFragment;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.ActivityMainBinding;
import com.astarivi.kaizoyu.gui.NotificationModalBottomSheet;
import com.astarivi.kaizoyu.gui.UpdaterModalBottomSheet;
import com.astarivi.kaizoyu.gui.WelcomeModalBottomSheet;
import com.astarivi.kaizoyu.gui.adapters.TabAdapter;
import com.astarivi.kaizoyu.updater.UpdaterActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.io.IOException;


public class MainActivity extends AppCompatActivityTheme {
    private ActivityMainBinding binding;
    private TabLayout tabLayout;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.clearCache();

        binding = ActivityMainBinding.inflate(this.getLayoutInflater());

        setContentView(binding.getRoot());

        // Fullscreen
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
                binding.bottomTabs,
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    v.setPadding(0, 0, 0, insets.bottom);
                    return windowInsets;
                }
        );

        // Tabs
        configureTabAdapter();

        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt("index");
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (tab == null) return;
            tab.select();
        }

        ExtendedProperties appSettings = Data.getProperties(Data.CONFIGURATION.APP);

        boolean isFirstTime = appSettings.getBooleanProperty("first_time", true);

        if (isFirstTime) {
            WelcomeModalBottomSheet welcomeModalBottomSheet = new WelcomeModalBottomSheet(this::checkForGDPR);

            welcomeModalBottomSheet.show(getSupportFragmentManager(), WelcomeModalBottomSheet.TAG);

            appSettings.setBooleanProperty("first_time", false);
            appSettings.save();
            // This if is here to avoid showing both the modals at the same time
        } else if (appSettings.getBooleanProperty("autoupdate", true)) {
            Threading.instant(() -> {
                UpdateManager.AppUpdate latestUpdate;
                try {
                    latestUpdate = UpdateManager.getAppUpdate();
                } catch (IOException e) {
                    return;
                }

                String versionToSkip = Data.getProperties(Data.CONFIGURATION.APP)
                        .getProperty("skip_version", "false");

                if (latestUpdate == null || versionToSkip.equals(latestUpdate.getVersion())) return;

                binding.getRoot().post(() -> {
                    UpdaterModalBottomSheet modalBottomSheet = new UpdaterModalBottomSheet(latestUpdate, (result) -> {
                        if (result == UpdaterModalBottomSheet.Result.SKIP) return;

                        ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

                        if (result == UpdaterModalBottomSheet.Result.UPDATE_NOW) {
                            appProperties.setProperty("skip_version", "false");
                            Intent intent = new Intent();
                            intent.setClassName(BuildConfig.APPLICATION_ID, UpdaterActivity.class.getName());
                            intent.putExtra("latestUpdate", latestUpdate);
                            startActivity(intent);
                        }

                        if (result == UpdaterModalBottomSheet.Result.NEVER) {
                            appProperties.setProperty("skip_version", latestUpdate.getVersion());
                        }

                        appProperties.save();
                    });

                    modalBottomSheet.show(getSupportFragmentManager(), UpdaterModalBottomSheet.TAG);
                });
            });
        }

        if (!isFirstTime && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkForNotificationsPermission();
        }

        if (!isFirstTime) {
            checkForGDPR();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int i = tabLayout.getSelectedTabPosition();
        outState.putInt("index", i);
    }

    @Override
    protected void onPause() {
        super.onPause();

        PersistenceRepository.getInstance().saveSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int position = tabLayout.getSelectedTabPosition();

        if (position != 0) {
            tabLayout.selectTab(
                    tabLayout.getTabAt(0)
            );
            return;
        }

        super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkForNotificationsPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            NotificationModalBottomSheet notificationModalBottomSheet = new NotificationModalBottomSheet(() ->
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            );

            notificationModalBottomSheet.show(getSupportFragmentManager(), NotificationModalBottomSheet.TAG);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void checkForGDPR() {
        ExtendedProperties appSettings = Data.getProperties(Data.CONFIGURATION.APP);
        boolean hasAskedGdpr = appSettings.getBooleanProperty("has_asked_gdpr", false);

        if (hasAskedGdpr) return;

        GenericModalBottomSheet modalDialog = new GenericModalBottomSheet(
                getString(R.string.fw_ad_title),
                new ModalOption[]{
                        new ModalOption(
                                getString(R.string.fw_ad_allow),
                                getString(R.string.fw_ad_allow_desc),
                                true
                        ),
                        new ModalOption(
                                getString(R.string.fw_ad_deny),
                                getString(R.string.fw_ad_deny_desc)
                        )
                },
                (index, highlight) -> {
                    appSettings.setBooleanProperty("gdpr_consent", index == 0);
                    appSettings.setBooleanProperty("has_asked_gdpr", true);

                    StartAppSDK.setUserConsent (
                            this,
                            "pas",
                            System.currentTimeMillis(),
                            index == 0
                    );

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        checkForNotificationsPermission();
                    }
                }
        );

        modalDialog.show(getSupportFragmentManager(), GenericModalBottomSheet.TAG);
    }

    private void configureTabAdapter() {
        tabLayout = binding.bottomTabs;
        TabAdapter tabAdapter = new TabAdapter(this);
        ViewPager2 viewPager = binding.mainPager;
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(tabAdapter);

        final String[] tabTitles = new String[]{
                getString(R.string.home_button),
                getString(R.string.schedule_button),
                getString(R.string.history_button),
                getString(R.string.more_button)
        };

        final int[] tabIcons = new int[]{
                R.drawable.ic_main_home_alt,
                R.drawable.ic_main_emission_alt,
                R.drawable.ic_main_collection,
                R.drawable.ic_main_more_alt
        };

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int currentTabPosition = viewPager.getCurrentItem();
                int reselectedTabPosition = tab.getPosition();

                if (currentTabPosition != reselectedTabPosition) return;

                try {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + reselectedTabPosition);

                    if (fragment instanceof TabFragment) {
                        ((TabFragment) fragment).onTabReselected();
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }
}