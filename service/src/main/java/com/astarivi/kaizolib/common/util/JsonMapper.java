package com.astarivi.kaizolib.common.util;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JsonMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = XmlMapper.builder(
            XmlFactory.builder()
                    .xmlInputFactory(new WstxInputFactory())
                    .xmlOutputFactory(new WstxOutputFactory())
                    .build()
    ).build();

    public static ObjectReader getXmlReader() {
        return xmlMapper.reader();
    }

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
