package com.astarivi.kaizoyu.details.gui.recycler;

import com.astarivi.kaizoyu.core.models.Episode;

import org.jetbrains.annotations.Nullable;


public interface SharedEpisodeItemClickListener {
    void onItemClick(@Nullable Episode episode);
}
