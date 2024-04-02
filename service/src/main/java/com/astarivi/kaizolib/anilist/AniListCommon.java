package com.astarivi.kaizolib.anilist;

import com.astarivi.kaizolib.anilist.exception.ParsingError;
import com.astarivi.kaizolib.common.network.CommonHeaders;
import com.astarivi.kaizolib.common.util.JsonMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;


abstract class AniListCommon {
    public static final HttpUrl ANILIST_URL = new HttpUrl.Builder()
            .scheme("https")
            .host("graphql.anilist.co")
            .build();

    protected static final String ANIME_QUERY_BY_ID = "query($id:Int){Media(id:$id,type:ANIME){id " +
            "idMal description title{romaji english native userPreferred}averageScore format status" +
            " startDate{year month day}coverImage{extraLarge large medium color}bannerImage siteUrl" +
            " episodes duration genres trailer{id site}}}";

    protected static final String ANIME_QUERY_SEARCH_TITLE = "query($name:String,$page:Int" +
            "){Page(page:$page){pageInfo{hasNextPage}media(search:$name,type:ANIME){id idMal descri" +
            "ption title{romaji english native userPreferred}averageScore format status startDate" +
            "{year month day}coverImage{extraLarge large medium color}bannerImage siteUrl episodes" +
            " duration genres trailer{id site}}}}";

    protected static final String AIRING_SCHEDULE_QUERY = "query($page:Int,$week_start:Int,$week_" +
            "end:Int){Page(page:$page){pageInfo{hasNextPage}airingSchedules(airingAt_greater:$wee" +
            "k_start,airingAt_lesser:$week_end){id episode airingAt media{id idMal description ti" +
            "tle{romaji english native userPreferred}averageScore type format status startDate{year mo" +
            "nth day}coverImage{extraLarge large medium color}bannerImage siteUrl episodes durati" +
            "on genres trailer{id site}}}}}";

    protected static final String AIRING_ANIME_QUERY = "query($media_id:Int,$start:Int){AiringSch" +
            "edule(airingAt_greater:$start,mediaId:$media_id){id episode airingAt media{id idMal " +
            "description title{romaji english native userPreferred}averageScore type format status sta" +
            "rtDate{year month day}coverImage{extraLarge large medium color}bannerImage siteUrl e" +
            "pisodes duration genres trailer{id site}}}}";

    protected static final String TRENDING_ANIME_QUERY = "query($page:Int){Page(page:$page){pageI" +
            "nfo{hasNextPage}media(sort:TRENDING_DESC,type:ANIME){id idMal description title{roma" +
            "ji english native userPreferred}averageScore format status startDate{year month day}" +
            "coverImage{extraLarge large medium color}bannerImage siteUrl episodes duration genre" +
            "s trailer{id site}}}}";

    protected static Request getRequestFor(GraphQLRequest request) throws ParsingError {
        return getBaseBuilder().post(getBaseBody(request)).build();
    }

    protected static Request.Builder getBaseBuilder() {
        Request.Builder builder = new Request.Builder().url(ANILIST_URL);

        CommonHeaders.addTo(builder, CommonHeaders.JSON_HEADERS);

        return builder;
    }

    protected static RequestBody getBaseBody(GraphQLRequest request) throws ParsingError {
        return RequestBody.create(request.serialize(), MediaType.get("application/json"));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static class GraphQLRequest {
        public String query;
        public Map<String, Object> variables;

        @JsonIgnore
        protected GraphQLRequest(String query, @Nullable Map<String, Object> variables) {
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

    protected static class PagedGraphQLRequest extends GraphQLRequest {
        @JsonIgnore
        protected PagedGraphQLRequest(String query, @NotNull Map<String, Object> variables) {
            super(query, variables);
            variables.put("page", 1);
        }

        protected void setPage(long page) {
            variables.put("page", page);
        }
    }
}
