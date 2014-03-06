package com.goodow.drive.server.analytics;

import com.alienos.guice.GuiceVerticleHelper;
import com.alienos.guice.GuiceVertxBinding;
import com.goodow.drive.server.bootstrap.DriveModule;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.server.ChannelModule;
import com.google.inject.Inject;
import org.elasticsearch.client.Client;
import org.vertx.java.busmods.BusModBase;

@GuiceVertxBinding(modules = {ChannelModule.class, DriveModule.class})
public class PlayerAnalyticsVerticle extends BusModBase {
  @Inject private Bus bus;
  @Inject private Client client;
  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
    client.close();
  }
}
