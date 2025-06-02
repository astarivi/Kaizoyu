package com.astarivi.kaizolib.kitsuv2.exception;

public class CloudflareChallengeException extends Exception {
    private final String url;

    public CloudflareChallengeException(String url) {
        super("Cloudflare link returned a challenge");
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
