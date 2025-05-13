package com.astarivi.kaizoyu.core.storage.database.repo;

import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.storage.PersistenceRepository;
import com.astarivi.kaizoyu.core.storage.database.tables.search_history.SearchHistory;
import com.astarivi.kaizoyu.core.storage.database.tables.search_history.SearchHistoryDao;
import com.astarivi.kaizoyu.utils.Threading;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchHistoryRepo {
    private final static SearchHistoryDao searchDao = PersistenceRepository.getInstance().getDatabase().searchHistoryDao();

    @ThreadedOnly
    public static List<SearchHistory> getAll() {
        return searchDao.getAll();
    }

    public static void bumpUpAsync(@NotNull SearchHistory searchHistory) {
        if (searchHistory.id == 0) return;

        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            searchHistory.date = System.currentTimeMillis();
            searchDao.update(searchHistory);
        });
    }

    public static void saveAsync(String searchTerm) {
        Threading.submitTask(Threading.TASK.DATABASE, () -> {
            SearchHistory currentSearch = new SearchHistory(
                    searchTerm,
                    System.currentTimeMillis()
            );

            List<SearchHistory> currentList = searchDao.getAll();

            // If we're saving a term that already exists, bump it up instead.
            for (SearchHistory searchHistory : currentList) {
                if (searchHistory.searchTerm.equalsIgnoreCase(searchTerm)) {
                    searchHistory.date = System.currentTimeMillis();
                    searchDao.update(searchHistory);
                    return;
                }
            }

            if (currentList.isEmpty() || currentList.size() < 6) {
                searchDao.insert(
                        currentSearch
                );
                currentList.clear();
                return;
            }

            currentSearch.id = currentList.get(currentList.size() - 1).id;

            searchDao.update(currentSearch);
            currentList.clear();
        });
    }

    public static void deleteAllAsync() {
        Threading.submitTask(Threading.TASK.DATABASE, searchDao::deleteAll);
    }
}
