package com.astarivi.kaizolib.nibl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NiblBot {
    public int id;
    public String name;
    public String owner;
    public String lastProcessed;
    public int batchEnable;
    public int packSize;
}
