package com.astarivi.kaizolib.nibl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NiblResult {
    public int botId;
    public int number;
    public String name;
    public String size;
    public long sizekbits;
    public int episodeNumber;
    public String lastModified;
}
