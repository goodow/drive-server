package com.goodow.drive.server.bootstrap;

import com.goodow.drive.server.attachment.DownloadHandler;
import com.goodow.drive.server.attachment.FormUploadHandler;

import com.alienos.guice.GuiceVerticleHelper;
import com.alienos.guice.GuiceVertxBinding;
import com.google.inject.Inject;

import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServer;

@GuiceVertxBinding(modules = {DriveModule.class})
public class DriveWebServer extends WebServer {
  @Inject private FormUploadHandler formUploadHandler;
  @Inject private DownloadHandler downloadHandler;

  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);
    super.start();
    config = config.getObject("web_server");
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher routeMatcher = super.routeMatcher();
    routeMatcher.post("/" + DriveModule.INDEX, formUploadHandler);
    routeMatcher.get("/" + DriveModule.INDEX + "/:id", downloadHandler);

    return routeMatcher;
  }
}
