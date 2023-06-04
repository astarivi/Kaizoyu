package com.astarivi.kaizoyu.core.storage.database.repositories;

import com.astarivi.kaizoyu.core.storage.database.AppDatabase;


public class RepositoryDirectory {
    private final FavoriteAnimeRepository favoriteAnimeRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final SeenAnimeRepository seenAnimeRepository;

    public RepositoryDirectory(AppDatabase database) {
        favoriteAnimeRepository = new FavoriteAnimeRepository(database);
        searchHistoryRepository = new SearchHistoryRepository(database);
        seenAnimeRepository = new SeenAnimeRepository(database);
    }

    public FavoriteAnimeRepository getFavoriteAnimeRepository() {
        return favoriteAnimeRepository;
    }

    public SearchHistoryRepository getSearchHistoryRepository() {
        return searchHistoryRepository;
    }

    public SeenAnimeRepository getSeenAnimeRepository() {
        return seenAnimeRepository;
    }
}
