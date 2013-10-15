package com.goodow.drive.server.bootstrap;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;

public class DriveVerticle extends BusModBase {
  @Override
  public void start(final Future<Void> startedResult) {
    super.start();

    container.deployVerticle(DriveWebServer.class.getName(), config,
        new AsyncResultHandler<String>() {
          @Override
          public void handle(AsyncResult<String> event) {
            if (event.succeeded()) {
              startedResult.setResult(null);
            } else {
              startedResult.setFailure(event.cause());
            }
          }
        });
  }
}
