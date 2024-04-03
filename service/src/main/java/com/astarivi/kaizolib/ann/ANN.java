package com.astarivi.kaizolib.ann;

import com.astarivi.kaizolib.ann.model.ANNItem;
import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Request;


public class ANN {
    public static final HttpUrl ANN_URL = new HttpUrl.Builder()
            .scheme("https")
            .host("www.animenewsnetwork.com")
            .addPathSegments("all/rss.xml")
            .addQueryParameter("ann-edition", "w")
            .build();

    public static final Pattern THUMBNAIL_PATTERN = Pattern.compile("<meta property=\"og:image\" content=\"(.*)\">");

    public static List<ANNItem> getANNFeed() throws IOException {
        Request.Builder getRequestBuilder = new Request.Builder();
        getRequestBuilder.url(
                ANN_URL
        );

        CommonHeaders.addTo(getRequestBuilder, CommonHeaders.XML_HEADERS);

        return RssDeserializer.deserialize(
                HttpMethodsV2.executeRequest(getRequestBuilder.build())
        );
    }
}
