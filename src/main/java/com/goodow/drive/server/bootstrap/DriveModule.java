package com.goodow.drive.server.bootstrap;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.server.VertxBus;
import com.goodow.realtime.channel.util.IdGenerator;

import com.alienos.guice.VertxModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.mods.web.WebServerBase;

import java.util.logging.Logger;

public class DriveModule extends AbstractModule implements VertxModule {
  private static final Logger log = Logger.getLogger(DriveModule.class.getName());
  public static final String INDEX = "attachment";
  public static final String READABLE_TYPE = "readable";
  public static final String BINARY_TYPE = "binary";
  public static final String ATTACHMENTS_DIR = "attachments_dir";
  public static final String WEB_ROOT = "web_root";
  private Vertx vertx;
  private Container container;

  @Override
  public void setContainer(Container container) {
    this.container = container;
  }

  @Override
  public void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  protected void configure() {
  }

  @Named(ATTACHMENTS_DIR)
  @Provides
  @Singleton
  String provideAttachmentsDir(@Named("web_root") String webRoot) {
    String attachmentsDir = webRoot + "/attachments";
    if (!vertx.fileSystem().existsSync(attachmentsDir)) {
      vertx.fileSystem().mkdirSync(attachmentsDir, true);
    }
    return attachmentsDir;
  }

  @Provides
  @Singleton
  Bus provideBus() {
    return new VertxBus(vertx.eventBus());
  }

  @Provides
  @Singleton
  IdGenerator provideIdGenerator() {
    return new IdGenerator();
  }

  @Named(WEB_ROOT)
  @Provides
  @Singleton
  String provideWebRoot() {
    String webRoot = container.config().getObject("web_server").getString("web_root");
    return webRoot == null ? WebServerBase.DEFAULT_WEB_ROOT : webRoot;
  }
}
