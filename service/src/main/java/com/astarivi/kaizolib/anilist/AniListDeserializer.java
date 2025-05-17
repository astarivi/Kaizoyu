package com.astarivi.kaizolib.anilist;

import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.anilist.model.AniListAnime;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.util.List;


class AniListDeserializer {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ResponseMany {
        public DataMany data;

        private static class DataMany {
            public Page Page;
        }

        private static class Page {
            public PageInfo pageInfo;
            public List<AniListAnime> media;
        }

        private static class PageInfo {
            public boolean hasNextPage;
        }
    }

    protected static Deserialized deserializeMany(String serialized) throws ParsingError {
        ResponseMany response;
        try {
            response = JsonMapper.deserializeGeneric(serialized, ResponseMany.class);
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }

        return new Deserialized(
                response.data.Page.pageInfo.hasNextPage,
                response.data.Page.media
        );
    }

    protected record Deserialized(boolean hasNext, List<AniListAnime> items) {
    }
}
