package com.astarivi.kaizoyu.updater;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.updater.UpdateDownloader;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.databinding.ActivityUpdaterBinding;
import com.astarivi.kaizoyu.utils.Threading;
import com.astarivi.zparc.Zparc;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.Future;


public class UpdaterActivity extends AppCompatActivityTheme {
    private ActivityUpdaterBinding binding;
    private UpdateDownloader updateDownloader;
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

        updateDownloader = new UpdateDownloader(
                latestUpdate,
                new File(getCacheDir(), "update.apk"),
                new UpdateDownloader.DownloadStatusListener() {
                    @Override
                    public void onProgress(int percentage) {
                        binding.getRoot().post(() -> binding.downloadProgress.setProgress(percentage));
                    }

                    // TODO Handle this better
                    @Override
                    public void onError(UpdateDownloader.DownloadErrorCodes code) {
                        binding.getRoot().post(() -> cancelUpdate());
                    }

                    @Override
                    public void onFinish(File file) {
                        binding.getRoot().post(() -> installUpdate(file));
                    }
                }
        );

        downloadingFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> updateDownloader.download());
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
                                        "com.astarivi.kaizoyu.fileprovider",
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
        AnalyticsClient.logEvent("canceled_update");
        Toast.makeText(
                this,
                getString(R.string.update_error),
                Toast.LENGTH_SHORT
        ).show(
        );
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::finish, 3000);
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.update_cancel_title))
                .setMessage(getString(R.string.update_cancel_description))
                .setPositiveButton(getString(R.string.update_cancel_accept), (dialog, which) -> {
                    downloadingFuture.cancel(true);
                    AnalyticsClient.logEvent("canceled_update_willingly");
                    finish();
                }).setNegativeButton(getString(R.string.update_cancel_deny), null).show();
    }
}
