package com.astarivi.kaizolib.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;


public class JsonMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectReader getObjectReader() {
        return objectMapper.reader();
    }

    public static ObjectWriter getObjectWriter() {
        return objectMapper.writer();
    }
}
