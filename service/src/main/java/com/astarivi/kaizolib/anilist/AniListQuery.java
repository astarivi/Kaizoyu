package com.astarivi.kaizolib.anilist;

import com.astarivi.kaizolib.anilist.exception.AniListException;
import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;

import java.io.IOException;
import java.util.List;


public class AniListQuery extends AniListCommon {
    public static class Single {
        private final AniListCommon.GraphQLRequest request;

        protected Single(AniListCommon.GraphQLRequest request) {
            this.request = request;
        }

        public AniListAnime get() throws AniListException, IOException {
            String response = HttpMethodsV2.executeRequest(
                    getRequestFor(request)
            );

            return AniListAnime.deserializeOne(response);
        }
    }

    public static class Paged {
        private final AniListCommon.PagedGraphQLRequest request;
        private boolean hasNext = true;
        private long page = 0;

        protected Paged(AniListCommon.PagedGraphQLRequest request) {
            this.request = request;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public List<AniListAnime> current() throws AniListException, IOException {
            return execute();
        }

        public List<AniListAnime> next() throws AniListException, IOException {
            if (!hasNext) throw new IndexOutOfBoundsException("No next page for this AniListQuery");

            request.setPage(++page);
            return execute();
        }

        private List<AniListAnime> execute() throws AniListException, IOException {
            String response = HttpMethodsV2.executeRequest(
                    getRequestFor(request)
            );

            AniListDeserializer.Deserialized res = AniListDeserializer.deserializeMany(response);
            hasNext = res.hasNext();
            return res.items();
        }
    }
}
