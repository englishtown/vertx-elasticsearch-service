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
import com.englishtown.vertx.elasticsearch.DeleteOptions;
import io.vertx.rxjava.core.Vertx;
import com.englishtown.vertx.elasticsearch.GetOptions;
import java.util.List;
import com.englishtown.vertx.elasticsearch.UpdateOptions;
import com.englishtown.vertx.elasticsearch.SearchOptions;
import com.englishtown.vertx.elasticsearch.IndexOptions;
import com.englishtown.vertx.elasticsearch.SuggestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import com.englishtown.vertx.elasticsearch.SearchScrollOptions;

/**
 * ElasticSearch service
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link com.englishtown.vertx.elasticsearch.ElasticSearchService original} non RX-ified interface using Vert.x codegen.
 */

public class ElasticSearchService {

  final com.englishtown.vertx.elasticsearch.ElasticSearchService delegate;

  public ElasticSearchService(com.englishtown.vertx.elasticsearch.ElasticSearchService delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public static ElasticSearchService createEventBusProxy(Vertx vertx, String address) { 
    ElasticSearchService ret= ElasticSearchService.newInstance(com.englishtown.vertx.elasticsearch.ElasticSearchService.createEventBusProxy((io.vertx.core.Vertx) vertx.getDelegate(), address));
    return ret;
  }

  public void start() { 
    this.delegate.start();
  }

  public void stop() { 
    this.delegate.stop();
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html
   * @param index the index name
   * @param type the type name
   * @param source the source to be indexed
   * @param options optional index options (id, timeout, ttl, etc.)
   * @param resultHandler result handler callback
   */
  public void index(String index, String type, JsonObject source, IndexOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.index(index, type, source, options, resultHandler);
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html
   * @param index the index name
   * @param type the type name
   * @param source the source to be indexed
   * @param options optional index options (id, timeout, ttl, etc.)
   * @return 
   */
  public Observable<JsonObject> indexObservable(String index, String type, JsonObject source, IndexOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    index(index, type, source, options, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/java-update-api.html
   * @param index the index name
   * @param type the type name
   * @param id the source id to update
   * @param options the update options (doc, script, etc.)
   * @param resultHandler result handler callback
   */
  public void update(String index, String type, String id, UpdateOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.update(index, type, id, options, resultHandler);
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/java-update-api.html
   * @param index the index name
   * @param type the type name
   * @param id the source id to update
   * @param options the update options (doc, script, etc.)
   * @return 
   */
  public Observable<JsonObject> updateObservable(String index, String type, String id, UpdateOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    update(index, type, id, options, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html
   * @param index the index name
   * @param type the type name
   * @param id the source id to update
   * @param options the update options
   * @param resultHandler result handler callback
   */
  public void get(String index, String type, String id, GetOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.get(index, type, id, options, resultHandler);
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html
   * @param index the index name
   * @param type the type name
   * @param id the source id to update
   * @param options the update options
   * @return 
   */
  public Observable<JsonObject> getObservable(String index, String type, String id, GetOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    get(index, type, id, options, resultHandler.toHandler());
    return resultHandler;
  }

  public void search(List<String> indices, SearchOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.search(indices, options, resultHandler);
  }

  public Observable<JsonObject> searchObservable(List<String> indices, SearchOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    search(indices, options, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/reference/1.4/search-request-scroll.html
   * @param scrollId 
   * @param options 
   * @param resultHandler 
   */
  public void searchScroll(String scrollId, SearchScrollOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.searchScroll(scrollId, options, resultHandler);
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/reference/1.4/search-request-scroll.html
   * @param scrollId 
   * @param options 
   * @return 
   */
  public Observable<JsonObject> searchScrollObservable(String scrollId, SearchScrollOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    searchScroll(scrollId, options, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html
   * @param index the index name
   * @param type the type name
   * @param id the source id to delete
   * @param options optional delete options (timeout, etc.)
   * @param resultHandler result handler callback
   */
  public void delete(String index, String type, String id, DeleteOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.delete(index, type, id, options, resultHandler);
  }

  /**
   * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html
   * @param index the index name
   * @param type the type name
   * @param id the source id to delete
   * @param options optional delete options (timeout, etc.)
   * @return 
   */
  public Observable<JsonObject> deleteObservable(String index, String type, String id, DeleteOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    delete(index, type, id, options, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html
   * @param index the index name
   * @param options optional suggest options
   * @param resultHandler result handler callback
   */
  public void suggest(String index, SuggestOptions options, Handler<AsyncResult<JsonObject>> resultHandler) { 
    this.delegate.suggest(index, options, resultHandler);
  }

  /**
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html
   * @param index the index name
   * @param options optional suggest options
   * @return 
   */
  public Observable<JsonObject> suggestObservable(String index, SuggestOptions options) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    suggest(index, options, resultHandler.toHandler());
    return resultHandler;
  }


  public static ElasticSearchService newInstance(com.englishtown.vertx.elasticsearch.ElasticSearchService arg) {
    return arg != null ? new ElasticSearchService(arg) : null;
  }
}
