package com.englishtown.vertx.elasticsearch.integration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * HK2 integration tests
 */
public class HK2IntegrationTest extends IntegrationTestBase {

    @Override
    protected String getVerticleName() {
        return "java-hk2:com.englishtown.vertx.elasticsearch.ElasticSearchServiceVerticle";
    }

    @Override
    protected void configure(JsonObject config) {
        config.put("address", "et.elasticsearch")
                .put("hk2_binder", new JsonArray()
                        .add("com.englishtown.vertx.hk2.ElasticSearchBinder"));
    }
}
