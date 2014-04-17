package com.goodow.drive.server.bootstrap;

import com.goodow.drive.server.analytics.PlayerAnalyticsVerticle;
import com.goodow.drive.server.geo.GeoAuthenticate;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;

public class DriveVerticle extends BusModBase {
  private int count;

  @Override
  public void start(final Future<Void> startedResult) {
    super.start();
    AsyncResultHandler<String> doneHandler = new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.succeeded()) {
          count++;
          if (count == 4) {
            startedResult.setResult(null);
          }
        } else {
          startedResult.setFailure(ar.cause());
        }
      }
    };
    container
        .deployModule("com.goodow.realtime~realtime-store~0.5.5-SNAPSHOT", config, doneHandler);

    container.deployVerticle(DriveWebServer.class.getName(), config, doneHandler);
    container.deployVerticle(PlayerAnalyticsVerticle.class.getName(), config, doneHandler);
    container.deployVerticle(GeoAuthenticate.class.getName(), config, doneHandler);
  }
}
