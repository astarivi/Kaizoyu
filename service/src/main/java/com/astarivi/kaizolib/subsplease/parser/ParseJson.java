package com.astarivi.kaizolib.subsplease.parser;

import com.astarivi.kaizolib.subsplease.model.SubsPleaseResult;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseTodayResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;


public class ParseJson {
    public static @Nullable SubsPleaseResult parse(String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, SubsPleaseResult.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static @Nullable SubsPleaseTodayResult parseToday(String jsonInString) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonInString, SubsPleaseTodayResult.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
