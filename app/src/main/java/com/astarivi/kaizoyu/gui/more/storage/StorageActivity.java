package com.astarivi.kaizoyu.gui.more.storage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.storage.database.io.Manager;
import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivityStorageBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileNotFoundException;


public class StorageActivity extends AppCompatActivityTheme {
    private ActivityStorageBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStorageBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.backupTrigger.setOnClickListener(v ->
            startActivityForResult(
                    new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("application/zip")
                            .putExtra(Intent.EXTRA_TITLE, "KaizoyuBackup.zip"),
                    1
            )
        );

        binding.restoreTrigger.setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.restore_dialog)
                    .setMessage(R.string.restore_dialog_description)
                    .setNegativeButton(R.string.restore_negative, null)
                    .setPositiveButton(R.string.restore_positive, (dialog, which) -> {

                    })
                    .show()
        );
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
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
