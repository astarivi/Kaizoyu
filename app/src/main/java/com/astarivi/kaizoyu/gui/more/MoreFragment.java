package com.astarivi.kaizoyu.gui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.adapters.tab.TabFragment;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.FragmentMoreBinding;
import com.astarivi.kaizoyu.gui.UpdaterModalBottomSheet;
import com.astarivi.kaizoyu.gui.more.settings.SettingsActivity;
import com.astarivi.kaizoyu.gui.more.storage.StorageActivity;
import com.astarivi.kaizoyu.updater.UpdaterActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.io.IOException;


public class MoreFragment extends TabFragment {
    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WindowCompatUtils.setOnApplyWindowInsetsListener(
                binding.getRoot(),
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    if (getContext() == null) return windowInsets;

                    v.setPadding(
                            0,
                            insets.top + (int) Utils.convertDpToPixel(8, requireContext()),
                            0,
                            insets.bottom + (int) Utils.convertDpToPixel(8, requireContext())
                    );

                    return windowInsets;
                }
        );

        binding.updateSettingDescription.setText(
                String.format(getString(R.string.updatecheck_description), UpdateManager.VERSION)
        );

        binding.githubButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/astarivi/Kaizoyu")))
        );

        binding.discordButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/8YvvxbfqSG")))
        );

        binding.openSettings.setOnClickListener(v -> {
            if (getActivity() == null) return;

            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, SettingsActivity.class.getName());
            startActivity(intent);
        });

        binding.openLicensesActivity.setOnClickListener(v -> {
            if (getActivity() == null) return;

            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, OssLicensesMenuActivity.class.getName());
            startActivity(intent);
        });

        binding.openDataSettings.setOnClickListener(v -> {
            if (getActivity() == null) return;

            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, StorageActivity.class.getName());
            startActivity(intent);
        });

        binding.checkForUpdate.setOnClickListener(v ->
                Threading.submitTask(Threading.TASK.INSTANT, () -> {
                    UpdateManager.AppUpdate latestUpdate;
                    try {
                        latestUpdate = UpdateManager.getAppUpdate();
                    } catch (IOException e) {
                        return;
                    }

                    if (latestUpdate == null) {
                        binding.getRoot().post(() -> Toast.makeText(getContext(), getString(R.string.update_already_latest), Toast.LENGTH_SHORT).show());
                        return;
                    }

                    binding.getRoot().post(() -> {
                        UpdaterModalBottomSheet modalBottomSheet = new UpdaterModalBottomSheet(latestUpdate, (result) -> {
                            if (result == UpdaterModalBottomSheet.Result.SKIP) return;

                            ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

                            if (result == UpdaterModalBottomSheet.Result.UPDATE_NOW) {
                                appProperties.setBooleanProperty("skip_version", false);
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

                        modalBottomSheet.show(getParentFragmentManager(), UpdaterModalBottomSheet.TAG);
                    });
                })
        );
    }

    @Override
    public void onTabReselected() {

    }
}
