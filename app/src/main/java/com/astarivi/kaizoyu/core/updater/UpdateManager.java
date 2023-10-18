package com.astarivi.kaizoyu.core.updater;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.adapters.WebAdapter;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.HttpUrl;


public class UpdateManager {
    public static String VERSION = BuildConfig.VERSION_NAME;
    public static String VERSION_NAME = "judgement";

    public static boolean isBeta() {
        return VERSION.contains("-BETA");
    }

    @ThreadedOnly
    public @Nullable LatestUpdate getLatestUpdate() throws ParseException {
        // This version has no update capabilities.
        if (VERSION.contains("-DEBUG")) return null;

        if (Boolean.parseBoolean(KaizoyuApplication.getContext().getString(R.string.is_fdroid)))
            return null;

        final boolean isBeta = isBeta();
        final LatestCDNReleases latestReleaseCDN = getLatestReleases();

        if (latestReleaseCDN == null || (!isBeta && latestReleaseCDN.latest_release == null) || (isBeta && latestReleaseCDN.latest_beta == null))
            return null;

        float latestVersion;
        float currentVersion;

        try {
            latestVersion = Float.parseFloat(
                    isBeta ? latestReleaseCDN.latest_beta : latestReleaseCDN.latest_release
            );
            currentVersion = Float.parseFloat(
                    isBeta ? VERSION.replace("-BETA", "") : VERSION
            );
        } catch (NumberFormatException e) {
            throw new ParseException("Couldn't parse latest version TAG", 0);
        }

        if (Float.compare(latestVersion, currentVersion) <= 0) {
            return null;
        }

        GithubRelease latestRelease = getVersion(
                isBeta ? latestReleaseCDN.latest_beta : latestReleaseCDN.latest_release
        );

        if (latestRelease == null || latestRelease.assets == null || latestRelease.assets.length == 0)
            return null;

        final String desiredVersion = String.format(
                "app-%s-%s.apk",
                Build.SUPPORTED_ABIS[0],
                isBeta ? "beta" : "release"
        );

        for (GithubReleaseAsset releaseAsset : latestRelease.assets) {
            if (releaseAsset.name.equals(desiredVersion)) {
                return new LatestUpdate(
                        releaseAsset,
                        latestRelease.tag_name,
                        latestRelease.body
                );
            }
        }

        return null;
    }

    @ThreadedOnly
    private @Nullable LatestCDNReleases getLatestReleases() {
        String body = WebAdapter.getJSON(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("raw.githubusercontent.com")
                        .addPathSegments("astarivi/KaizoDelivery/main/data/latest_release.json")
                        .build()
        );

        if (body == null) return null;

        LatestCDNReleases latestCDNReleases;

        try {
            latestCDNReleases = new ObjectMapper().readValue(body, LatestCDNReleases.class);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to decode Github KaizoDelivery latest releases");
            return null;
        }

        return latestCDNReleases;
    }

    @ThreadedOnly
    private @Nullable GithubRelease getVersion(@NotNull final String tag) {
        String body = WebAdapter.getJSON(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("api.github.com")
                        .addPathSegments("repos/astarivi/KaizoDelivery/releases/tags")
                        .addPathSegment(tag)
                        .build()
        );

        if (body == null) return null;

        GithubRelease latestRelease;

        try {
            latestRelease = new ObjectMapper().readValue(body, GithubRelease.class);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to decode Github latest release");
            return null;
        }

        return latestRelease;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatestCDNReleases {
        public String latest_release;
        public String latest_beta;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubRelease {
        public String tag_name;
        public String published_at;
        public String name;
        public String body;
        public GithubReleaseAsset[] assets;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubReleaseAsset {
        public String url;
        public String name;
        public double size;
        public String browser_download_url;
        public String created_at;
    }

    public static class LatestUpdate implements Parcelable {
        public String version;
        public String name;
        public double size;
        public Date releaseDate;
        public String downloadUrl;
        public String body;

        public LatestUpdate(@NotNull GithubReleaseAsset releaseAsset, String version, String body) throws ParseException {
            Calendar calendar = Calendar.getInstance(
                    TimeZone.getTimeZone(
                            ZoneId.systemDefault()
                    )
            );

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
            sdf.setCalendar(calendar);
            try {
                calendar.setTime(
                        Objects.requireNonNull(sdf.parse(
                                releaseAsset.created_at
                        ))
                );
            } catch(NullPointerException ex) {
                throw new ParseException("The parsed date was null", 0);
            }

            this.releaseDate = calendar.getTime();
            this.name = releaseAsset.name;
            this.size = releaseAsset.size;
            this.downloadUrl = releaseAsset.browser_download_url;
            this.version = version;
            this.body = body;
        }

        // Parcelable implementation
        protected LatestUpdate(@NotNull Parcel parcel) {
            version = parcel.readString();
            name = parcel.readString();
            size = parcel.readDouble();
            releaseDate = new Date(parcel.readLong());
            downloadUrl = parcel.readString();
            body = parcel.readString();
        }

        public static final Parcelable.Creator<LatestUpdate> CREATOR = new Parcelable.Creator<LatestUpdate>() {
            @Override
            public LatestUpdate createFromParcel(Parcel parcel) {
                return new LatestUpdate(parcel);
            }

            @Override
            public LatestUpdate[] newArray(int size) {
                return new LatestUpdate[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(version);
            dest.writeString(name);
            dest.writeDouble(size);
            dest.writeLong(releaseDate.getTime());
            dest.writeString(downloadUrl);
            dest.writeString(body);
        }
    }
}
