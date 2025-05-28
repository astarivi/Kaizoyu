package com.astarivi.kaizolib.kitsuv2.model;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KitsuUser {
    public long id;
    public Attributes attributes;


    @JsonProperty("id")
    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        public String name;
        public String about;
        public KitsuAnime.KitsuAnimeImages avatar;
        public KitsuAnime.KitsuAnimeImages coverImage;
        public String email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Deserializer {
        public List<KitsuUser> data;

        public Deserializer() {
        }
    }

    @JsonIgnore
    public static @NotNull KitsuUser deserialize(String serialized) throws ParsingError {
        try {
            List<KitsuUser> data = JsonMapper.deserializeGeneric(serialized, KitsuUser.Deserializer.class).data;

            if (data == null || data.isEmpty()) {
                throw new ParsingError("Nothing to deserialize, no results");
            }

            return data.get(0);
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }
}
