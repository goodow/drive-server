package com.goodow.drive.server.attachment;

import com.google.inject.Inject;

import org.elasticsearch.client.Client;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class SearchHandler implements Handler<HttpServerRequest> {
  @Inject Client client;

  @Override
  public void handle(HttpServerRequest req) {
    // client.prepareSearch(indices)
  }

}
