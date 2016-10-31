package com.englishtown.vertx.elasticsearch.impl;

import com.englishtown.vertx.elasticsearch.DeleteByQueryOptions;
import com.englishtown.vertx.elasticsearch.DeleteOptions;
import com.englishtown.vertx.elasticsearch.ElasticSearchConfigurator;
import com.englishtown.vertx.elasticsearch.GetOptions;
import com.englishtown.vertx.elasticsearch.IndexOptions;
import com.englishtown.vertx.elasticsearch.SearchOptions;
import com.englishtown.vertx.elasticsearch.SearchScrollOptions;
import com.englishtown.vertx.elasticsearch.SuggestOptions;
import com.englishtown.vertx.elasticsearch.TransportClientFactory;
import com.englishtown.vertx.elasticsearch.UpdateOptions;
import com.englishtown.vertx.elasticsearch.internal.InternalElasticSearchService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.Template;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link com.englishtown.vertx.elasticsearch.ElasticSearchService}
 */
public class DefaultElasticSearchService implements InternalElasticSearchService {

    private final TransportClientFactory clientFactory;
    private final ElasticSearchConfigurator configurator;
    protected TransportClient client;

    public static final String CONST_ID = "id";
    public static final String CONST_INDEX = "index";
    public static final String CONST_TYPE = "type";
    public static final String CONST_VERSION = "version";
    public static final String CONST_SOURCE = "source";
    public static final String CONST_CREATED = "created";

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    @Inject
    public DefaultElasticSearchService(TransportClientFactory clientFactory, ElasticSearchConfigurator configurator) {
        this.clientFactory = clientFactory;
        this.configurator = configurator;
    }

    @Override
    public void start() {

        Settings.setSettingsRequireUnits(configurator.getSettingsRequireUnits());

        Settings settings = Settings.builder()
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
                handleFailure(resultHandler, t);
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

            if (options.getRetryOnConflict() != null) builder.setRetryOnConflict(options.getRetryOnConflict());
            if (options.getDoc() != null) builder.setDoc(options.getDoc().encode());
            if (options.getUpsert() != null) builder.setUpsert(options.getUpsert().encode());
            if (options.isDocAsUpsert() != null) builder.setDocAsUpsert(options.isDocAsUpsert());
            if (options.isDetectNoop() != null) builder.setDetectNoop(options.isDetectNoop());
            if (options.isScriptedUpsert() != null) builder.setScriptedUpsert(options.isScriptedUpsert());

            if (options.getScript() != null) {
                if (options.getScriptType() != null) {
                    Map<String, ? extends Object> params = (options.getScriptParams() == null ? null : options.getScriptParams().getMap());
                    builder.setScript(new Script(options.getScript(), options.getScriptType(), options.getScriptLang(), params));
                } else {
                    builder.setScript(new Script(options.getScript()));
                }
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
                handleFailure(resultHandler, e);
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
                handleFailure(resultHandler, t);
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
            if (options.getTemplateName() != null) {
                if (options.getTemplateType() != null) {
                    Map<String, Object> params = (options.getTemplateParams() == null ? null : options.getTemplateParams().getMap());
                    builder.setTemplate(new Template(options.getTemplateName(), options.getTemplateType(), null, null, params));
                } else {
                    builder.setTemplate(new Template(options.getTemplateName()));
                }
            }
        }

        builder.execute(new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                JsonObject json = readResponse(searchResponse);
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                handleFailure(resultHandler, t);
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
                handleFailure(resultHandler, t);
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
                handleFailure(resultHandler, t);
            }
        });

    }

    @Override
    public void suggest(String index, SuggestOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        final SuggestRequestBuilder builder = client.prepareSuggest(index);

        if (options != null) {
            if (options.getName() != null) {
                final CompletionSuggestionBuilder completionBuilder = new CompletionSuggestionBuilder(options.getName());
                if (options.getText() != null) {
                    completionBuilder.text(options.getText());
                }
                if (options.getField() != null) {
                    completionBuilder.field(options.getField());
                }
                if (options.getSize() != null) {
                    completionBuilder.size(options.getSize());
                }

                builder.addSuggestion(completionBuilder);
            }
        }

        builder.execute(new ActionListener<SuggestResponse>() {

            @Override
            public void onResponse(SuggestResponse suggestResponse) {
                JsonObject json = readResponse(suggestResponse.getSuggest());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                handleFailure(resultHandler, t);
            }
        });

    }

    @Override
    public void deleteByQuery(List<String> indices, DeleteByQueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        final DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
                .setIndices(indices.toArray(new String[indices.size()]));

        if (options != null) {
            if (!options.getTypes().isEmpty()) {
                deleteByQueryRequestBuilder.setTypes(options.getTypes().toArray(new String[options.getTypes().size()]));
            }
            if (options.getTimeout() != null) deleteByQueryRequestBuilder.setTimeout(options.getTimeout());
            if (options.getRouting() != null) deleteByQueryRequestBuilder.setRouting(options.getRouting());
            if (options.getQuery() != null) deleteByQueryRequestBuilder.setSource("{\"query\": " + options.getQuery().encode() + "}");
        }

        deleteByQueryRequestBuilder.execute(new ActionListener<DeleteByQueryResponse>() {
            @Override
            public void onResponse(DeleteByQueryResponse deleteByQueryResponse) {
                JsonObject json = readResponse(deleteByQueryResponse);
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Throwable t) {
                handleFailure(resultHandler, t);
            }
        });
    }

    @Override
    public TransportClient getClient() {
        return client;
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

    private static void handleFailure(final Handler<AsyncResult<JsonObject>> resultHandler, final Throwable t) {
        if (t instanceof ElasticsearchException) {
            final ElasticsearchException esException = (ElasticsearchException) t;
            resultHandler.handle(Future.failedFuture(esException.getDetailedMessage()));
        } else {
            resultHandler.handle(Future.failedFuture(t));
        }
    }

}
