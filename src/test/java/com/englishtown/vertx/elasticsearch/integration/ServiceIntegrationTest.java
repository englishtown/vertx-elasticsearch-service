package com.englishtown.vertx.elasticsearch.integration;

import io.vertx.core.json.JsonObject;

/**
 * "service" prefix integration tests
 */
public class ServiceIntegrationTest extends IntegrationTestBase {

    @Override
    protected String getVerticleName() {
        return "service:com.englishtown.vertx.vertx-elasticsearch-service";
    }

    @Override
    protected void configure(JsonObject config) {
        // Do nothing
    }
}
