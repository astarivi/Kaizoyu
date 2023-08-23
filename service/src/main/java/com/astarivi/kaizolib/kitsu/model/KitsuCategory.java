package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuCategory {
    public String id;
    public String type;
    public KitsuCategoriesAttributes attributes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KitsuCategoriesAttributes {
        public String createdAt;
        public String updatedAt;
        public String title;
        public String description;
        public Integer totalMediaCount;
        public String slug;
        public Boolean nsfw;
        public Integer childCount;
    }
}
