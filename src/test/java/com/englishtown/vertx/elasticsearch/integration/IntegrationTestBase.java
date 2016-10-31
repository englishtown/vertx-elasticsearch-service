package com.englishtown.vertx.elasticsearch.integration;

import com.englishtown.vertx.elasticsearch.AbstractVertxIntegrationTest;
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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Scanner;
import java.util.UUID;

import static com.englishtown.vertx.elasticsearch.VertxMatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * {@link com.englishtown.vertx.elasticsearch.ElasticSearchServiceVerticle} integration test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class IntegrationTestBase extends AbstractVertxIntegrationTest {

    private ElasticSearchService service;
    private ElasticSearchAdminService adminService;
    private String id = "integration-test-1";
    private String index = "test_index";
    private String type = "test_type";
    private String source_user = "englishtown";
    private String source_message = "vertx elastic search";

    protected JsonObject config;

    protected abstract String getVerticleName();
    protected abstract void configure(JsonObject config);

    @Before
    public void setUp(TestContext testContext) throws Exception {
        config = readConfig();
        configure(config);

        deployVerticle(testContext, getVerticleName(), new DeploymentOptions().setConfig(config));

        service = ElasticSearchService.createEventBusProxy(vertx, "et.elasticsearch");
        adminService = ElasticSearchAdminService.createEventBusProxy(vertx, "et.elasticsearch.admin");
    }

    @After
    public void tearDown() throws Exception {
        service.stop();
        destroyVerticle();
    }

    @Test
    public void test1Index(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        IndexOptions options = new IndexOptions().setId(id);
        service.index(index, type, source, options, result -> {

            assertThat(testContext, result.succeeded(), is(true));
            JsonObject json = result.result();

            assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_INDEX), is(index));
            assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_TYPE), is(type));
            assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_ID), is(id));
            assertThat(testContext, json.getInteger(DefaultElasticSearchService.CONST_VERSION, 0), greaterThan(0));

            // Give elasticsearch time to index the document
            vertx.setTimer(1000, id -> async.complete());

        });
    }

    @Test
    public void test2Get(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        service.get(index, type, id, result -> {

            assertThat(testContext, result.succeeded(), is(true));
            JsonObject body = result.result();

            assertThat(testContext, body.getString(DefaultElasticSearchService.CONST_INDEX), is(index));
            assertThat(testContext, body.getString(DefaultElasticSearchService.CONST_TYPE), is(type));
            assertThat(testContext, body.getString(DefaultElasticSearchService.CONST_ID), is(id));
            assertThat(testContext, body.getInteger(DefaultElasticSearchService.CONST_VERSION, 0), greaterThan(0));

            JsonObject source = body.getJsonObject(DefaultElasticSearchService.CONST_SOURCE);
            assertThat(testContext, source, notNullValue());
            assertThat(testContext, source.getString("user"), is(source_user));
            assertThat(testContext, source.getString("message"), is(source_message));

            async.complete();

        });
    }

    @Test
    public void test3Search_Simple(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        SearchOptions options = new SearchOptions()
                .setTimeout("1000")
                .setSize(10)
                .setFrom(10)
                .addField("user")
                .addField("message")
                .addSort("user", SortOrder.DESC)
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        service.search(index, options, result -> {

            assertThat(testContext, result.succeeded(), is(true));
            JsonObject json = result.result();
            assertThat(testContext, json, notNullValue());
            async.complete();

        });
    }

    @Test
    public void test4Search_EventBus_Invalid_Enum(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject json = new JsonObject()
                .put("indices", new JsonArray().add(index))
                .put("options", new JsonObject().put("templateType", "invalid_type"));

        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "search");

        vertx.eventBus().<JsonObject>send("et.elasticsearch", json, options, res -> {
            assertThat(testContext, res.failed(), is(true));
            Throwable t = res.cause();
            assertThat(testContext, t, instanceOf(ReplyException.class));
            async.complete();
        });
    }

    @Test
    public void test5Scroll(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        SearchOptions options = new SearchOptions()
                .setSearchType(SearchType.SCAN)
                .setScroll("5m")
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        service.search(index, options, result1 -> {

            assertThat(testContext, result1.succeeded(), is(true));
            JsonObject json = result1.result();

            String scrollId = json.getString("_scroll_id");
            assertThat(testContext, scrollId, notNullValue());

            SearchScrollOptions scrollOptions = new SearchScrollOptions().setScroll("5m");

            service.searchScroll(scrollId, scrollOptions, result2 -> {

                assertThat(testContext, result2.succeeded(), is(true));
                JsonObject json2 = result2.result();

                JsonObject hits = json2.getJsonObject("hits");
                assertThat(testContext, hits, notNullValue());
                JsonArray hitsArray = hits.getJsonArray("hits");
                assertThat(testContext, hitsArray, notNullValue());
                assertThat(testContext, hitsArray.size(), greaterThan(0));

                async.complete();
            });

        });
    }

    @Test
    public void test6Suggest(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject mapping = readJson("mapping.json");

        adminService.putMapping(index, type, mapping, result1 -> {

            assertThat(testContext, result1.succeeded(), is(true));

            JsonObject source = new JsonObject()
                    .put("user", source_user)
                    .put("message", source_message)
                    .put("message_suggest", source_message);


            service.index(index, type, source, result2 -> {

                assertThat(testContext, result2.succeeded(), is(true));

                // Delay 1s to give time for indexing
                vertx.setTimer(1000, id -> {
                    SuggestOptions options = new SuggestOptions();
                    options.setText("v");
                    options.setField("message_suggest");
                    options.setName("test-suggest");

                    service.suggest(index, options, result3 -> {

                        assertThat(testContext, result3.succeeded(), is(true));
                        JsonObject json = result3.result();

                        assertThat(testContext, json.getJsonArray("test-suggest"), notNullValue());
                        assertThat(testContext, json.getJsonArray("test-suggest").getJsonObject(0), notNullValue());
                        assertThat(testContext, json.getJsonArray("test-suggest").getJsonObject(0).getInteger("length"), is(1));
                        assertThat(testContext, json.getJsonArray("test-suggest").getJsonObject(0).getJsonArray("options").getJsonObject(0).getString("text"), is(source_message));

                        async.complete();
                    });
                });

            });
        });
    }

    @Test
    public void test7DeleteByQuery_Simple(TestContext testContext) throws Exception {

        final Async async = testContext.async();
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

            assertThat(testContext, indexResult.succeeded(), is(true));

            // Give elasticsearch time to index the document
            vertx.setTimer(2000, id -> {
                DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
                        .setTimeout("1000")
                        .setQuery(new JsonObject().put("ids", new JsonObject().put("values", new JsonArray().add(documentId.toString()))));

                service.deleteByQuery(index, deleteByQueryOptions, deleteByQueryResult -> {

                    assertThat(testContext, deleteByQueryResult.succeeded(), is(true));
                    JsonObject json = deleteByQueryResult.result();
                    System.out.println(json);
                    assertThat(testContext, json, notNullValue());

                    assertThat(testContext, json.getJsonObject("_indices"), notNullValue());
                    assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index), notNullValue());
                    assertThat(testContext, json.getJsonObject("_indices").getJsonObject("_all"), notNullValue());
                    assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index).getInteger("found"), is(1));
                    assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index).getInteger("deleted"), is(1));
                    assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index).getInteger("failed"), is(0));

                    async.complete();
                });
            });
        });
    }

    @Test
    public void test99Delete(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        service.delete(index, type, id, result -> {

            assertThat(testContext, result.succeeded(), is(true));
            JsonObject json = result.result();

            assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_INDEX), is(index));
            assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_TYPE), is(type));
            assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_ID), is(id));
            assertThat(testContext, json.getInteger(DefaultElasticSearchService.CONST_VERSION, 0), greaterThan(0));

            async.complete();

        });
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
