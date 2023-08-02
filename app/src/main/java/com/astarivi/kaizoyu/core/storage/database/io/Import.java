package com.astarivi.kaizoyu.core.storage.database.io;

import android.content.Context;

import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.utils.Data;

import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@ThreadedOnly
public class Import {
    private final Manager.Callback callback;

    public Import(Manager.Callback c) {
        callback = c;
    }

    public void importBackup(Context context, FileInputStream zippedFile) {
        AppDatabase database = PersistenceRepository.getInstance().getDatabase();

        database.close();

        File databaseDir = context.getDatabasePath("kaizo-database").getParentFile();

        // Delete old database

        if (databaseDir == null) {
            Logger.error("Database folder doesn't exist while importing");

            callback.onError(new IllegalStateException("Database folder doesn't exist"));
            return;
        }

        if (databaseDir.isDirectory()) {
            for (File databaseFile : Objects.requireNonNull(databaseDir.listFiles())) {
                if (!databaseFile.isDirectory()) {
                    if (!databaseFile.delete()) {
                        Logger.error("Failed to delete database file {}", databaseFile.getAbsolutePath());
                        callback.onError(new IllegalStateException("Failed to delete a database file"));
                        return;
                    }
                }
            }
        } else {
            if (!databaseDir.mkdirs()) {
                throw new IllegalStateException("Database folder doesn't exist and cannot be created");
            }
        }

        try (
                ZipInputStream zipInputStream = new ZipInputStream(zippedFile)
        ) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            int currentItem = 0;
            while (zipEntry != null) {
                String filename = zipEntry.getName();

                if (filename.equalsIgnoreCase("APP.properties")) {
                    ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);
                    appProperties.load(zipInputStream);
                    appProperties.save();
                } else {
                    File outputFile = new File(databaseDir, filename);

                    try (FileOutputStream databaseOutputFile = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int readLength;
                        while ((readLength = zipInputStream.read(buffer)) > 0) {
                            databaseOutputFile.write(buffer, 0, readLength);
                        }
                    }
                }

                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();

                int percentage = (int) ((++currentItem / 4F) * 100F);
                callback.onProgress(percentage);

                Logger.info("File {} has been recovered from a backup", filename);
            }
        } catch (IOException | IllegalStateException e) {
            callback.onError(e);
            Logger.error("Failed to import database backup");
            Logger.error(e);
            return;
        }

        callback.onFinished();
    }
}
