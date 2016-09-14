package com.englishtown.vertx.elasticsearch.integration;

import com.englishtown.vertx.elasticsearch.DeleteByQueryOptions;
import com.englishtown.vertx.elasticsearch.ElasticSearchAdminService;
import com.englishtown.vertx.elasticsearch.ElasticSearchService;
import com.englishtown.vertx.elasticsearch.IndexOptions;
import com.englishtown.vertx.elasticsearch.SearchOptions;
import com.englishtown.vertx.elasticsearch.SearchScrollOptions;
import com.englishtown.vertx.elasticsearch.SuggestOptions;
import com.englishtown.vertx.elasticsearch.impl.DefaultElasticSearchService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;

/**
 * {@link com.englishtown.vertx.elasticsearch.ElasticSearchServiceVerticle} integration test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class IntegrationTestBase extends VertxTestBase {

    private ElasticSearchService service;
    private ElasticSearchAdminService adminService;
    private String id = "integration-test-1";
    private String index = "test_index";
    private String type = "test_type";
    private String source_user = "englishtown";
    private String source_message = "vertx elastic search";

    protected JsonObject config;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        service = ElasticSearchService.createEventBusProxy(vertx, "et.elasticsearch");
        adminService = ElasticSearchAdminService.createEventBusProxy(vertx, "et.elasticsearch.admin");

        config = readConfig();
        deployVerticle();
    }

    protected abstract void deployVerticle() throws Exception;

    protected void deployVerticle(String name) throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(name, options, result -> {
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
    public void test2Get() throws Exception {

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
    public void test3Search_Simple() throws Exception {

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
    public void test4Search_EventBus_Invalid_Enum() throws Exception {

        JsonObject json = new JsonObject()
                .put("indices", new JsonArray().add(index))
                .put("options", new JsonObject().put("templateType", "invalid_type"));

        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "search");

        vertx.eventBus().<JsonObject>send("et.elasticsearch", json, options, res -> {
            assertTrue(res.failed());
            Throwable t = res.cause();
            assertThat(t, instanceOf(ReplyException.class));
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
    public void test6Suggest() throws Exception {

        JsonObject mapping = readJson("mapping.json");

        adminService.putMapping(index, type, mapping, result1 -> {

            if (result1.failed()) {
                result1.cause().printStackTrace();
                fail();
                return;
            }

            JsonObject source = new JsonObject()
                    .put("user", source_user)
                    .put("message", source_message)
                    .put("message_suggest", source_message);


            service.index(index, type, source, result2 -> {

                if (result2.failed()) {
                    result1.cause().printStackTrace();
                    fail();
                    return;
                }

                // Delay 1s to give time for indexing
                vertx.setTimer(1000, id -> {
                    SuggestOptions options = new SuggestOptions();
                    options.setText("v");
                    options.setField("message_suggest");
                    options.setName("test-suggest");

                    service.suggest(index, options, result3 -> {

                        assertTrue(result3.succeeded());
                        JsonObject json = result3.result();

                        assertNotNull(json.getJsonArray("test-suggest"));
                        assertNotNull(json.getJsonArray("test-suggest").getJsonObject(0));
                        assertEquals(Integer.valueOf(1), json.getJsonArray("test-suggest").getJsonObject(0).getInteger("length"));
                        assertEquals(source_message, json.getJsonArray("test-suggest").getJsonObject(0).getJsonArray("options").getJsonObject(0).getString("text"));

                        testComplete();
                    });
                });

            });

        });

        await();
    }

    @Test
    public void test7DeleteByQuery_Simple() throws Exception {

        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        service.index(index, type, source, indexOptions, indexResult -> {

            assertTrue(indexResult.succeeded());

            // Give elasticsearch time to index the document
            vertx.setTimer(2000, id -> {
                DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
                        .setTimeout("1000")
                        .setQuery(new JsonObject().put("ids", new JsonObject().put("values", new JsonArray().add(documentId.toString()))));

                service.deleteByQuery(index, deleteByQueryOptions, deleteByQueryResult -> {

                    assertTrue(deleteByQueryResult.succeeded());
                    JsonObject json = deleteByQueryResult.result();
                    System.out.println(json);
                    assertNotNull(json);

                    assertNotNull(json.getJsonObject("_indices"));
                    assertNotNull(json.getJsonObject("_indices").getJsonObject(index));
                    assertNotNull(json.getJsonObject("_indices").getJsonObject("_all"));
                    assertEquals((Integer) 1, json.getJsonObject("_indices").getJsonObject(index).getInteger("found"));
                    assertEquals((Integer) 1, json.getJsonObject("_indices").getJsonObject(index).getInteger("deleted"));
                    assertEquals((Integer) 0, json.getJsonObject("_indices").getJsonObject(index).getInteger("failed"));

                    testComplete();
                });
            });
        });

        await();
    }

    @Test
    public void test99Delete() throws Exception {

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
        return readJson("config.json");
    }

    private JsonObject readJson(String path) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try (Scanner scanner = new Scanner(cl.getResourceAsStream(path)).useDelimiter("\\A")) {
            String s = scanner.next();
            return new JsonObject(s);
        }

    }

}
