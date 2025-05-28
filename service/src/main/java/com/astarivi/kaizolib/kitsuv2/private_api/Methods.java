package com.astarivi.kaizolib.kitsuv2.private_api;

import com.astarivi.kaizolib.kitsuv2.common.KitsuCommon;
import com.astarivi.kaizolib.kitsuv2.exception.KitsuException;
import com.astarivi.kaizolib.kitsuv2.exception.NotAuthenticatedException;
import com.astarivi.kaizolib.kitsuv2.exception.ParsingError;
import com.astarivi.kaizolib.kitsuv2.model.KitsuCredentials;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Consumer;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Methods extends KitsuCommon {
    protected static KitsuCredentials credentials = null;
    protected static Consumer<KitsuCredentials> refreshCallback = null;
    protected static HttpUrl AUTH_ENDPOINT = new HttpUrl.Builder()
            .scheme("https")
            .host("kitsu.app")
            .addPathSegments("api/oauth/token")
            .build();

    protected static HttpUrl SELF_USER_ENDPOINT = new HttpUrl.Builder()
            .scheme("https")
            .host("kitsu.app")
            .addPathSegments("api/edge/users")
            .addQueryParameter("filter[self]", "true")
            .build();

    protected static synchronized KitsuCredentials getCredentials() throws KitsuException, ParsingError {
        if (credentials == null)
            throw new KitsuException(new NotAuthenticatedException("Not authenticated"));

        if (credentials.accessTokenExpired()) {
            credentials = refresh(credentials);
            if (refreshCallback != null) refreshCallback.accept(credentials);
        }

        return credentials;
    }

    protected static String executeGetWithCredentials(HttpUrl url) throws KitsuException, ParsingError {
        Request.Builder base = getBaseBuilder(url);

        base.addHeader(
                "Authorization",
                String.format(Locale.US, "Bearer %s", getCredentials().accessToken())
        );

        return execute(base.get().build());
    }

    public static void initializeCredentials(KitsuCredentials creds) {
        credentials = creds;
    }

    public static void setOnRefreshListener(Consumer<KitsuCredentials> onRefresh) {
        refreshCallback = onRefresh;
    }

    @NotNull
    protected static Request.Builder baseAuthRequest() {
        Request.Builder builder = new Request.Builder().url(AUTH_ENDPOINT);

        builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
        builder.addHeader("Accept", "application/vnd.api+json");

        return builder;
    }

    @NotNull
    public static KitsuCredentials login(String email, String password) throws KitsuException, ParsingError {
        Request.Builder builder = baseAuthRequest();

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", email)
                .add("password", password)
                .build();

        String result = execute(builder.post(requestBody).build());

        return KitsuCredentials.deserialize(result);
    }

    @NotNull
    public static KitsuCredentials refresh(@NotNull KitsuCredentials credentials) throws KitsuException, ParsingError {
        Request.Builder builder = baseAuthRequest();

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", credentials.refreshToken())
                .build();

        String result = execute(builder.post(requestBody).build());

        return KitsuCredentials.deserialize(result);
    }
}
