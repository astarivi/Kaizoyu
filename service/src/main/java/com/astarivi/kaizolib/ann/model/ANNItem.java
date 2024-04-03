package com.astarivi.kaizolib.ann.model;

import com.astarivi.kaizolib.ann.ANN;
import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.tinylog.Logger;

import java.util.regex.Matcher;

import okhttp3.HttpUrl;
import okhttp3.Request;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ANNItem {
    public String title;
    public String link;
    public String description;
    public String pubDate;
    public String category;

    @JacksonXmlProperty(localName = "description")
    public void setDescription(String value) {
        if (value == null) {
            description = null;
            return;
        }

        description = Jsoup.parse(value).text();
    }

    @JsonIgnore
    public @Nullable String getThumbnailUrl() {
        if (link == null) return null;
        Request.Builder getRequestBuilder = new Request.Builder();

        HttpUrl parsedUrl = HttpUrl.parse(link);

        if (parsedUrl == null) return null;

        getRequestBuilder.url(
                parsedUrl
        );

        CommonHeaders.addTo(getRequestBuilder, CommonHeaders.TEXT_HEADERS);

        String feedBody;
        try {
            feedBody = HttpMethodsV2.executeRequest(getRequestBuilder.build());
        } catch (Exception e) {
            Logger.error("Error fetching ANN thumbnail for {}", link);
            return null;
        }

        Matcher matcher = ANN.THUMBNAIL_PATTERN.matcher(feedBody);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
