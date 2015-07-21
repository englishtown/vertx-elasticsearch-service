package com.englishtown.vertx.elasticsearch.impl;

import com.englishtown.vertx.elasticsearch.ElasticSearchConfigurator;
import io.vertx.core.Vertx;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * ElasticSearch configuration is read from JSON
 */
public class JsonElasticSearchConfigurator implements ElasticSearchConfigurator {

    protected String clusterName;
    protected boolean clientTransportSniff;
    protected final List<TransportAddress> transportAddresses = new ArrayList<>();

    public static final String CONFIG_NAME = "elasticsearch";
    public static final String CONFIG_TRANSPORT_ADDRESSES = "transportAddresses";
    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_PORT = "port";

    @Inject
    public JsonElasticSearchConfigurator(Vertx vertx) {
        this(getConfig(vertx));
    }

    public JsonElasticSearchConfigurator(JsonObject config) {
        if (config == null) {
            throw new RuntimeException("JSON config was null");
        }
        init(config);
    }

    private static JsonObject getConfig(Vertx vertx) {
        JsonObject config = vertx.getOrCreateContext().config();

        if (config.containsKey(CONFIG_NAME)) {
            config = config.getJsonObject(CONFIG_NAME);
        }

        return config;
    }

    protected void init(JsonObject config) {
        initClusterName(config);
        initClientTransportSniff(config);
        initTransportAddresses(config);
    }

    protected void initClusterName(JsonObject config) {
        clusterName = config.getString("cluster_name", "elasticsearch");
    }

    protected void initClientTransportSniff(JsonObject config) {
        clientTransportSniff = config.getBoolean("client_transport_sniff", true);
    }

    protected void initTransportAddresses(JsonObject config) {

        JsonArray jsonArray = config.getJsonArray(CONFIG_TRANSPORT_ADDRESSES);
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject transportAddress = jsonArray.getJsonObject(i);
                String hostname = transportAddress.getString(CONFIG_HOSTNAME);

                if (hostname != null && !hostname.isEmpty()) {
                    int port = transportAddress.getInteger(CONFIG_PORT, 9300);
                    transportAddresses.add(new InetSocketTransportAddress(hostname, port));
                }
            }
        }

        // If no addresses are configured, add local host on the default port
        if (transportAddresses.size() == 0) {
            transportAddresses.add(new InetSocketTransportAddress("localhost", 9300));
        }

    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public boolean getClientTransportSniff() {
        return clientTransportSniff;
    }

    @Override
    public List<TransportAddress> getTransportAddresses() {
        return transportAddresses;
    }
}
