package com.astarivi.kaizolib.kitsuv2.model;

import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public record KitsuCredentials(
        String accessToken,
        String refreshToken,
        long createdAt,
        long expiresIn
) {
    @JsonIgnore
    public boolean accessTokenExpired() {
        long now = System.currentTimeMillis() / 1000;
        long leeway = 60;
        return now >= (createdAt + expiresIn - leeway);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Deserializer {
        public String access_token;
        public String token_type;
        public long expires_in;
        public String refresh_token;
        public String scope;
        public long created_at;

        public Deserializer() {
        }
    }

    @NotNull
    @Contract("_ -> new")
    @JsonIgnore
    public static KitsuCredentials deserialize(String serialized) throws ParsingError {
        Deserializer deserialized;

        try {
            deserialized = JsonMapper.deserializeGeneric(serialized, Deserializer.class);
        } catch (IOException | NullPointerException e) {
            throw new ParsingError(e);
        }

        return new KitsuCredentials(
                deserialized.access_token,
                deserialized.refresh_token,
                deserialized.created_at,
                deserialized.expires_in
        );
    }
}
