package com.astarivi.kaizolib.kitsu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuResourceResult {
    public KitsuAnime data;
}