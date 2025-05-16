package com.astarivi.kaizoyu.core.updater;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.network.HttpMethodsV2;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.utils.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.HttpUrl;
import okhttp3.Request;


@ThreadedOnly
public class UpdateManager {
    public static String VERSION = BuildConfig.VERSION_NAME;
    public static String[] RELEASE_ABI = new String[]{
            "app-armeabi-v7a-release.apk",
            "app-arm64-v8a-release.apk",
            "app-x86-release.apk",
            "app-x86_64-release.apk"
    };

    public static boolean isBeta() {
        return VERSION.contains("-BETA");
    }

    @Nullable
    public static String databaseUpdateAvailable() throws IOException {
        ExtendedProperties config = Data.getProperties(Data.CONFIGURATION.APP);
        String localVersion = config.getProperty("idsVersion", null);

        final RemoteVersions remoteVersions = RemoteVersions.latest();

        if (remoteVersions == null || remoteVersions.database == null || remoteVersions.database.isEmpty())
            return null;

        if (localVersion == null) return remoteVersions.database;

        LocalDate localDate;
        LocalDate remoteDate;
        try {
            localDate = LocalDate.parse(localVersion);
            remoteDate = LocalDate.parse(remoteVersions.database);
        } catch (Exception e) {
            Logger.error("Failed to parse dates {} and/or {}", localVersion, remoteVersions.database);
            return null;
        }

        if (remoteDate.isAfter(localDate)) {
            return remoteVersions.database;
        }

        return null;
    }

    public static @Nullable AppUpdate getAppUpdate() throws IOException {
        // This version has no update capabilities.
        if (VERSION.contains("-DEBUG") || isBeta()) return null;

        if (Boolean.parseBoolean(KaizoyuApplication.getContext().getString(R.string.is_fdroid)))
            return null;

        final RemoteVersions remoteVersions = RemoteVersions.latest();

        if (remoteVersions == null) return null;

        float latestVersion;
        float currentVersion;

        try {
            latestVersion = Float.parseFloat(
                    remoteVersions.app
            );
            currentVersion = Float.parseFloat(
                    VERSION
            );
        } catch (NumberFormatException e) {
            throw new IOException("Couldn't parse latest version TAG");
        }

        if (Float.compare(latestVersion, currentVersion) <= 0) {
            return null;
        }

        GitHubLatestRelease latestRelease = GitHubLatestRelease.latest();

        if (latestRelease == null)
            return null;

        final String desiredVersion = String.format(
                "app-%s-release.apk",
                Build.SUPPORTED_ABIS[0]
        );

        for (String releaseAbi : RELEASE_ABI) {
            if (releaseAbi.equals(desiredVersion)) {
                return new AppUpdate(
                        String.format(
                                Locale.US,
                                "https://github.com/astarivi/Kaizoyu/releases/latest/download/%s",
                                releaseAbi
                        ),
                        latestRelease.body,
                        String.valueOf(latestVersion)
                );
            }
        }

        return new AppUpdate(
                "https://github.com/astarivi/Kaizoyu/releases/latest/download/app-universal-release.apk",
                latestRelease.body,
                String.valueOf(latestVersion)
        );
    }

    @Getter
    @AllArgsConstructor
    public static class AppUpdate implements Parcelable {
        private String URL;
        private String body;
        private String version;

        protected AppUpdate(Parcel parcel) {
            URL = parcel.readString();
            body = parcel.readString();
            version = parcel.readString();
        }

        public static final Parcelable.Creator<AppUpdate> CREATOR = new Parcelable.Creator<>() {
            @Override
            public AppUpdate createFromParcel(Parcel parcel) {
                return new AppUpdate(parcel);
            }

            @Override
            public AppUpdate[] newArray(int size) {
                return new AppUpdate[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(URL);
            dest.writeString(body);
            dest.writeString(version);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubLatestRelease {
        public String body;

        /**
         * @noinspection unused
         */
        public GitHubLatestRelease() {
        }

        @Nullable
        protected static GitHubLatestRelease latest() throws IOException {
            Request.Builder getRequestBuilder = new Request.Builder();

            getRequestBuilder.url(
                    new HttpUrl.Builder()
                            .scheme("https")
                            .host("api.github.com")
                            .addPathSegments("repos/astarivi/Kaizoyu/releases/latest")
                            .build()
            );

            CommonHeaders.addTo(getRequestBuilder, CommonHeaders.JSON_HEADERS);

            String body = HttpMethodsV2.executeRequest(getRequestBuilder.build());

            if (body == null) return null;

            return JsonMapper.deserializeGeneric(body, GitHubLatestRelease.class);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoteVersions {
        public String app;
        public String database;

        /**
         * @noinspection unused
         */
        public RemoteVersions() {
        }

        @Nullable
        protected static RemoteVersions latest() throws IOException {
            Request.Builder getRequestBuilder = new Request.Builder();

            getRequestBuilder.url(
                    new HttpUrl.Builder()
                            .scheme("https")
                            .host("raw.githubusercontent.com")
                            .addPathSegments("astar-workspace/k.delivery/refs/heads/main/metadata/versions.json")
                            .build()
            );

            CommonHeaders.addTo(getRequestBuilder, CommonHeaders.TEXT_HEADERS);

            String body = HttpMethodsV2.executeRequest(getRequestBuilder.build());

            if (body == null) return null;

            return JsonMapper.deserializeGeneric(body, RemoteVersions.class);
        }
    }
}
