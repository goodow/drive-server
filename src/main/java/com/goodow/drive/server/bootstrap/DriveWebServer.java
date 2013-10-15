package com.goodow.drive.server.bootstrap;

import com.goodow.drive.server.attachment.FormUploadHandler;

import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServer;
import org.vertx.mods.web.WebServerBase;

public class DriveWebServer extends WebServer {
  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher routeMatcher = super.routeMatcher();

    routeMatcher.post("/upload", new FormUploadHandler(getOptionalStringConfig("web_root",
        WebServerBase.DEFAULT_WEB_ROOT)));
    return routeMatcher;
  }
}
