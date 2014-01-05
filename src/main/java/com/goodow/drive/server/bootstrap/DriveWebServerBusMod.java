package com.goodow.drive.server.bootstrap;

import com.goodow.drive.server.attachment.DownloadHandler;
import com.goodow.drive.server.attachment.FormUploadHandler;

import com.google.inject.Inject;

import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServer;

public class DriveWebServerBusMod extends WebServer {
  @Inject private FormUploadHandler formUploadHandler;
  @Inject private DownloadHandler downloadHandler;

  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher routeMatcher = super.routeMatcher();

    routeMatcher.post("/" + DriveModule.INDEX, formUploadHandler);
    routeMatcher.get("/" + DriveModule.INDEX + "/:id", downloadHandler);
    return routeMatcher;
  }
}
