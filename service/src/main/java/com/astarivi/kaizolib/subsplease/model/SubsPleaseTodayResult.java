package com.astarivi.kaizolib.subsplease.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SubsPleaseTodayResult {
    public String tz;
    public List<SubsPleaseAnime> schedule;
}
