package com.englishtown.vertx.elasticsearch;

import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.index.VersionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DeleteOptions}
 */
public class DeleteOptionsTest {

    @Test
    public void testToJson() throws Exception {

        DeleteOptions options1 = new DeleteOptions()
                .setParent("parent");
        JsonObject json1 = options1.toJson();

        assertEquals("{\"parent\":\"parent\"}", json1.encode());

        options1 = new DeleteOptions()
                .setConsistencyLevel(WriteConsistencyLevel.ALL)
                .setParent("parent")
                .setRefresh(true)
                .setRouting("routing")
                .setTimeout("timeout")
                .setVersion(10000L)
                .setVersionType(VersionType.EXTERNAL);

        json1 = options1.toJson();

        assertEquals(7, json1.fieldNames().size());

        DeleteOptions options2 = new DeleteOptions(json1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new DeleteOptions(options1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

    }

}