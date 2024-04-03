package com.astarivi.kaizolib.ann;

import com.astarivi.kaizolib.ann.model.ANNItem;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.IOException;
import java.util.List;


class RssDeserializer {
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RssFeed {
        @JacksonXmlProperty(localName = "channel")
        public Channel channel;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Channel {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<ANNItem> items;
    }

    protected static List<ANNItem> deserialize(String serialized) throws IOException {
        RssFeed result = JsonMapper.getXmlReader().readValue(serialized, RssFeed.class);

        return result.channel.items;
    }
}
