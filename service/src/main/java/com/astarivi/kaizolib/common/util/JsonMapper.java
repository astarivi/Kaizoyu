package com.astarivi.kaizolib.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class JsonMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectReader getObjectReader() {
        return objectMapper.reader();
    }

    public static ObjectWriter getObjectWriter() {
        return objectMapper.writer();
    }

    public static @NotNull <T> T deserializeGeneric(@NotNull String jsonString, Class<T> resultType) throws IOException {
        return getObjectReader().readValue(jsonString, resultType);
    }
}
