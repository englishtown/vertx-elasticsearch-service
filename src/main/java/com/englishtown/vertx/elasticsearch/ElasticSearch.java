package com.englishtown.vertx.elasticsearch;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.sort.SortOrder;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * ElasticSearch event bus verticle
 */
public class ElasticSearch extends BusModBase implements Handler<Message<JsonObject>> {

    protected final TransportClientFactory clientFactory;
    private final ElasticSearchConfigurator configurator;
    protected TransportClient client;
    protected String address;

    public static final String CONFIG_ADDRESS = "address";
    public static final String DEFAULT_ADDRESS = "et.vertx.elasticsearch";
    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static final String CONST_ID = "_id";
    public static final String CONST_INDEX = "_index";
    public static final String CONST_INDICES = "_indices";
    public static final String CONST_TYPE = "_type";
    public static final String CONST_VERSION = "_version";
    public static final String CONST_SOURCE = "_source";
    public static final String CONST_SCRIPT = "_script";
    public static final String CONST_SCRIPT_PARAMS = "_params";
    public static final String CONST_SCRIPT_RETRY = "_retry";
    public static final String CONST_SCRIPT_UPSERT = "_upsert";
    public static final String CONST_SCRIPT_LANGUAGE = "_lang";

    @Inject
    public ElasticSearch(TransportClientFactory clientFactory, ElasticSearchConfigurator configurator) {
        if (clientFactory == null) {
            throw new IllegalArgumentException("clientProvider is null");
        }
        this.clientFactory = clientFactory;
        this.configurator = configurator;
    }

