package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuEpisodeResults {
    public List<KitsuEpisode> data;
    public KitsuEpisodeMeta meta;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuEpisodeMeta {
        public int count;
    }
}
