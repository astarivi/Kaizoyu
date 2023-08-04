package com.astarivi.kaizoyu.core.storage.database.io;

import android.content.Context;

import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@ThreadedOnly
public class Export {
    private final Manager.Callback callback;

    public Export(Manager.Callback c) {
        callback = c;
    }

    public void exportBackup(Context context, OutputStream output) {
        AppDatabase database = PersistenceRepository.getInstance().getDatabase();

        database.close();

        File[] databasePaths = new File[] {
                context.getDatabasePath("kaizo-database"),
                context.getDatabasePath("kaizo-database-shm"),
                context.getDatabasePath("kaizo-database-wal")
        };

        int currentItem = 0;
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(output)) {
            for (File databasePath : databasePaths) {
                if (!databasePath.exists()) {
                    Logger.error("Missing database files while exporting, {}", databasePath);
                    callback.onError(new IllegalStateException("Missing database files"));
                    return;
                }

                try (FileInputStream databaseStream = new FileInputStream(databasePath)) {
                    byte[] buffer = new byte[1024];
                    zipOutputStream.putNextEntry(new ZipEntry(databasePath.getName()));
                    int length;
                    while ((length = databaseStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0 , length);
                    }

                    zipOutputStream.closeEntry();
                }

                int percentage = (int) ((++currentItem / 4F) * 100F);
                callback.onProgress(percentage);

                Logger.info("File {} has been compressed into the backup file", databasePath.getName());
            }

            if (Data.getProperties(Data.CONFIGURATION.APP).size() > 0) {
                try (
                        PipedInputStream pipeInput = new PipedInputStream();
                        PipedOutputStream pipeOutput = new PipedOutputStream(pipeInput)
                ) {
                    // Avoid race condition deadlock... or something like that, idk.
                    Threading.submitTask(Threading.TASK.INSTANT, () -> {
                        try {
                            Data.getProperties(Data.CONFIGURATION.APP).store(pipeOutput, "APP_CONFIG");
                            pipeOutput.close();
                        } catch (IOException e) {
                            Logger.error("Couldn't write to PipedOutput to save");
                            Logger.error(e);
                            throw new RuntimeException(e);
                        }
                    });

                    zipOutputStream.putNextEntry(new ZipEntry("APP.properties"));
                    byte[] buffer = new byte[1024];

                    int length;
                    while ((length = pipeInput.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0 , length);
                    }

                    zipOutputStream.closeEntry();
                }
            }

            callback.onProgress(100);
        } catch (IOException e) {
            callback.onError(e);
            Logger.error("Failed to backup database");
            Logger.error(e);
            return;
        }

        callback.onFinished();
    }
}
