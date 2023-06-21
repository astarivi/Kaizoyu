package com.astarivi.kaizoyu.gui.home.recycler;

import android.content.Context;

import androidx.annotation.StringRes;

import com.astarivi.kaizoyu.core.models.Anime;

import java.util.List;

public class MainCategoryContainer {
    private final @StringRes int title;
    private final List<Anime> anime;

    public MainCategoryContainer(@StringRes int title, List<Anime> anime) {
        this.title = title;
        this.anime = anime;
    }

    public @StringRes int getTitle() {
        return title;
    }

    public String getVerboseTitle(Context context) {
        return context.getString(title);
    }

    public List<Anime> getAnime() {
        return anime;
    }
}
