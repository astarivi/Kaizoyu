package com.astarivi.kaizoyu.gui.home.recycler.recommendations;

import android.content.Context;

import androidx.annotation.StringRes;

import com.astarivi.kaizoyu.core.models.anime.RemoteAnime;

import java.util.List;

import lombok.Getter;


public class MainCategoryContainer {
    private final @StringRes int title;
    @Getter
    private final List<RemoteAnime> anime;

    public MainCategoryContainer(@StringRes int title, List<RemoteAnime> anime) {
        this.title = title;
        this.anime = anime;
    }

    public @StringRes int getTitle() {
        return title;
    }

    public String getVerboseTitle(Context context) {
        return context.getString(title);
    }
}
