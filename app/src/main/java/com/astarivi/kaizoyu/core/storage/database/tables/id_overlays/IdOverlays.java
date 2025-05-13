package com.astarivi.kaizoyu.core.storage.database.tables.id_overlays;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(
        tableName = "id_overlays",
        indices = {
                @Index(
                        value = "kitsuId",
                        unique = true
                ),
                @Index(
                        value = "malId",
                        unique = true
                ),
                @Index(
                        value = "aniId",
                        unique = true
                )
        }
)
public class IdOverlays {
    @PrimaryKey(autoGenerate = true) public int id = 0;
    public long kitsuId;
    public long malId = 0;
    public long aniId = 0;

    public IdOverlays(long kitsuId, long malId, long anilistId) {
        this.kitsuId = kitsuId;
        this.malId = malId;
        this.aniId = anilistId;
    }
}
