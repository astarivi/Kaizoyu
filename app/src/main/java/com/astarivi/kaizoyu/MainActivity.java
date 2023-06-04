package com.astarivi.kaizoyu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.astarivi.kaizoyu.core.storage.DataAssistant;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.ActivityMainBinding;
import com.astarivi.kaizoyu.gui.UpdaterModalBottomSheet;
import com.astarivi.kaizoyu.gui.adapters.TabAdapter;
import com.astarivi.kaizoyu.updater.UpdaterActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Properties;


public class MainActivity extends AppCompatActivityTheme {
    public static WeakReference<MainActivity> weakActivity;
    private DataAssistant dataAssistant;
    private ActivityMainBinding binding;
    private TabLayout tabLayout;

    public DataAssistant getDataAssistant() {
        return dataAssistant;
    }

    public static MainActivity getInstance() {
        return weakActivity.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Do this before initialization to have DataAssistant ready before starting the View.
        weakActivity = new WeakReference<>(MainActivity.this);
        dataAssistant = new DataAssistant(this, weakActivity);

        super.onCreate(savedInstanceState);

        dataAssistant.clearCache();
        dataAssistant.initializeSettings();

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

            Properties properties = Data.getProperties(Data.CONFIGURATION.APP);
            String versionToSkip = properties.getProperty("skip_version", "false");

            if (latestUpdate == null || versionToSkip.equals(latestUpdate.version)) return;

            binding.getRoot().post(() -> {
                UpdaterModalBottomSheet modalBottomSheet = new UpdaterModalBottomSheet(latestUpdate, (result, update) -> {
                    if (result == UpdaterModalBottomSheet.Result.SKIP) return;

                    Properties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

                    if (result == UpdaterModalBottomSheet.Result.UPDATE_NOW) {
                        appProperties.setProperty("skip_version", "false");
                        Intent intent = new Intent(this, UpdaterActivity.class);
                        intent.putExtra("latestUpdate", update);
                        startActivity(intent);
                    }

                    if (result == UpdaterModalBottomSheet.Result.NEVER) {
                        appProperties.setProperty("skip_version", update.version);
                    }

                    Data.saveProperties(Data.CONFIGURATION.APP);
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

        this.dataAssistant.save();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.dataAssistant.close();
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