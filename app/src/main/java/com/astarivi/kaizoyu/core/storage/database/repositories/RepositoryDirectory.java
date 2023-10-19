package com.astarivi.kaizoyu.core.storage.database.repositories;

import com.astarivi.kaizoyu.core.storage.database.AppDatabase;

import lombok.Getter;


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

    public AnimeStorageRepository getAnimeStorageRepository() {
        return animeStorageRepository;
    }

    public SearchHistoryRepository getSearchHistoryRepository() {
        return searchHistoryRepository;
    }

    public SeenAnimeRepository getSeenAnimeRepository() {
        return seenAnimeRepository;
    }
}
