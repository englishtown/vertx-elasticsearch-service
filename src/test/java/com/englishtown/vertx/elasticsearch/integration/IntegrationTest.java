package com.englishtown.vertx.elasticsearch.integration;

import com.englishtown.vertx.elasticsearch.ElasticSearchService;
import com.englishtown.vertx.elasticsearch.IndexOptions;
import com.englishtown.vertx.elasticsearch.SearchOptions;
import com.englishtown.vertx.elasticsearch.SearchScrollOptions;
import com.englishtown.vertx.elasticsearch.impl.DefaultElasticSearchService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * {@link com.englishtown.vertx.elasticsearch.ElasticSearchServiceVerticle} integration test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest extends VertxTestBase {

    private ElasticSearchService service;
    private String id = "integration-test-1";
    private String index = "test_index";
    private String type = "test_type";
    private String source_user = "englishtown";
    private String source_message = "vertx elastic search";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        service = ElasticSearchService.createEventBusProxy(vertx, "et.elasticsearch");

        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions options = new DeploymentOptions().setConfig(readConfig());

        vertx.deployVerticle("service:com.englishtown.vertx.vertx-elasticsearch-service", options, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
                fail();
            }
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        service.stop();
    }

    @Test
    public void test1Index() throws Exception {

        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        IndexOptions options = new IndexOptions().setId(id);

        service.index(index, type, source, options, result -> {

            assertTrue(result.succeeded());
            JsonObject json = result.result();

            assertEquals(index, json.getString(DefaultElasticSearchService.CONST_INDEX));
            assertEquals(type, json.getString(DefaultElasticSearchService.CONST_TYPE));
            assertEquals(id, json.getString(DefaultElasticSearchService.CONST_ID));
            assertTrue(json.getInteger(DefaultElasticSearchService.CONST_VERSION, 0) > 0);

            // Give elasticsearch time to index the document
            vertx.setTimer(1000, id -> testComplete());

        });

        await();

    }

    @Test
    public void test3Get() throws Exception {

        service.get(index, type, id, result -> {

            assertTrue(result.succeeded());
            JsonObject body = result.result();

            assertEquals(index, body.getString(DefaultElasticSearchService.CONST_INDEX));
            assertEquals(type, body.getString(DefaultElasticSearchService.CONST_TYPE));
            assertEquals(id, body.getString(DefaultElasticSearchService.CONST_ID));
            assertTrue(body.getInteger(DefaultElasticSearchService.CONST_VERSION, 0) > 0);

            JsonObject source = body.getJsonObject(DefaultElasticSearchService.CONST_SOURCE);
            assertNotNull(source);
            assertEquals(source_user, source.getString("user"));
            assertEquals(source_message, source.getString("message"));

            testComplete();

        });

        await();

    }

    @Test
    public void test4Search_Simple() throws Exception {

        SearchOptions options = new SearchOptions()
                .setTimeout("1000")
                .setSize(10)
                .setFrom(10)
                .addField("user")
                .addField("message")
                .addSort("user", SortOrder.DESC)
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        service.search(index, options, result -> {

            assertTrue(result.succeeded());
            JsonObject json = result.result();
            assertNotNull(json);
            testComplete();

        });

        await();
    }

    @Test
    public void test5Scroll() throws Exception {

        SearchOptions options = new SearchOptions()
                .setSearchType(SearchType.SCAN)
                .setScroll("5m")
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        service.search(index, options, result1 -> {

            assertTrue(result1.succeeded());
            JsonObject json = result1.result();

            String scrollId = json.getString("_scroll_id");
            assertNotNull(scrollId);

            SearchScrollOptions scrollOptions = new SearchScrollOptions().setScroll("5m");

            service.searchScroll(scrollId, scrollOptions, result2 -> {

                assertTrue(result2.succeeded());
                JsonObject json2 = result2.result();

                JsonObject hits = json2.getJsonObject("hits");
                assertNotNull(hits);
                JsonArray hitsArray = hits.getJsonArray("hits");
                assertNotNull(hitsArray);
                assertTrue(hitsArray.size() > 0);

                testComplete();

            });

        });

        await();
    }

    @Test
    public void test6Delete() throws Exception {

        service.delete(index, type, id, result -> {

            assertTrue(result.succeeded());
            JsonObject json = result.result();

            assertEquals(index, json.getString(DefaultElasticSearchService.CONST_INDEX));
            assertEquals(type, json.getString(DefaultElasticSearchService.CONST_TYPE));
            assertEquals(id, json.getString(DefaultElasticSearchService.CONST_ID));
            assertTrue(json.getInteger(DefaultElasticSearchService.CONST_VERSION, 0) > 0);

            testComplete();

        });

        await();
    }

    private JsonObject readConfig() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try (Scanner scanner = new Scanner(cl.getResourceAsStream("config.json")).useDelimiter("\\A")) {
            String s = scanner.next();
            return new JsonObject(s);
        }

    }

}
