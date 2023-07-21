package com.astarivi.kaizoyu.core.rss;

import com.astarivi.kaizolib.common.network.HttpMethods;
import com.astarivi.kaizolib.common.util.StringPair;
import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizolib.kitsu.exception.NoResponseException;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.utils.Data;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;


public class RssFetcher {
    private static final Pattern pattern = Pattern.compile("<meta property=\"og:image\" content=\"(.*)\">");

    @ThreadedOnly
    public static List<SyndEntry> getANNFeed() throws NetworkConnectionException, NoResponseException, FeedException {
        String feedBody = HttpMethods.get(
                Data.getUserHttpClient(),
                buildANNFeedUrl(),
                new ArrayList<>(
                        Arrays.asList(
                                new StringPair("Accept", "text/xml;charset=UTF-8"),
                                new StringPair("Content-Type", "text/xml;charset=UTF-8")
                        )
                )
        );

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new StringReader(feedBody));
        List<SyndEntry> entries = feed.getEntries();

        if (entries.isEmpty()) throw new FeedException("Empty feed");
        return entries;
    }

    public static String getThumbnailUrl(SyndEntry entry) {
        if (entry.getLink() == null) return null;

        String feedBody;
        try {
            feedBody = HttpMethods.get(
                    Data.getUserHttpClient(),
                    HttpUrl.parse(entry.getLink()),
                    new ArrayList<>(
                            Arrays.asList(
                                    new StringPair("Accept", "text/html; charset=UTF-8"),
                                    new StringPair("Content-Type", "text/html; charset=UTF-8")
                            )
                    )
            );
        } catch (Exception e) {
            Logger.error("Error fetching image for {}", entry.getLink());
            return null;
        }

        Matcher matcher = pattern.matcher(feedBody);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static @NotNull HttpUrl buildANNFeedUrl() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("www.animenewsnetwork.com")
                .addPathSegments("all/rss.xml")
                .addQueryParameter("ann-edition", "w")
                .build();
    }

}
