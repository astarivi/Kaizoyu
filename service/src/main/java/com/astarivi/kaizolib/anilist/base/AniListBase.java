package com.astarivi.kaizolib.anilist.base;

import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.jetbrains.annotations.Nullable;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;


public abstract class AniListBase {
    public static final HttpUrl ANILIST_URL = new HttpUrl.Builder()
            .scheme("https")
            .host("graphql.anilist.co")
            .build();

    protected static final String ANIME_QUERY_BY_ID = "query($id:Int){Media(id:$id,type:ANIME){id " +
            "idMal description title{romaji english native userPreferred}averageScore format status" +
            " startDate{year month day}coverImage{extraLarge large medium color}bannerImage siteUrl" +
            " episodes duration genres trailer{id site}}}";

    protected static final String ANIME_QUERY_SEARCH_TITLE = "query($name:String,$page:Int,$limit" +
            ":Int){Page(page:$page,perPage:$limit){media(search:$name,type:ANIME){id idMal descri" +
            "ption title{romaji english native userPreferred}averageScore format status startDate" +
            "{year month day}coverImage{extraLarge large medium color}bannerImage siteUrl episodes" +
            " duration genres trailer{id site}}}}";

    protected static final String AIRING_SCHEDULE_QUERY = "query($page:Int,$week_start:Int,$week_" +
            "end:Int){Page(page:$page){pageInfo{hasNextPage}airingSchedules(airingAt_greater:$wee" +
            "k_start,airingAt_lesser:$week_end){id episode airingAt media{id idMal description ti" +
            "tle{romaji english native userPreferred}averageScore format status startDate{year mo" +
            "nth day}coverImage{extraLarge large medium color}bannerImage siteUrl episodes durati" +
            "on genres trailer{id site}}}}}";

    protected static <T> Request getRequestFor(GraphQLRequest<T> request) throws ParsingError {
        return getBaseBuilder().post(getBaseBody(request)).build();
    }

    protected static Request.Builder getBaseBuilder() {
        Request.Builder builder = new Request.Builder().url(ANILIST_URL);

        CommonHeaders.addTo(builder, CommonHeaders.JSON_HEADERS);

        return builder;
    }

    protected static <T> RequestBody getBaseBody(GraphQLRequest<T> request) throws ParsingError {
        return RequestBody.create(request.serialize(), MediaType.get("application/json"));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static class GraphQLRequest<T> {
        public String query;
        public T variables;

        @JsonIgnore
        public GraphQLRequest(String query, @Nullable T variables) {
            this.query = query;
            this.variables = variables;
        }

        public GraphQLRequest() {
        }

        @JsonIgnore
        public String serialize() throws ParsingError {
            try {
                return JsonMapper.getObjectWriter().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new ParsingError(e);
            }
        }
    }
}
