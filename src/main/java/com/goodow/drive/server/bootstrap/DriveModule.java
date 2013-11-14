package com.goodow.drive.server.bootstrap;

import com.goodow.realtime.operation.id.IdGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.mods.web.WebServerBase;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriveModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(DriveModule.class.getName());
  public static final String INDEX = "attachment";
  public static final String READABLE_TYPE = "readable";
  public static final String BINARY_TYPE = "binary";
  public static final String ATTACHMENTS_DIR = "attachments_dir";
  public static final String WEB_ROOT = "web_root";
  @Inject private Vertx vertx;
  @Inject private Container container;

  @Override
  protected void configure() {
    requestInjection(this);
  }

  @Named(ATTACHMENTS_DIR)
  @Provides
  @Singleton
  String provideattachmentsDir(@Named("web_root") String webRoot) {
    String attachmentsDir = webRoot + "/attachments";
    if (!vertx.fileSystem().existsSync(attachmentsDir)) {
      vertx.fileSystem().mkdirSync(attachmentsDir, true);
    }
    return attachmentsDir;
  }

  @Provides
  @Singleton
  Client provideElasticSearchClient() {
    JsonObject config =
        container.config().getObject("elasticsearch").getObject("client").getObject("transport");
    TransportClient client =
        new TransportClient().addTransportAddress(new InetSocketTransportAddress(config.getString(
            "host", "localhost"), config.getInteger("port", 9300)));
    if (!client.admin().indices().exists(Requests.indicesExistsRequest(INDEX)).actionGet()
        .isExists()) {
      try {
        String mapping = Streams.copyToStringFromClasspath("/index-settings.json");
        client.admin().indices().create(Requests.createIndexRequest(INDEX).source(mapping))
            .actionGet();
      } catch (IOException e) {
        log.log(Level.SEVERE, "Failed to read index-settings.json", e);
      }
    }
    return client;
    // return NodeBuilder.nodeBuilder().node().client();
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
    String webRoot = container.config().getString("web_root");
    return webRoot == null ? WebServerBase.DEFAULT_WEB_ROOT : webRoot;
  }
}
