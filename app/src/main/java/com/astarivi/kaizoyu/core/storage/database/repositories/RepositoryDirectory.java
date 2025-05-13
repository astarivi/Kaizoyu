package com.astarivi.kaizoyu.core.storage.database.repositories;

import com.astarivi.kaizoyu.core.storage.database.AppDatabase;

import lombok.Getter;


@Deprecated
@Getter
public class RepositoryDirectory {
    private final AnimeStorageRepository animeStorageRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final SeenAnimeRepository seenAnimeRepository;

    public RepositoryDirectory(AppDatabase database) {
        animeStorageRepository = new AnimeStorageRepository(database);
        searchHistoryRepository = new SearchHistoryRepository(database);
        seenAnimeRepository = new SeenAnimeRepository(database);
    }
}
