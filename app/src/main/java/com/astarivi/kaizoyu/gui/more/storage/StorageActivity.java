package com.astarivi.kaizoyu.gui.more.storage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.gui.WindowCompatUtils;
import com.astarivi.kaizoyu.core.storage.database.io.Manager;
import com.astarivi.kaizoyu.core.storage.database.repo.SearchHistoryRepo;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.core.theme.Colors;
import com.astarivi.kaizoyu.databinding.ActivityStorageBinding;
import com.astarivi.kaizoyu.utils.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class StorageActivity extends AppCompatActivityTheme {
    private ActivityStorageBinding binding;
    private final Manager.FacadeCallback callback = new Manager.FacadeCallback() {
        @Override
        public void onStatusChange(Manager.State currentState) {
            if (binding == null) return;

            binding.getRoot().post(() -> {
                switch (currentState) {
                    case IMPORT:
                        binding.currentOperationContainer.setVisibility(View.VISIBLE);
                        binding.currentTask.setText(R.string.st_db_import_operation);
                        break;
                    case EXPORT:
                        binding.currentOperationContainer.setVisibility(View.VISIBLE);
                        binding.currentTask.setText(R.string.st_db_backup_operation);
                        break;
                    case IDLE:
                    default:
                        binding.currentOperationContainer.setVisibility(View.GONE);
                        binding.loadingBar.setProgressCompat(0, false);
                        binding.loadingPercentage.setText("0%");
                        break;
                }
            });
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onProgress(int percentage) {
            if (binding == null) return;

            binding.getRoot().post(() -> {
                binding.loadingBar.setProgressCompat(percentage, true);
                binding.loadingPercentage.setText(percentage + "%");
            });
        }

        @Override
        public void onFinished(Manager.State lastState) {
            Utils.makeToastRegardless(StorageActivity.this, R.string.st_db_operation_done, Toast.LENGTH_SHORT);
        }

        @Override
        public void onError(Exception e) {
            Utils.makeToastRegardless(StorageActivity.this, e.getMessage(), Toast.LENGTH_LONG);
            Manager.cancelOperation();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStorageBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.internalToolbar.setNavigationOnClickListener(v -> finish());

        binding.storageMainContainer.getLayoutTransition().setAnimateParentHierarchy(false);

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
                binding.storageScrollContainer,
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

        binding.loadingBar.setProgressCompat(0, false);
        binding.currentOperationContainer.setVisibility(View.GONE);

        binding.clearCacheTrigger.setOnClickListener(view -> {
            Utils.clearCache();
            Toast.makeText(this, getString(R.string.cache_toast), Toast.LENGTH_SHORT).show();
        });

        binding.clearSearchTrigger.setOnClickListener(v -> {
            SearchHistoryRepo.deleteAllAsync();
            Toast.makeText(this, getString(R.string.history_toast), Toast.LENGTH_SHORT).show();
        });

        binding.cancelOperation.setOnClickListener(v ->
            Manager.cancelOperation()
        );

        binding.backupTrigger.setOnClickListener(v ->
            startActivityForResult(
                    new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("application/zip")
                            .putExtra(Intent.EXTRA_TITLE, String.format(
                                    "KaizoyuBackup-%s.zip",
                                    new SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(new Date()))
                            ),
                    1
            )
        );

        binding.restoreTrigger.setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.restore_dialog)
                    .setMessage(R.string.restore_dialog_description)
                    .setNegativeButton(R.string.restore_negative, null)
                    .setPositiveButton(R.string.restore_positive, (dialog, which) ->
                        startActivityForResult(
                                new Intent(Intent.ACTION_OPEN_DOCUMENT)
                                        .addCategory(Intent.CATEGORY_OPENABLE)
                                        .setType("application/zip"),
                                2
                        )
                    )
                    .show()
        );

        Manager.subscribe(callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null || data.getData() == null) return;

        if (requestCode == 1) {
            try {
                Manager.doExportDatabase(
                        this,
                        getContentResolver().openOutputStream(
                                data.getData()
                        )
                );
            } catch (FileNotFoundException e) {
                Toast.makeText(this, R.string.backup_io_failure, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 2) {
            try {
                Manager.doImportDatabase(
                        this,
                        getContentResolver().openInputStream(
                                data.getData()
                        )
                );
            } catch (FileNotFoundException e) {
                Toast.makeText(this, R.string.backup_io_failure, Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
