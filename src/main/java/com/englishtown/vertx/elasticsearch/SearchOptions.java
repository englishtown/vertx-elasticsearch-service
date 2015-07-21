package com.englishtown.vertx.elasticsearch;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Search operation options
 */
@DataObject
public class SearchOptions {

    private List<String> types = new ArrayList<>();
    private SearchType searchType;
    private String scroll;
    private String timeout;
    private Integer terminateAfter;
    private String routing;
    private String preference;
    private JsonObject query;
    private JsonObject postFilter;
    private Float minScore;
    private Integer size;
    private Integer from;
    private Boolean explain;
    private Boolean version;
    private Boolean fetchSource;
    private List<String> fields = new ArrayList<>();
    private Boolean trackScores;
    private JsonObject aggregations;
    private List<SortOption> sorts = new ArrayList<>();
    private JsonObject extraSource;

    public static final String JSON_FIELD_TYPES = "types";
    public static final String JSON_FIELD_SEARCH_TYPE = "searchType";
    public static final String JSON_FIELD_SCROLL = "scroll";
    public static final String JSON_FIELD_TIMEOUT = "timeout";
    public static final String JSON_FIELD_TERMINATE_AFTER = "terminateAfter";
    public static final String JSON_FIELD_ROUTING = "routing";
    public static final String JSON_FIELD_PREFERENCE = "preference";
    public static final String JSON_FIELD_QUERY = "query";
    public static final String JSON_FIELD_POST_FILTER = "postFilter";
    public static final String JSON_FIELD_MIN_SCORE = "minScore";
    public static final String JSON_FIELD_SIZE = "size";
    public static final String JSON_FIELD_FROM = "from";
    public static final String JSON_FIELD_EXPLAIN = "explain";
    public static final String JSON_FIELD_VERSION = "version";
    public static final String JSON_FIELD_FETCH_SOURCE = "fetchSource";
    public static final String JSON_FIELD_FIELDS = "fields";
    public static final String JSON_FIELD_TRACK_SCORES = "trackScores";
    public static final String JSON_FIELD_AGGREGATIONS = "aggregations";
    public static final String JSON_FIELD_SORTS = "sorts";
    public static final String JSON_FIELD_EXTRA_SOURCE = "extraSource";

    public SearchOptions() {
    }

    public SearchOptions(SearchOptions other) {
        types = other.getTypes();
        searchType = other.getSearchType();
        scroll = other.getScroll();
        timeout = other.getTimeout();
        terminateAfter = other.getTerminateAfter();
        routing = other.getRouting();
        preference = other.getPreference();
        query = other.getQuery();
        postFilter = other.getPostFilter();
        minScore = other.getMinScore();
        size = other.getSize();
        from = other.getFrom();
        explain = other.isExplain();
        version = other.isVersion();
        fetchSource = other.isFetchSource();
        fields = other.getFields();
        trackScores = other.isTrackScores();
        aggregations = other.getAggregations();
        sorts = other.getSorts();
        extraSource = other.getExtraSource();
    }

    public SearchOptions(JsonObject json) {

        types = json.getJsonArray(JSON_FIELD_TYPES, new JsonArray()).getList();
        scroll = json.getString(JSON_FIELD_SCROLL);
        timeout = json.getString(JSON_FIELD_TIMEOUT);
        terminateAfter = json.getInteger(JSON_FIELD_TERMINATE_AFTER);
        routing = json.getString(JSON_FIELD_ROUTING);
        preference = json.getString(JSON_FIELD_PREFERENCE);
        query = json.getJsonObject(JSON_FIELD_QUERY);
        postFilter = json.getJsonObject(JSON_FIELD_POST_FILTER);
        minScore = json.getFloat(JSON_FIELD_MIN_SCORE);
        size = json.getInteger(JSON_FIELD_SIZE);
        from = json.getInteger(JSON_FIELD_FROM);
        explain = json.getBoolean(JSON_FIELD_EXPLAIN);
        version = json.getBoolean(JSON_FIELD_VERSION);
        fetchSource = json.getBoolean(JSON_FIELD_FETCH_SOURCE);
        fields = json.getJsonArray(JSON_FIELD_FIELDS, new JsonArray()).getList();
        trackScores = json.getBoolean(JSON_FIELD_TRACK_SCORES);
        aggregations = json.getJsonObject(JSON_FIELD_AGGREGATIONS);
        extraSource = json.getJsonObject(JSON_FIELD_EXTRA_SOURCE);

        String s = json.getString(JSON_FIELD_SEARCH_TYPE);
        if (s != null) searchType = SearchType.fromString(s);

        JsonArray sortOptionsJson = json.getJsonArray(JSON_FIELD_SORTS);
        if (sortOptionsJson != null) {
            for (int i = 0; i < sortOptionsJson.size(); i++) {
                sorts.add(new SortOption(sortOptionsJson.getJsonObject(i)));
            }
        }

    }

