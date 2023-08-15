package com.astarivi.kaizoyu.core.models.local;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.kitsu.model.KitsuAnime;
import com.astarivi.kaizoyu.core.models.Anime;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.storage.database.data.favorite.FavoriteAnimeWithSeenAnime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;


@Getter
public class LocalAnime extends Anime {
    protected final int databaseId;
    protected final Date watchDate;
    protected final ModelType.LocalAnime localAnimeType;

    public LocalAnime(KitsuAnime anime, int databaseId, Date watchDate, ModelType.LocalAnime type) {
        super(anime);
        this.databaseId = databaseId;
        this.watchDate = watchDate;
        this.localAnimeType = type;
    }

    // region Parcelable implementation

    protected LocalAnime(Parcel parcel) {
        super(parcel);
        databaseId = parcel.readInt();
        watchDate = new Date(parcel.readLong());
        localAnimeType = ModelType.LocalAnime.valueOf(parcel.readString());
    }

    public static final Creator<LocalAnime> CREATOR = new Creator<LocalAnime>() {
        @Override
        public LocalAnime createFromParcel(Parcel parcel) {
            return new LocalAnime(parcel);
        }

        @Override
        public LocalAnime[] newArray(int size) {
            return new LocalAnime[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(databaseId);
        dest.writeLong(watchDate.getTime());
        dest.writeString(localAnimeType.name());
    }

    // endregion

    // region Builders

    public static class BulkFavoriteLocalAnimeBuilder {
        private final List<FavoriteAnimeWithSeenAnime> anime;

        public BulkFavoriteLocalAnimeBuilder(List<FavoriteAnimeWithSeenAnime> anime) {
            this.anime = anime;
        }

        public BulkFavoriteLocalAnimeBuilder addAnime(FavoriteAnimeWithSeenAnime anime) {
            this.anime.add(anime);
            return this;
        }

        public ArrayList<LocalAnime> build() {
            ArrayList<LocalAnime> result = new ArrayList<>();

            for (FavoriteAnimeWithSeenAnime favRelation : this.anime) {
                result.add(
                        favRelation.toLocalAnime()
                );
            }

            return result;
        }
    }

    // endregion
}
