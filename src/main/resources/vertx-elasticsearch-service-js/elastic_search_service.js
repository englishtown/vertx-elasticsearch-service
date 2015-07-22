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

/** @module vertx-elasticsearch-service-js/elastic_search_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JElasticSearchService = com.englishtown.vertx.elasticsearch.ElasticSearchService;
var IndexOptions = com.englishtown.vertx.elasticsearch.IndexOptions;
var UpdateOptions = com.englishtown.vertx.elasticsearch.UpdateOptions;
var GetOptions = com.englishtown.vertx.elasticsearch.GetOptions;
var SearchOptions = com.englishtown.vertx.elasticsearch.SearchOptions;
var SearchScrollOptions = com.englishtown.vertx.elasticsearch.SearchScrollOptions;
var DeleteOptions = com.englishtown.vertx.elasticsearch.DeleteOptions;
var PutIndexedScriptOptions = com.englishtown.vertx.elasticsearch.PutIndexedScriptOptions;

/**
 ElasticSearch service

 @class
*/
var ElasticSearchService = function(j_val) {

  var j_elasticSearchService = j_val;
  var that = this;

  /**

   @public

   */
  this.start = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_elasticSearchService["start()"]();
    } else utils.invalidArgs();
  };

  /**

   @public

   */
  this.stop = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_elasticSearchService["stop()"]();
    } else utils.invalidArgs();
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param source {Object} the source to be indexed 
   @param options {Object} optional index options (id, timeout, ttl, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.index = function(index, type, source, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'object' && typeof __args[3] === 'object' && typeof __args[4] === 'function') {
      j_elasticSearchService["index(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,com.englishtown.vertx.elasticsearch.IndexOptions,io.vertx.core.Handler)"](index, type, utils.convParamJsonObject(source), options != null ? new IndexOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/java-update-api.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param id {string} the source id to update 
   @param options {Object} the update options (doc, script, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.update = function(index, type, id, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'object' && typeof __args[4] === 'function') {
      j_elasticSearchService["update(java.lang.String,java.lang.String,java.lang.String,com.englishtown.vertx.elasticsearch.UpdateOptions,io.vertx.core.Handler)"](index, type, id, options != null ? new UpdateOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param id {string} the source id to update 
   @param options {Object} the update options 
   @param resultHandler {function} result handler callback 
   */
  this.get = function(index, type, id, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'object' && typeof __args[4] === 'function') {
      j_elasticSearchService["get(java.lang.String,java.lang.String,java.lang.String,com.englishtown.vertx.elasticsearch.GetOptions,io.vertx.core.Handler)"](index, type, id, options != null ? new GetOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param indices {Array.<string>} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.search = function(indices, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0] instanceof Array && typeof __args[1] === 'object' && typeof __args[2] === 'function') {
      j_elasticSearchService["search(java.util.List,com.englishtown.vertx.elasticsearch.SearchOptions,io.vertx.core.Handler)"](indices, options != null ? new SearchOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/reference/1.4/search-request-scroll.html

   @public
   @param scrollId {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.searchScroll = function(scrollId, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && typeof __args[2] === 'function') {
      j_elasticSearchService["searchScroll(java.lang.String,com.englishtown.vertx.elasticsearch.SearchScrollOptions,io.vertx.core.Handler)"](scrollId, options != null ? new SearchScrollOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html

   @public
   @param index {string} the index name 
   @param type {string} the type name 
   @param id {string} the source id to delete 
   @param options {Object} optional delete options (timeout, etc.) 
   @param resultHandler {function} result handler callback 
   */
  this.delete = function(index, type, id, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'object' && typeof __args[4] === 'function') {
      j_elasticSearchService["delete(java.lang.String,java.lang.String,java.lang.String,com.englishtown.vertx.elasticsearch.DeleteOptions,io.vertx.core.Handler)"](index, type, id, options != null ? new DeleteOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Indexes a script on elasticsearch

   @public
   @param options {Object} the options for the script 
   @param resultHandler {function} result handler callback 
   */
  this.putIndexedScript = function(options, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && typeof __args[1] === 'function') {
      j_elasticSearchService["putIndexedScript(com.englishtown.vertx.elasticsearch.PutIndexedScriptOptions,io.vertx.core.Handler)"](options != null ? new PutIndexedScriptOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_elasticSearchService;
};

/**

 @memberof module:vertx-elasticsearch-service-js/elastic_search_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {ElasticSearchService}
 */
ElasticSearchService.createEventBusProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JElasticSearchService["createEventBusProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), ElasticSearchService);
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = ElasticSearchService;