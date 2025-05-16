package com.astarivi.kaizoyu.core.storage.ids;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.core.adapters.HttpFileDownloader;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.utils.Data;

import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;


public class AnimeIdsDatabase {
    private static final String REMOTE_URL = "https://raw.githubusercontent.com/astar-workspace/k.delivery/refs/heads/main/dist/anime_ids.sqlite3";
    private static final File databaseFile = new File(KaizoyuApplication.getContext().getFilesDir(), "config/anime_ids.sqlite3");
    ;
    private static boolean isDownloading = false;

    public static boolean isDatabaseAvailable() {
        return databaseFile.exists() && !isDownloading;
    }

    @ThreadedOnly
    public static void tryUpdate() {
        String remoteVersion;
        try {
            remoteVersion = UpdateManager.databaseUpdateAvailable();
        } catch (IOException e) {
            Logger.info("Failed to fetch ids database, {}", e);
            return;
        }

        if (remoteVersion != null) tryFetchRemote(remoteVersion);
    }

    @ThreadedOnly
    public static void tryFetchRemote(String remoteVersion) {
        if (isDownloading) return;
        isDownloading = true;

        if (databaseFile.exists()) {
            if (!databaseFile.delete()) {
                Logger.error("Couldn't delete database file. Is it open?");
                isDownloading = false;
                return;
            }
        }

        HttpFileDownloader downloader = new HttpFileDownloader(
                REMOTE_URL,
                databaseFile
        );

        try {
            downloader.download();
        } catch (Exception e) {
            Logger.error("Failed to download remote ids database");
            Logger.error(e);
        } finally {
            isDownloading = false;
        }

        ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);
        config.setProperty("idsVersion", remoteVersion);
    }

    @Nullable
    public static AnimeIds getByKitsuId(long kitsuId) {
        return retrieveRow(
                "SELECT kitsu_id, anilist_id, mal_id FROM anime_ids WHERE kitsu_id = ?",
                String.valueOf(kitsuId)
        );
    }

    @Nullable
    public static AnimeIds getByAniListId(long aniListId) {
        return retrieveRow(
                "SELECT kitsu_id, anilist_id, mal_id FROM anime_ids WHERE anilist_id = ?",
                String.valueOf(aniListId)
        );
    }

    @Nullable
    public static AnimeIds getByMalId(long malId) {
        return retrieveRow(
                "SELECT kitsu_id, anilist_id, mal_id FROM anime_ids WHERE mal_id = ?",
                String.valueOf(malId)
        );
    }

    @Nullable
    private static AnimeIds retrieveRow(String query, String argument) {
        if (!isDatabaseAvailable()) return null;

        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(databaseFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
             Cursor cursor = db.rawQuery(query, new String[]{argument})) {

            if (cursor.moveToFirst()) {
                return new AnimeIds(
                        cursor.getLong(0),
                        cursor.isNull(1) ? null : cursor.getLong(1),
                        cursor.isNull(2) ? null : cursor.getLong(2)
                );
            }
        } catch (SQLException e) {
            ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);
            // Marked as corrupt
            config.remove("idsVersion");
        }

        return null;
    }
}
