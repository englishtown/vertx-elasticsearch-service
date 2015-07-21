package com.englishtown.vertx.elasticsearch.impl;

import com.englishtown.vertx.elasticsearch.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Default implementation of {@link com.englishtown.vertx.elasticsearch.ElasticSearchService}
 */
public class DefaultElasticSearchService implements ElasticSearchService {

    private final TransportClientFactory clientFactory;
    private final ElasticSearchConfigurator configurator;
    protected TransportClient client;

    public static final String CONST_ID = "id";
    public static final String CONST_INDEX = "index";
    public static final String CONST_TYPE = "type";
    public static final String CONST_VERSION = "version";
    public static final String CONST_SOURCE = "source";
    public static final String CONST_CREATED = "created";
    public static final String CONST_IS_CREATED = "isCreated";
    public static final String CONST_SCRIPT_LANG = "lang";

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    @Inject
    public DefaultElasticSearchService(TransportClientFactory clientFactory, ElasticSearchConfigurator configurator) {
        this.clientFactory = clientFactory;
        this.configurator = configurator;
    }

    @Override
    public void start() {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", configurator.getClusterName())
                .put("client.transport.sniff", configurator.getClientTransportSniff())
                .build();

        client = clientFactory.create(settings);
        configurator.getTransportAddresses().forEach(client::addTransportAddress);

    }

    @Override
    public void stop() {
        client.close();
        client = null;
    }

