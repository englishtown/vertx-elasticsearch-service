package com.englishtown.vertx.elasticsearch.impl;

import com.englishtown.vertx.elasticsearch.TransportClientFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;

/**
 * Default implementation of {@link TransportClientFactory}
 */
public class DefaultTransportClientFactory implements TransportClientFactory {
    /**
     * Create a client from the settings
     *
     * @param settings the settings used to create the client
     * @return transport client
     */
    @Override
    public TransportClient create(Settings settings) {
        return TransportClient.builder().addPlugin(DeleteByQueryPlugin.class).settings(settings).build();
    }
}