    /**
     * Start the busmod
     */
    @Override
    public void start() {
        super.start();

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", configurator.getClusterName())
                .put("client.transport.sniff", configurator.getClientTransportSniff())
                .build();

        client = clientFactory.create(settings);

        for (TransportAddress transportAddress : configurator.getTransportAddresses()) {
            client.addTransportAddress(transportAddress);
        }

        address = config.getString(CONFIG_ADDRESS, DEFAULT_ADDRESS);
        eb.registerHandler(address, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        client.close();
        client = null;
    }

    /**
     * Handle an incoming elastic search message
     */
    @Override
    public void handle(Message<JsonObject> message) {

        try {
            String action = getMandatoryString("action", message);
            if (action == null) {
                return;
            }

            switch (action) {
                case "index":
                    doIndex(message);
                    break;
                case "update":
                    doUpdate(message);
                    break;
                case "get":
                    doGet(message);
                    break;
                case "search":
                    doSearch(message);
                    break;
                case "scroll":
                    doScroll(message);
                    break;
                case "delete":
                    doDelete(message);
                    break;
                default:
                    sendError(message, "Unrecognized action " + action);
                    break;
            }

        } catch (Exception e) {
            sendError(message, "Unhandled exception!", e);
        }

    }

    /**
     * See http://www.elasticsearch.org/guide/reference/api/index_/
     *
     * @param message index event bus message
     */
    public void doIndex(final Message<JsonObject> message) {

        JsonObject body = message.body();

        final String index = getRequiredIndex(body, message);
        if (index == null) {
            return;
        }

        // type is optional
        String type = body.getString(CONST_TYPE);

        JsonObject source = body.getObject(CONST_SOURCE);
        if (source == null) {
            sendError(message, CONST_SOURCE + " is required");
            return;
        }

        // id is optional
        String id = body.getString(CONST_ID);

        client.prepareIndex(index, type, id)
                .setSource(source.encode())
                .execute(new ActionListener<IndexResponse>() {
                    @Override
                    public void onResponse(IndexResponse indexResponse) {
                        JsonObject reply = new JsonObject()
                                .putString(CONST_INDEX, indexResponse.getIndex())
                                .putString(CONST_TYPE, indexResponse.getType())
                                .putString(CONST_ID, indexResponse.getId())
                                .putNumber(CONST_VERSION, indexResponse.getVersion());
                        sendOK(message, reply);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        sendError(message, "Index error: " + e.getMessage(), new RuntimeException(e));
                    }
                });

    }

    /**
     * http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/docs-update.html
     *
     * @param message update event bus message
     */
    public void doUpdate(final Message<JsonObject> message) {

        JsonObject body = message.body();

        final String index = getRequiredIndex(body, message);
        if (index == null) {
            return;
        }

        // type is optional
        final String type = body.getString(CONST_TYPE);

        final String id = body.getString(CONST_ID);

        final String script = body.getString(CONST_SCRIPT);
        if (script == null) {
            sendError(message, CONST_SCRIPT + " is required");
            return;
        }

        final UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(index, type, id)
                .setScript(script, ScriptService.ScriptType.INLINE);

        final JsonObject params = body.getObject(CONST_SCRIPT_PARAMS);

        if ( params != null ) {
            for ( final String fieldName : params.getFieldNames() ) {
                final Object value = params.getValue(fieldName);
                updateRequestBuilder.addScriptParam( fieldName, value );
            }
        }

        if ( body.containsField( CONST_SCRIPT_RETRY ) ) {
            updateRequestBuilder.setRetryOnConflict( body.getInteger( CONST_SCRIPT_RETRY ) );
        }

        if ( body.containsField( CONST_SCRIPT_UPSERT ) ) {
            updateRequestBuilder.setUpsert( body.getObject( CONST_SCRIPT_UPSERT ).encode() );
        }

        if ( body.containsField( CONST_SCRIPT_LANGUAGE ) ) {
            updateRequestBuilder.setScriptLang( body.getString( CONST_SCRIPT_LANGUAGE ) );
        }

        updateRequestBuilder.execute(new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                JsonObject reply = new JsonObject()
                        .putString(CONST_INDEX, updateResponse.getIndex())
                        .putString(CONST_TYPE, updateResponse.getType())
                        .putString(CONST_ID, updateResponse.getId())
                        .putNumber(CONST_VERSION, updateResponse.getVersion());
                sendOK(message, reply);
            }

            @Override
            public void onFailure(Throwable e) {
                sendError(message, "Update error: " + e.getMessage(), new RuntimeException(e));
            }
        });


    }

    /**
     * http://www.elasticsearch.org/guide/reference/java-api/get/
     *
     * @param message get event bus message
     */
    public void doGet(final Message<JsonObject> message) {

        JsonObject body = message.body();

        final String index = getRequiredIndex(body, message);
        if (index == null) {
            return;
        }

        // type is optional
        String type = body.getString(CONST_TYPE);

        String id = getRequiredId(body, message);
        if (id == null) {
            return;
        }

        client.prepareGet(index, type, id)
                .execute(new ActionListener<GetResponse>() {
                    @Override
                    public void onResponse(GetResponse getResponse) {
                        JsonObject source = (getResponse.isExists() ? new JsonObject(getResponse.getSourceAsString()) : null);
                        JsonObject reply = new JsonObject()
                                .putString(CONST_INDEX, getResponse.getIndex())
                                .putString(CONST_TYPE, getResponse.getType())
                                .putString(CONST_ID, getResponse.getId())
                                .putNumber(CONST_VERSION, getResponse.getVersion())
                                .putObject(CONST_SOURCE, source);
                        sendOK(message, reply);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        sendError(message, "Get error: " + e.getMessage(), new RuntimeException(e));
                    }
                });

    }

    /**
     * http://www.elasticsearch.org/guide/reference/api/search/
     * http://www.elasticsearch.org/guide/reference/query-dsl/
     *
     * @param message search event bus message
     */
    public void doSearch(final Message<JsonObject> message) {

        JsonObject body = message.body();

        // Get indices to be searched
        String index = body.getString(CONST_INDEX);
        JsonArray indices = body.getArray(CONST_INDICES);
        List<String> list = new ArrayList<>();
        if (index != null) {
            list.add(index);
        }
        if (indices != null) {
            for (int i = 0; i < indices.size(); i++) {
                list.add(indices.<String>get(i));
            }
        }

        SearchRequestBuilder builder = client.prepareSearch(list.toArray(new String[list.size()]));

        // Get types to be searched
        String type = body.getString(CONST_TYPE);
        JsonArray types = body.getArray("_types");
        list.clear();
        if (type != null) {
            list.add(type);
        }
        if (types != null) {
            for (int i = 0; i < types.size(); i++) {
                list.add(types.<String>get(i));
            }
        }
        if (!list.isEmpty()) {
            builder.setTypes(list.toArray(new String[list.size()]));
        }

        // Set the query
        JsonObject query = body.getObject("query");
        if (query != null) {
            builder.setQuery(query.encode());
        }

        // Set the filter
        JsonObject filter = body.getObject("filter");
        if (filter != null) {
            builder.setPostFilter(filter.encode());
        }

        // Set facets
        JsonObject facets = body.getObject("facets");
        if (facets != null) {
            builder.setFacets(facets.encode().getBytes(CHARSET_UTF8));
        }

        // Set search type
        String searchType = body.getString("search_type");
        if (searchType != null) {
            builder.setSearchType(searchType);
        }

        // Set scroll keep alive time
        String scroll = body.getString("scroll");
        if (scroll != null) {
            builder.setScroll(scroll);
        }

        // Set Size
        Integer size = body.getInteger("size");
        if (size != null) {
            builder.setSize(size);
        }

        // Set From
        Integer from = body.getInteger("from");
        if (from != null) {
            builder.setFrom(from);
        }

        //Set requested fields
        JsonArray fields = body.getArray("fields");
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                builder.addField(fields.<String>get(i));
            }
        }

        // Set Sort fields:
        final JsonObject sort = body.getObject("sort");
        if ( sort != null ) {
            for ( final String fieldName : sort.getFieldNames() ) {
                final JsonObject orderObject = sort.getObject( fieldName );
                final String orderValue = orderObject.getString("order");

                SortOrder order = SortOrder.DESC;
                if ( "asc".equalsIgnoreCase( orderValue ) ) {
                    order = SortOrder.ASC;
                }

                builder.addSort( fieldName, order );
            }
        }

        JsonArray includes = body.getArray("fetch_includes");
        final String[] includeArray;
        if ( includes != null ) {
            final List<Object> includeList = includes.toList();
            includeArray = includeList.stream().map( x -> x.toString() ).toArray( String[]::new );
        } else {
            includeArray = new String[0];
        }

        JsonArray excludes = body.getArray("fetch_excludes");
        final String[] excludeArray;
        if ( excludes != null ) {
            final List<Object> excludeList = excludes.toList();
            excludeArray = excludeList.stream().map( x -> x.toString() ).toArray( String[]::new );
        } else {
            excludeArray = new String[0];
        }

        if ( includeArray.length > 0 || excludeArray.length > 0 ) {
            builder.setFetchSource( includeArray, excludeArray );
        }

        //Set query timeout
        Long queryTimeout = body.getLong("timeout");
        if (queryTimeout != null) {
            builder.setTimeout(new TimeValue(queryTimeout));
        }

        builder.execute(new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                handleActionResponse(searchResponse, message);
            }

            @Override
            public void onFailure(Throwable e) {
                sendError(message, "Search error: " + e.getMessage(), new RuntimeException(e));
            }
        });

    }

    /**
     * http://www.elasticsearch.org/guide/reference/api/search/scroll/
     *
     * @param message scroll event bus message
     */
    public void doScroll(final Message<JsonObject> message) {

        JsonObject body = message.body();
        String scrollId = body.getString("_scroll_id");
        if (scrollId == null) {
            sendError(message, "_scroll_id is required");
            return;
        }

        String scroll = body.getString("scroll");
        if (scroll == null) {
            sendError(message, "scroll is required");
            return;
        }

        client.prepareSearchScroll(scrollId)
                .setScroll(scroll)
                .execute(new ActionListener<SearchResponse>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        handleActionResponse(searchResponse, message);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        sendError(message, "Scroll error: " + e.getMessage(), new RuntimeException(e));
                    }
                });

    }

    /**
     * http://www.elasticsearch.org/guide/reference/java-api/delete/
     *
     * @param message delete event bus message
     */
    public void doDelete(final Message<JsonObject> message) {

        JsonObject body = message.body();

        final String index = getRequiredIndex(body, message);
        if (index == null) {
            return;
        }

        final String type = getRequiredType(body, message);
        if (type == null) {
            return;
        }
        final String id = getRequiredId(body, message);
        if (id == null) {
            return;
        }

        client.prepareDelete(index, type, id)
                .execute(new ActionListener<DeleteResponse>() {
                    @Override
                    public void onResponse(DeleteResponse deleteResponse) {
                        JsonObject reply = new JsonObject()
                                .putString(CONST_INDEX, deleteResponse.getIndex())
                                .putString(CONST_TYPE, deleteResponse.getType())
                                .putString(CONST_ID, deleteResponse.getId())
                                .putNumber(CONST_VERSION, deleteResponse.getVersion());
                        sendOK(message, reply);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        sendError(message, "Get error: " + e.getMessage(), new RuntimeException(e));
                    }
                });

    }

    protected String getRequiredIndex(JsonObject json, Message<JsonObject> message) {
        String index = json.getString(CONST_INDEX);
        if (index == null || index.isEmpty()) {
            sendError(message, CONST_INDEX + " is required");
            return null;
        }
        return index;
    }

    protected String getRequiredType(JsonObject json, Message<JsonObject> message) {
        String type = json.getString(CONST_TYPE);
        if (type == null || type.isEmpty()) {
            sendError(message, CONST_TYPE + " is required");
            return null;
        }
        return type;
    }

    protected String getRequiredId(JsonObject json, Message<JsonObject> message) {
        String id = json.getString(CONST_ID);
        if (id == null || id.isEmpty()) {
            sendError(message, CONST_ID + " is required");
            return null;
        }
        return id;
    }

    protected void handleActionResponse(ToXContent toXContent, Message<JsonObject> message) {

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            toXContent.toXContent(builder, SearchResponse.EMPTY_PARAMS);
            builder.endObject();

            JsonObject response = new JsonObject(builder.string());
            sendOK(message, response);

        } catch (IOException e) {
            sendError(message, "Error reading search response: " + e.getMessage(), e);
        }

    }
}
