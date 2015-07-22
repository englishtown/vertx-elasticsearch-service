package com.englishtown.vertx.elasticsearch;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * PutIndexedScript operation options
 */
@DataObject
public class PutIndexedScriptOptions {

    private String scriptLang;
    private String id;
    private JsonObject source;

    public static final String JSON_FIELD_SCRIPT_LANG= "lang";
    public static final String JSON_FIELD_ID= "id";
    public static final String JSON_FIELD_SOURCE= "source";

    public PutIndexedScriptOptions() {
    }

    public PutIndexedScriptOptions(PutIndexedScriptOptions other) {
        this.scriptLang = other.getScriptLang();
        this.id = other.getId();
        this.source = other.getSource();
    }

    public PutIndexedScriptOptions(JsonObject json) {
        scriptLang = json.getString(JSON_FIELD_SCRIPT_LANG);
        id = json.getString(JSON_FIELD_ID);
        source = json.getJsonObject(JSON_FIELD_SOURCE);
    }

    public String getScriptLang() {
        return scriptLang;
    }

    public PutIndexedScriptOptions setScriptLang(String scriptLang) {
        this.scriptLang = scriptLang;
        return this;
    }

    public String getId() {
        return id;
    }

    public PutIndexedScriptOptions setId(String id) {
        this.id = id;
        return this;
    }

    public JsonObject getSource() {
        return source;
    }

    public PutIndexedScriptOptions setSource(JsonObject source) {
        this.source = source;
        return this;
    }

    public JsonObject toJson() {

        JsonObject json = new JsonObject();

        if (scriptLang != null) json.put(JSON_FIELD_SCRIPT_LANG, scriptLang);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (source != null) json.put(JSON_FIELD_SOURCE, source);

        return json;
    }
}
