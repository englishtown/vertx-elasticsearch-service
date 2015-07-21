package com.englishtown.vertx.elasticsearch;

import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.VersionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link IndexOptions}
 */
public class IndexOptionsTest {

    @Test
    public void testIndexOptions() throws Exception {

        IndexOptions options1 = new IndexOptions()
                .setId("test_id");

        JsonObject json1 = options1.toJson();
        assertEquals(1, json1.fieldNames().size());

        IndexOptions options2 = new IndexOptions(json1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options1.setId("test_id")
                .setRouting("routing")
                .setParent("parent")
                .setOpType(IndexRequest.OpType.CREATE)
                .setRefresh(true)
                .setConsistencyLevel(WriteConsistencyLevel.ALL)
                .setVersion(2L)
                .setVersionType(VersionType.EXTERNAL)
                .setTimestamp("timestamp")
                .setTtl(1000L)
                .setTimeout("timeout");

        json1 = options1.toJson();
        assertEquals(11, json1.fieldNames().size());

        options2 = new IndexOptions(json1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new IndexOptions(options1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

    }

}