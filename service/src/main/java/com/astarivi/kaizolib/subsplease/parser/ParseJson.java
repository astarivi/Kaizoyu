package com.astarivi.kaizolib.subsplease.parser;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseResult;
import com.astarivi.kaizolib.subsplease.model.SubsPleaseTodayResult;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;


public class ParseJson {
    public static @Nullable SubsPleaseResult parse(String jsonInString) {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, SubsPleaseResult.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static @Nullable SubsPleaseTodayResult parseToday(String jsonInString) {
        try {
            return JsonMapper.getObjectReader().readValue(jsonInString, SubsPleaseTodayResult.class);
        } catch (IOException e) {
            return null;
        }
    }
}
