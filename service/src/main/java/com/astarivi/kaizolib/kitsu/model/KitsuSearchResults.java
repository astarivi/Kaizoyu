package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuSearchResults {
    public List<KitsuAnime> data;
    public KitsuSearchMeta meta;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuSearchMeta {
        public int count;
    }
}
