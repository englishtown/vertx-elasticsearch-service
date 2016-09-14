package com.englishtown.vertx.elasticsearch;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Delete by query operation options
 */
@DataObject
public class DeleteByQueryOptions {

    private List<String> types = new ArrayList<>();
    private String timeout;
    private String routing;
    private JsonObject query;

    public static final String JSON_FIELD_TYPES = "types";
    public static final String JSON_FIELD_TIMEOUT = "timeout";
    public static final String JSON_FIELD_ROUTING = "routing";
    public static final String JSON_FIELD_QUERY = "query";

    public DeleteByQueryOptions() {
    }

    public DeleteByQueryOptions(DeleteByQueryOptions other) {
        types = other.getTypes();
        timeout = other.getTimeout();
        routing = other.getRouting();
        query = other.getQuery();
    }

    public DeleteByQueryOptions(JsonObject json) {
        types = json.getJsonArray(JSON_FIELD_TYPES, new JsonArray()).getList();
        timeout = json.getString(JSON_FIELD_TIMEOUT);
        routing = json.getString(JSON_FIELD_ROUTING);
        query = json.getJsonObject(JSON_FIELD_QUERY);
    }

    public List<String> getTypes() {
        return types;
    }

    public DeleteByQueryOptions addType(String type) {
        types.add(type);
        return this;
    }

    public JsonObject getQuery() {
        return query;
    }

    public DeleteByQueryOptions setQuery(JsonObject query) {
        this.query = query;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public DeleteByQueryOptions setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getRouting() {
        return routing;
    }

    public DeleteByQueryOptions setRouting(String routing) {
        this.routing = routing;
        return this;
    }

    public JsonObject toJson() {

        JsonObject json = new JsonObject();

        if (!types.isEmpty()) json.put(JSON_FIELD_TYPES, new JsonArray(types));
        if (timeout != null) json.put(JSON_FIELD_TIMEOUT, timeout);
        if (routing != null) json.put(JSON_FIELD_ROUTING, routing);
        if (query != null) json.put(JSON_FIELD_QUERY, query);

        return json;
    }

}
