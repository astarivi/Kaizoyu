package com.astarivi.kaizoyu.core.storage.database.repositories;

import com.astarivi.kaizoyu.core.storage.database.AppDatabase;
import com.astarivi.kaizoyu.core.storage.database.data.search.SearchHistory;
import com.astarivi.kaizoyu.core.storage.database.data.search.SearchHistoryDao;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import lombok.Getter;


public class SearchHistoryRepository {
    @Getter
    private final SearchHistoryDao searchHistoryDao;

    public SearchHistoryRepository(@NotNull AppDatabase database) {
        searchHistoryDao = database.searchHistoryDao();
    }

    public List<SearchHistory> getAll() {
        return searchHistoryDao.getAll();
    }

    public void bumpUpAsync(@NotNull SearchHistory searchHistory) {
        if (searchHistory.id == 0) return;

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            searchHistory.date = System.currentTimeMillis();
            searchHistoryDao.update(searchHistory);
        });
    }

    public void saveAsync(String searchTerm) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            SearchHistory currentSearch = new SearchHistory(
                    searchTerm,
                    System.currentTimeMillis()
            );

            List<SearchHistory> currentList = searchHistoryDao.getAll();

            // If we're saving a term that already exists, bump it up instead.
            for (SearchHistory searchHistory : currentList) {
                if (searchHistory.searchTerm.equalsIgnoreCase(searchTerm)) {
                    searchHistory.date = System.currentTimeMillis();
                    searchHistoryDao.update(searchHistory);
                    return;
                }
            }

            if (currentList.isEmpty() || currentList.size() < 6) {
                searchHistoryDao.insert(
                        currentSearch
                );
                currentList.clear();
                return;
            }

            currentSearch.id = currentList.get(currentList.size() - 1).id;

            searchHistoryDao.update(currentSearch);
            currentList.clear();
        });
    }

    public void deleteAllAsync() {
        Threading.submitTask(Threading.TASK.DATABASE, searchHistoryDao::deleteAll);
    }
}
