package com.astarivi.kaizoyu.gui.home.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.astarivi.kaizolib.kitsuv2.public_api.SearchParams;
import com.astarivi.kaizoyu.R;

import lombok.Getter;


public enum Categories {
    POPULAR(
            R.string.home_popular,
            new SearchParams().
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    ).
                    setCustomParameter(
                            "filter[subtype]",
                            "TV"
                    )
    ),
    LEGENDS(
            R.string.home_beloved,
            new SearchParams().
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    ).
                    setCustomParameter(
                            "sort",
                            "-favoritesCount"
                    ).
                    setCustomParameter(
                            "filter[subtype]",
                            "TV"
                    )
    ),
    AIRING(
            R.string.home_airing,
            new SearchParams().
                    setCustomParameter(
                            "filter[subtype]",
                            "TV"
                    ).
                    setCustomParameter(
                            "filter[status]",
                            "current"
                    ).
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    )
    ),
    UPCOMING(
            R.string.home_upcoming,
            new SearchParams().
                    setCustomParameter(
                            "filter[subtype]",
                            "TV"
                    ).
                    setCustomParameter(
                            "filter[status]",
                            "upcoming"
                    ).
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    )
    ),
    SEINEN(
            R.string.home_seinen,
            new SearchParams().
                    setCustomParameter(
                            "filter[categories]",
                            "seinen"
                    ).
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    ).
                    setCustomParameter(
                            "sort",
                            "-favoritesCount"
                    )
    ),
    SHONEN(
            R.string.home_shonen,
            new SearchParams().
                    setCustomParameter(
                            "filter[categories]",
                            "shounen"
                    ).
                    setCustomParameter(
                            "filter[subtype]",
                            "TV"
                    ).
                    setCustomParameter(
                            "filter[status]",
                            "finished"
                    ).
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    )
    ),
    SHOJO(
            R.string.home_shojo,
            new SearchParams().
                    setCustomParameter(
                            "filter[categories]",
                            "shoujo"
                    ).
                    setCustomParameter(
                            "filter[subtype]",
                            "TV"
                    ).
                    setCustomParameter(
                            "filter[status]",
                            "finished"
                    ).
                    setCustomParameter(
                            "sort",
                            "popularityRank"
                    )
    );


    @Getter
    private final SearchParams search;
    private final @StringRes int title;

    Categories(@StringRes int title, SearchParams search) {
        this.title = title;
        this.search = search;
    }

    @NonNull
    public String getTitle(@NonNull Context context) {
        return context.getResources().getString(title);
    }
}
