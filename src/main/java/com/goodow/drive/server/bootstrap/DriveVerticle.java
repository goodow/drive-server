package com.goodow.drive.server.bootstrap;

import com.goodow.drive.server.analytics.PlayerAnalyticsVerticle;
import com.goodow.drive.server.geo.GeoAuthenticate;

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

    container.deployModule("com.goodow.realtime~realtime-store~0.5.5-SNAPSHOT", config);

    container.deployVerticle(PlayerAnalyticsVerticle.class.getName(), config);
    container.deployVerticle(GeoAuthenticate.class.getName(), config);
  }
}
