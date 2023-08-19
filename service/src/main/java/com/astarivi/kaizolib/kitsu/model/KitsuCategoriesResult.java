package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuCategoriesResult {
    public List<KitsuCategories> data;
    public KitsuCategoriesMeta meta;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuCategoriesMeta {
        public int count;
    }
}
