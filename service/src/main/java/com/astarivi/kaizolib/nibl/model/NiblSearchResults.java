package com.astarivi.kaizolib.nibl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NiblSearchResults {
    public String status;
    public String message;
    public List<NiblResult> content;
}
