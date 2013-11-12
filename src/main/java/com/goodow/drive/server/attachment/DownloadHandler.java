package com.goodow.drive.server.attachment;

import com.goodow.drive.server.bootstrap.DriveModule;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class DownloadHandler implements Handler<HttpServerRequest> {
  @Inject @Named(DriveModule.ATTACHMENTS_DIR) private String attachmentsDir;

  @Override
  public void handle(HttpServerRequest req) {
    req.response().sendFile(attachmentsDir + "/" + req.params().get("id"));
  }
}
