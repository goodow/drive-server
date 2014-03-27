package com.goodow.drive.server.bootstrap;

import com.alienos.guice.GuiceVertxBinding;
import com.goodow.drive.server.attachment.DownloadHandler;
import com.goodow.drive.server.attachment.FormUploadHandler;

import com.goodow.realtime.channel.server.ChannelModule;
import com.goodow.realtime.channel.server.ChannelWebServer;

import com.google.inject.Inject;

import com.google.inject.Provider;
import org.elasticsearch.client.Client;
import org.vertx.java.core.http.RouteMatcher;

@GuiceVertxBinding(modules = {ChannelModule.class, DriveModule.class})
public class DriveWebServer extends ChannelWebServer {
  @Inject private Provider<Client> client;
  @Inject private FormUploadHandler formUploadHandler;
  @Inject private DownloadHandler downloadHandler;

  @Override
  protected RouteMatcher routeMatcher() {
    routeMatcher.post("/" + DriveModule.INDEX, formUploadHandler);
    routeMatcher.get("/" + DriveModule.INDEX + "/:id", downloadHandler);

    return super.routeMatcher();
  }

  @Override
  public void stop() {
    super.stop();
    client.get().close();
  }
}
