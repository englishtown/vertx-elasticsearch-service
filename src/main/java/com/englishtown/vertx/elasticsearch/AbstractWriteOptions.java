package com.englishtown.vertx.elasticsearch;

import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.WriteConsistencyLevel;

/**
 * Abstract options
 */
public abstract class AbstractWriteOptions<T extends AbstractWriteOptions<T>> extends AbstractOptions<T> {

    private WriteConsistencyLevel consistencyLevel;
    private String timeout;

    public static final String FIELD_CONSISTENCY_LEVEL = "consistencyLevel";
    public static final String FIELD_TIMEOUT = "timeout";

    protected AbstractWriteOptions() {
    }

    protected AbstractWriteOptions(T other) {
        super(other);
        consistencyLevel = other.getConsistencyLevel();
        timeout = other.getTimeout();
    }

    protected AbstractWriteOptions(JsonObject json) {
        super(json);

        timeout = json.getString(FIELD_TIMEOUT);

        String s = json.getString(FIELD_CONSISTENCY_LEVEL);
        if (s != null) consistencyLevel = WriteConsistencyLevel.fromString(s);

    }

    public WriteConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public T setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return returnThis();
    }

    public String getTimeout() {
        return timeout;
    }

    public T setTimeout(String timeout) {
        this.timeout = timeout;
        return returnThis();
    }

    public JsonObject toJson() {
        JsonObject json = super.toJson();

        if (getConsistencyLevel() != null) {
            json.put(FIELD_CONSISTENCY_LEVEL, getConsistencyLevel().toString().toLowerCase());
        }
        if (getTimeout() != null) json.put(FIELD_TIMEOUT, getTimeout());

        return json;
    }

}
