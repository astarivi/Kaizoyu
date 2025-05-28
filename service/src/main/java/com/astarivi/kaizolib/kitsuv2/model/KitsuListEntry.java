package com.astarivi.kaizolib.kitsuv2.model;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record KitsuListEntry(
        long id,
        int progress,
        KitsuAnime anime
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Deserializer {
        public List<ListInfo> data;
        public List<KitsuAnime> included;
        public Meta meta;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Meta {
            public long count;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ListInfo {
            public long id;
            public Attributes attributes;
            public Relationships relationships;

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
                public int progress;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Relationships {
                public AnimeRelation anime;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class AnimeRelation {
                    public Data data;

                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public static class Data {
                        public long id;

                        @JsonProperty("id")
                        public void setId(String id) {
                            this.id = Long.parseLong(id);
                        }

                        @JsonProperty("id")
                        public void setId(long id) {
                            this.id = id;
                        }
                    }
                }
            }
        }
    }

    @NotNull
    public static List<KitsuListEntry> deserialize(String serialized) throws ParsingError {
        try {
            Deserializer data = JsonMapper.deserializeGeneric(serialized, KitsuListEntry.Deserializer.class);

            if (data.data == null || data.data.isEmpty() || data.included == null || data.included.isEmpty()) {
                throw new ParsingError("Nothing to deserialize, no results (or incomplete)");
            }

            ArrayList<KitsuListEntry> kitsuListEntries = new ArrayList<>();

            for (Deserializer.ListInfo info : data.data) {
                // A hash dict COULD be faster here, but we have like, <= 20 items per batch so...
                for (KitsuAnime included : data.included) {
                    if (info.relationships.anime.data.id == included.id) {
                        kitsuListEntries.add(
                                new KitsuListEntry(
                                        info.id,
                                        info.attributes.progress,
                                        included
                                )
                        );
                    }
                }
            }

            return kitsuListEntries;
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }

    @NotNull
    public static RawResults<KitsuListEntry> deserializeRaw(String serialized) throws ParsingError {
        try {
            Deserializer data = JsonMapper.deserializeGeneric(serialized, KitsuListEntry.Deserializer.class);

            if (data.data == null || data.data.isEmpty() || data.included == null || data.included.isEmpty()) {
                throw new ParsingError("Nothing to deserialize, no results (or incomplete)");
            }

            ArrayList<KitsuListEntry> kitsuListEntries = new ArrayList<>();

            for (Deserializer.ListInfo info : data.data) {
                // A hash dict COULD be faster here, but we have like, <= 20 items per batch so...
                for (KitsuAnime included : data.included) {
                    if (info.relationships.anime.data.id == included.id) {
                        kitsuListEntries.add(
                                new KitsuListEntry(
                                        info.id,
                                        info.attributes.progress,
                                        included
                                )
                        );
                    }
                }
            }

            return new RawResults<>(
                    kitsuListEntries,
                    data.meta.count
            );
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }
    }
}
