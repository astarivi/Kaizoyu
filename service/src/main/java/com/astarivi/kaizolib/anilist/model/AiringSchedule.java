package com.astarivi.kaizolib.anilist.model;

import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.common.util.JsonMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AiringSchedule {
    public ArrayList<Episode> episodes = new ArrayList<>();

    public boolean deserialize(String serialized) throws ParsingError {
        try {
            Result result = JsonMapper.deserializeGeneric(serialized, Result.class);

            episodes.addAll(result.data.Page.airingSchedules);

            return result.data.Page.pageInfo.hasNextPage;
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Episode {
        public Integer episode;
        public Long airingAt;
        public AniListAnime media;

        private static class DetachedData {
            public DetachedSchedule data;
        }

        private static class DetachedSchedule {
            public Episode AiringSchedule;
        }

        public static Episode deserialize(String serialized) throws ParsingError {
            try {
                return JsonMapper.deserializeGeneric(serialized, DetachedData.class).data.AiringSchedule;
            } catch (IOException e) {
                throw new ParsingError(e);
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Result {
        public Group data;
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Group {
            public Page Page;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Page {
            public Info pageInfo;
            public List<Episode> airingSchedules;

            @JsonIgnoreProperties(ignoreUnknown = true)
            private static class Info {
                public boolean hasNextPage;
            }
        }
    }
}
