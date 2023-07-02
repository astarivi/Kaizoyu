package com.astarivi.kaizolib.subsplease.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SubsPleaseAnime {
    public String title;
    public String page;
    public String image_url;
    public String time;
    public Boolean aired;
}