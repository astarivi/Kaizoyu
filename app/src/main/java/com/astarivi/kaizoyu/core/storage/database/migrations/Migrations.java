package com.astarivi.kaizoyu.core.storage.database.migrations;

import androidx.annotation.NonNull;
import androidx.room.DeleteTable;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE favorite_anime ADD COLUMN type INTEGER NOT NULL DEFAULT 1");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE seen_episode ADD COLUMN notified INTEGER NOT NULL DEFAULT 0");
        }
    };

    @DeleteTable.Entries({
            @DeleteTable(tableName = "favorite_anime"),
            @DeleteTable(tableName = "seen_episode"),
            @DeleteTable(tableName = "seen_anime")
    })
    public static class AUTO_MIGRATION_3_4 implements AutoMigrationSpec {
    }
}
