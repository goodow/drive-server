package com.goodow.drive.server.bootstrap;

import com.goodow.drive.server.attachment.AttachmentInfo;
import com.goodow.drive.server.terminal.AuthTerminal;
import com.goodow.drive.server.terminal.PlayerAnalytics;
import com.goodow.drive.server.terminal.SystimeAnalytics;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;

public class DriveVerticle extends BusModBase {
  private int countDownLatch = 6;

  @Override
  public void start(final Future<Void> startedResult) {
    super.start();
    AsyncResultHandler<String> doneHandler = new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.failed()) {
          startedResult.setFailure(ar.cause());
        } else if (ar.succeeded() && --countDownLatch == 0) {
          startedResult.setResult(null);
        }
      }
    };
    container
        .deployModule("com.goodow.realtime~realtime-store~0.5.5-SNAPSHOT", config, doneHandler);

    container.deployVerticle(DriveWebServer.class.getName(), config, doneHandler);
    container.deployVerticle(AttachmentInfo.class.getName(), config, doneHandler);
    container.deployVerticle(PlayerAnalytics.class.getName(), config, doneHandler);
    container.deployVerticle(SystimeAnalytics.class.getName(), config, doneHandler);
    container.deployVerticle(AuthTerminal.class.getName(), config, doneHandler);
  }
}
