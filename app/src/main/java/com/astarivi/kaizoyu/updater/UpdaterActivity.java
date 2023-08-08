package com.astarivi.kaizoyu.updater;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.HttpFileDownloader;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.ActivityUpdaterBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.zparc.Zparc;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;


public class UpdaterActivity extends AppCompatActivityTheme {
    private ActivityUpdaterBinding binding;
    private HttpFileDownloader updateDownloader;
    private Future<?> downloadingFuture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUpdaterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Zparc spark = new Zparc.Builder(this)
                .setView(binding.getRoot())
                .setDuration(4000)
                .setAnimList(Zparc.ANIM_BLUE_PURPLE)
                .build();

        spark.startAnimation();

        Bundle bundle = getIntent().getExtras();

        if (bundle == null) return;

        UpdateManager.LatestUpdate latestUpdate = getUpdateFromBundle(getIntent().getExtras());

        if (latestUpdate == null) return;

        updateDownloader = new HttpFileDownloader(
                latestUpdate.downloadUrl,
                new File(getCacheDir(), "update.apk")
        );

        updateDownloader.setListener(progressPercentage ->
                binding.getRoot().post(() -> binding.downloadProgress.setProgress(progressPercentage))
        );

        downloadingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            File updateFile;
            try {
                updateFile = updateDownloader.download();
            } catch (IOException e) {
                Logger.error("Error downloading update");
                Logger.error(e);
                binding.getRoot().post(() -> {
                    Toast.makeText(this, R.string.parsing_error, Toast.LENGTH_SHORT).show();
                    cancelUpdate();
                });
                return;
            } catch (NetworkConnectionException e) {
                Logger.error("Error downloading update");
                Logger.error(e);
                binding.getRoot().post(() -> {
                    Toast.makeText(this, R.string.network_connection_error, Toast.LENGTH_SHORT).show();
                    cancelUpdate();
                });
                return;
            }

            binding.getRoot().post(() ->
                installUpdate(updateFile)
            );
        });
    }

    private void installUpdate(File updateFile) {
        // It better work. I don't want to use that dumb PackageInstaller thing
        startActivity(
                new Intent(Intent.ACTION_VIEW)
                        .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .setData(
                                FileProvider.getUriForFile(
                                        this,
                                        getString(R.string.provider_authority),
                                        updateFile
                                )
                        )
        );
    }

    @SuppressWarnings("deprecation")
    public static @Nullable UpdateManager.LatestUpdate getUpdateFromBundle(@NotNull Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable("latestUpdate", UpdateManager.LatestUpdate.class);
        }
        return bundle.getParcelable("latestUpdate");
    }

    private void cancelUpdate() {
        AnalyticsClient.logBreadcrumb("canceled_update");
        Toast.makeText(
                this,
                getString(R.string.update_error),
                Toast.LENGTH_SHORT
        ).show(
        );
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            try {
                this.finish();
            } catch (Exception ignored) {
            }
        }, 5000);
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.update_cancel_title))
                .setMessage(getString(R.string.update_cancel_description))
                .setPositiveButton(getString(R.string.update_cancel_accept), (dialog, which) -> {
                    downloadingFuture.cancel(true);
                    AnalyticsClient.logBreadcrumb("canceled_update_willingly");
                    finish();
                }).setNegativeButton(getString(R.string.update_cancel_deny), null).show();
    }
}