    public List<String> getTypes() {
        return types;
    }

    public SearchOptions addType(String type) {
        types.add(type);
        return this;
    }

    public JsonObject getQuery() {
        return query;
    }

    public SearchOptions setQuery(JsonObject query) {
        this.query = query;
        return this;
    }

    public JsonObject getPostFilter() {
        return postFilter;
    }

    public SearchOptions setPostFilter(JsonObject postFilter) {
        this.postFilter = postFilter;
        return this;
    }

    public JsonObject getAggregations() {
        return aggregations;
    }

    public SearchOptions setAggregations(JsonObject aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public SearchOptions setSearchType(SearchType searchType) {
        this.searchType = searchType;
        return this;
    }

    public String getScroll() {
        return scroll;
    }

    public SearchOptions setScroll(String keepAlive) {
        this.scroll = keepAlive;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public SearchOptions setSize(Integer size) {
        this.size = size;
        return this;
    }

    public Integer getFrom() {
        return from;
    }

    public SearchOptions setFrom(Integer from) {
        this.from = from;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public SearchOptions setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public SearchOptions addField(String field) {
        fields.add(field);
        return this;
    }

    public List<SortOption> getSorts() {
        return sorts;
    }

    public SearchOptions addSort(String field, SortOrder order) {
        sorts.add(new SortOption().setField(field).setOrder(order));
        return this;
    }

    public Integer getTerminateAfter() {
        return terminateAfter;
    }

    public SearchOptions setTerminateAfter(Integer terminateAfter) {
        this.terminateAfter = terminateAfter;
        return this;
    }

    public String getRouting() {
        return routing;
    }

    public SearchOptions setRouting(String routing) {
        this.routing = routing;
        return this;
    }

    public String getPreference() {
        return preference;
    }

    public SearchOptions setPreference(String preference) {
        this.preference = preference;
        return this;
    }

    public Float getMinScore() {
        return minScore;
    }

    public SearchOptions setMinScore(Float minScore) {
        this.minScore = minScore;
        return this;
    }

    public Boolean isExplain() {
        return explain;
    }

    public SearchOptions setExplain(Boolean explain) {
        this.explain = explain;
        return this;
    }

    public Boolean isVersion() {
        return version;
    }

    public SearchOptions setVersion(Boolean version) {
        this.version = version;
        return this;
    }

    public Boolean isFetchSource() {
        return fetchSource;
    }

    public SearchOptions setFetchSource(Boolean fetchSource) {
        this.fetchSource = fetchSource;
        return this;
    }

    public Boolean isTrackScores() {
        return trackScores;
    }

    public SearchOptions setTrackScores(Boolean trackScores) {
        this.trackScores = trackScores;
        return this;
    }

    public JsonObject getExtraSource() {
        return extraSource;
    }

    public SearchOptions setExtraSource(JsonObject extraSource) {
        this.extraSource = extraSource;
        return this;
    }

    public JsonObject toJson() {

        JsonObject json = new JsonObject();

        if (!types.isEmpty()) json.put(JSON_FIELD_TYPES, new JsonArray(types));
        if (searchType != null) json.put(JSON_FIELD_SEARCH_TYPE, searchType.name().toLowerCase());
        if (scroll != null) json.put(JSON_FIELD_SCROLL, scroll);
        if (timeout != null) json.put(JSON_FIELD_TIMEOUT, timeout);
        if (terminateAfter != null) json.put(JSON_FIELD_TERMINATE_AFTER, terminateAfter);
        if (routing != null) json.put(JSON_FIELD_ROUTING, routing);
        if (preference != null) json.put(JSON_FIELD_PREFERENCE, preference);
        if (query != null) json.put(JSON_FIELD_QUERY, query);
        if (postFilter != null) json.put(JSON_FIELD_POST_FILTER, postFilter);
        if (minScore != null) json.put(JSON_FIELD_MIN_SCORE, minScore);
        if (size != null) json.put(JSON_FIELD_SIZE, size);
        if (from != null) json.put(JSON_FIELD_FROM, from);
        if (explain != null) json.put(JSON_FIELD_EXPLAIN, explain);
        if (version != null) json.put(JSON_FIELD_VERSION, version);
        if (fetchSource != null) json.put(JSON_FIELD_FETCH_SOURCE, fetchSource);
        if (!fields.isEmpty()) json.put(JSON_FIELD_FIELDS, new JsonArray(fields));
        if (trackScores != null) json.put(JSON_FIELD_TRACK_SCORES, trackScores);
        if (aggregations != null) json.put(JSON_FIELD_AGGREGATIONS, aggregations);
        if (explain != null) json.put(JSON_FIELD_EXPLAIN, explain);

        if (!sorts.isEmpty()) {
            JsonArray jsonSorts = new JsonArray();
            sorts.forEach(sort -> jsonSorts.add(sort.toJson()));
            json.put(JSON_FIELD_SORTS, jsonSorts);
        }

        return json;
    }

}
