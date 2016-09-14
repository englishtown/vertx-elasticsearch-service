package com.englishtown.vertx.elasticsearch.integration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Guice integration tests
 */
public class GuiceIntegrationTest extends IntegrationTestBase {
    @Override
    protected String getVerticleName() {
        return "java-guice:com.englishtown.vertx.elasticsearch.ElasticSearchServiceVerticle";
    }

    @Override
    protected void configure(JsonObject config) {
        config.put("address", "et.elasticsearch")
                .put("guice_binder", new JsonArray()
                        .add("com.englishtown.vertx.elasticsearch.guice.ElasticSearchBinder"));
    }
}
