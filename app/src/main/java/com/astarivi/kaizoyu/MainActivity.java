package com.astarivi.kaizoyu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.ActivityMainBinding;
import com.astarivi.kaizoyu.gui.UpdaterModalBottomSheet;
import com.astarivi.kaizoyu.gui.adapters.TabAdapter;
import com.astarivi.kaizoyu.updater.UpdaterActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;


public class MainActivity extends AppCompatActivityTheme {
    private ActivityMainBinding binding;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.clearCache();

        System.setProperty("tinylog.directory", getFilesDir().getAbsolutePath());

        binding = ActivityMainBinding.inflate(this.getLayoutInflater());

        setContentView(binding.getRoot());
        // Tabs
        configureTabAdapter();

        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt("index");
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (tab == null) return;
            tab.select();
        }

        Threading.submitTask(Threading.TASK.INSTANT, () -> {
            UpdateManager updateManager = new UpdateManager();

            UpdateManager.LatestUpdate latestUpdate;
            try {
                latestUpdate = updateManager.getLatestUpdate();
            } catch (ParseException e) {
                return;
            }

            String versionToSkip = Data.getProperties(Data.CONFIGURATION.APP)
                    .getProperty("skip_version", "false");

            if (latestUpdate == null || versionToSkip.equals(latestUpdate.version)) return;

            binding.getRoot().post(() -> {
                UpdaterModalBottomSheet modalBottomSheet = new UpdaterModalBottomSheet(latestUpdate, (result, update) -> {
                    if (result == UpdaterModalBottomSheet.Result.SKIP) return;

                    ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

                    if (result == UpdaterModalBottomSheet.Result.UPDATE_NOW) {
                        appProperties.setProperty("skip_version", "false");
                        Intent intent = new Intent(this, UpdaterActivity.class);
                        intent.putExtra("latestUpdate", update);
                        startActivity(intent);
                    }

                    if (result == UpdaterModalBottomSheet.Result.NEVER) {
                        appProperties.setProperty("skip_version", update.version);
                    }

                    appProperties.save();
                });

                modalBottomSheet.show(getSupportFragmentManager(), UpdaterModalBottomSheet.TAG);
            });
        });
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

    private void configureTabAdapter(){
        tabLayout = binding.bottomTabs;
        TabAdapter tabAdapter = new TabAdapter(this);
        ViewPager2 viewPager = binding.paginadorPrincipal;
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(tabAdapter);

        final String[] tabTitles = new String[]{
                getString(R.string.home_button),
                getString(R.string.schedule_button),
                getString(R.string.history_button),
                getString(R.string.settings_button)
        };

        final int[] tabIcons = new int[]{
                R.drawable.ic_main_home_alt,
                R.drawable.ic_main_emission_alt,
                R.drawable.ic_main_history_alt,
                R.drawable.ic_main_settings_alt
        };

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();
    }
}