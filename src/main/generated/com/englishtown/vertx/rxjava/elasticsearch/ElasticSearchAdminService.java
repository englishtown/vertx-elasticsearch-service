/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.englishtown.vertx.rxjava.elasticsearch;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import java.util.List;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import com.englishtown.vertx.elasticsearch.MappingOptions;

/**
 * Admin service
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link com.englishtown.vertx.elasticsearch.ElasticSearchAdminService original} non RX-ified interface using Vert.x codegen.
 */

public class ElasticSearchAdminService {

  final com.englishtown.vertx.elasticsearch.ElasticSearchAdminService delegate;

  public ElasticSearchAdminService(com.englishtown.vertx.elasticsearch.ElasticSearchAdminService delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public static ElasticSearchAdminService createEventBusProxy(Vertx vertx, String address) { 
    ElasticSearchAdminService ret= ElasticSearchAdminService.newInstance(com.englishtown.vertx.elasticsearch.ElasticSearchAdminService.createEventBusProxy((io.vertx.core.Vertx) vertx.getDelegate(), address));
    return ret;
  }

  public void putMapping(List<String> indices, String type, JsonObject source, MappingOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.putMapping(indices, type, source, options, resultHandler);
  }

  public Observable<JsonObject> putMappingObservable(List<String> indices, String type, JsonObject source, MappingOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    putMapping(indices, type, source, options, resultHandler.toHandler());
    return resultHandler;
  }


  public static ElasticSearchAdminService newInstance(com.englishtown.vertx.elasticsearch.ElasticSearchAdminService arg) {
    return arg != null ? new ElasticSearchAdminService(arg) : null;
  }
}