    @Override
    public void index(String index, String type, JsonObject source, IndexOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        IndexRequestBuilder builder = client.prepareIndex(index, type)
                .setSource(source.encode());

        if (options != null) {
            if (options.getId() != null) builder.setId(options.getId());
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.getOpType() != null) builder.setOpType(options.getOpType());
            if (options.isRefresh() != null) builder.setRefresh(options.isRefresh());
            if (options.getConsistencyLevel() != null) builder.setConsistencyLevel(options.getConsistencyLevel());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimestamp() != null) builder.setTimestamp(options.getTimestamp());
            if (options.getTtl() != null) builder.setTTL(options.getTtl());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
        }

        builder.execute(new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                JsonObject result = new JsonObject()
                        .put(CONST_INDEX, indexResponse.getIndex())
                        .put(CONST_TYPE, indexResponse.getType())
                        .put(CONST_ID, indexResponse.getId())
                        .put(CONST_VERSION, indexResponse.getVersion())
                        .put(CONST_CREATED, indexResponse.isCreated());
                resultHandler.handle(Future.succeededFuture(result));
            }

            @Override
            public void onFailure(Throwable t) {
                resultHandler.handle(Future.failedFuture(t));
            }
        });

    }

    @Override
    public void update(String index, String type, String id, UpdateOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        UpdateRequestBuilder builder = client.prepareUpdate(index, type, id);

        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.isRefresh() != null) builder.setRefresh(options.isRefresh());
            if (options.getConsistencyLevel() != null) builder.setConsistencyLevel(options.getConsistencyLevel());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());

            if (options.getScriptLang() != null) builder.setScriptLang(options.getScriptLang());
            if (options.getScriptParams() != null) builder.setScriptParams(options.getScriptParams().getMap());
            if (options.getRetryOnConflict() != null) builder.setRetryOnConflict(options.getRetryOnConflict());
            if (options.getDoc() != null) builder.setDoc(options.getDoc().encode());
            if (options.getUpsert() != null) builder.setUpsert(options.getUpsert().encode());
            if (options.isDocAsUpsert() != null) builder.setDocAsUpsert(options.isDocAsUpsert());
            if (options.isDetectNoop() != null) builder.setDetectNoop(options.isDetectNoop());
            if (options.isScriptedUpsert() != null) builder.setScriptedUpsert(options.isScriptedUpsert());

            if (options.getScript() != null && options.getScriptType() != null) {
                builder.setScript(options.getScript(), options.getScriptType());
            }
            if (!options.getFields().isEmpty()) {
                builder.setFields(options.getFields().toArray(new String[options.getFields().size()]));
            }
        }

        builder.execute(new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                JsonObject result = new JsonObject()
                        .put(CONST_INDEX, updateResponse.getIndex())
                        .put(CONST_TYPE, updateResponse.getType())
                        .put(CONST_ID, updateResponse.getId())
                        .put(CONST_VERSION, updateResponse.getVersion())
                        .put(CONST_CREATED, updateResponse.isCreated());
                resultHandler.handle(Future.succeededFuture(result));
            }

            @Override
            public void onFailure(Throwable e) {
                resultHandler.handle(Future.failedFuture(e));
            }
        });

    }

    @Override
    public void get(String index, String type, String id, GetOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        GetRequestBuilder builder = client.prepareGet(index, type, id);

        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.isRefresh() != null) builder.setRefresh(options.isRefresh());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());

            if (options.getPreference() != null) builder.setPreference(options.getPreference());
            if (!options.getFields().isEmpty()) {
                builder.setFields(options.getFields().toArray(new String[options.getFields().size()]));
            }
            if (options.isFetchSource() != null) builder.setFetchSource(options.isFetchSource());
            if (!options.getFetchSourceIncludes().isEmpty() || !options.getFetchSourceExcludes().isEmpty()) {
                String[] includes = options.getFetchSourceIncludes().toArray(new String[options.getFetchSourceIncludes().size()]);
                String[] excludes = options.getFetchSourceExcludes().toArray(new String[options.getFetchSourceExcludes().size()]);
                builder.setFetchSource(includes, excludes);
            }
            if (options.isTransformSource() != null) builder.setTransformSource(options.isTransformSource());
            if (options.isRealtime() != null) builder.setRealtime(options.isRealtime());
            if (options.isIgnoreErrorsOnGeneratedFields() != null) {
                builder.setIgnoreErrorsOnGeneratedFields(options.isIgnoreErrorsOnGeneratedFields());
            }
        }

        builder.execute(new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                JsonObject source = (getResponse.isExists() ? new JsonObject(getResponse.getSourceAsString()) : null);
                JsonObject reply = new JsonObject()
                        .put(CONST_INDEX, getResponse.getIndex())
                        .put(CONST_TYPE, getResponse.getType())
                        .put(CONST_ID, getResponse.getId())
                        .put(CONST_VERSION, getResponse.getVersion())
                        .put(CONST_SOURCE, source);
                resultHandler.handle(Future.succeededFuture(reply));
            }

            @Override
            public void onFailure(Throwable t) {
                resultHandler.handle(Future.failedFuture(t));
            }
        });

    }

    @Override
    public void search(List<String> indices, SearchOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        SearchRequestBuilder builder = client.prepareSearch(indices.toArray(new String[indices.size()]));

        if (options != null) {
            if (!options.getTypes().isEmpty()) {
                builder.setTypes(options.getTypes().toArray(new String[options.getTypes().size()]));
            }
            if (options.getSearchType() != null) builder.setSearchType(options.getSearchType());
            if (options.getScroll() != null) builder.setScroll(options.getScroll());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
            if (options.getTerminateAfter() != null) builder.setTerminateAfter(options.getTerminateAfter());
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getPreference() != null) builder.setPreference(options.getPreference());
            if (options.getQuery() != null) builder.setQuery(options.getQuery().encode());
            if (options.getPostFilter() != null) builder.setPostFilter(options.getPostFilter().encode());
            if (options.getMinScore() != null) builder.setMinScore(options.getMinScore());
            if (options.getSize() != null) builder.setSize(options.getSize());
            if (options.getFrom() != null) builder.setFrom(options.getFrom());
            if (options.isExplain() != null) builder.setExplain(options.isExplain());
            if (options.isVersion() != null) builder.setVersion(options.isVersion());
            if (options.isFetchSource() != null) builder.setFetchSource(options.isFetchSource());
            if (!options.getFields().isEmpty()) options.getFields().forEach(builder::addField);
            if (options.isTrackScores() != null) builder.setTrackScores(options.isTrackScores());
            if (options.getAggregations() != null) {
                builder.setAggregations(options.getAggregations().encode().getBytes(CHARSET_UTF8));
            }
            if (!options.getSorts().isEmpty()) {
                options.getSorts().forEach(sort -> builder.addSort(sort.getField(), sort.getOrder()));
            }
            if (options.getExtraSource() != null) builder.setExtraSource(options.getExtraSource().encode());
        }

        builder.execute(new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                JsonObject json = readResponse(searchResponse);
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                resultHandler.handle(Future.failedFuture(t));
            }
        });
    }

    @Override
    public void searchScroll(String scrollId, SearchScrollOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        SearchScrollRequestBuilder builder = client.prepareSearchScroll(scrollId);

        if (options != null) {
            if (options.getScroll() != null) builder.setScroll(options.getScroll());
        }

        builder.execute(new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                JsonObject json = readResponse(searchResponse);
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                resultHandler.handle(Future.failedFuture(t));
            }
        });

    }

    @Override
    public void delete(String index, String type, String id, DeleteOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        DeleteRequestBuilder builder = client.prepareDelete(index, type, id);

        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.isRefresh() != null) builder.setRefresh(options.isRefresh());
            if (options.getConsistencyLevel() != null) builder.setConsistencyLevel(options.getConsistencyLevel());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
        }

        builder.execute(new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                JsonObject json = new JsonObject()
                        .put(CONST_INDEX, deleteResponse.getIndex())
                        .put(CONST_TYPE, deleteResponse.getType())
                        .put(CONST_ID, deleteResponse.getId())
                        .put(CONST_VERSION, deleteResponse.getVersion());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                resultHandler.handle(Future.failedFuture(t));
            }
        });

    }

    @Override
    public void putIndexedScript(PutIndexedScriptOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        PutIndexedScriptRequestBuilder builder = client.preparePutIndexedScript();

        if (options != null) {
            if (options.getScriptLang() != null) builder.setScriptLang(options.getScriptLang());
            if (options.getId() != null) builder.setId(options.getId());
            if (options.getSource() != null) builder.setSource(options.getSource().encode().getBytes(CHARSET_UTF8));
        }

        builder.execute(new ActionListener<PutIndexedScriptResponse>() {
            @Override
            public void onResponse(PutIndexedScriptResponse putIndexedScriptResponse) {
                JsonObject json = new JsonObject()
                        .put(CONST_INDEX, putIndexedScriptResponse.getIndex())
                        .put(CONST_SCRIPT_LANG, putIndexedScriptResponse.getScriptLang())
                        .put(CONST_ID, putIndexedScriptResponse.getId())
                        .put(CONST_IS_CREATED, putIndexedScriptResponse.isCreated())
                        .put(CONST_VERSION, putIndexedScriptResponse.getVersion());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                resultHandler.handle(Future.failedFuture(t));
            }
        });
    }

    protected JsonObject readResponse(ToXContent toXContent) {

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            toXContent.toXContent(builder, SearchResponse.EMPTY_PARAMS);
            builder.endObject();

            return new JsonObject(builder.string());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
