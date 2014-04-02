package com.goodow.drive.server.analytics;

import com.goodow.drive.server.bootstrap.DriveModule;
import com.goodow.drive.server.utils.DateUtil;
import com.goodow.drive.server.utils.MyConstant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import com.google.inject.Inject;

import com.alienos.guice.GuiceVerticleHelper;
import com.alienos.guice.GuiceVertxBinding;

import org.vertx.java.busmods.BusModBase;

@GuiceVertxBinding(modules = {DriveModule.class})
public class PlayerAnalyticsVerticle extends BusModBase {
  @Inject
  private Bus bus;

  private static final String ADDR_PLAYER_ANALYTICS = MyConstant.ADDR + MyConstant.ADDR_PLAYER;
  private static final String ES_INDEX = MyConstant.ES_INDEX;
  private static final String ES_TYPE = MyConstant.ES_TYPE_T_PLAYER;

  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);

    bus.registerHandler(ADDR_PLAYER_ANALYTICS, new MessageHandler<JsonObject>() {
      @Override
      public void handle(final Message<JsonObject> message) {

        final JsonObject msg =
            Json.createObject().set("action", "index").set("_index", ES_INDEX)
                .set("_type", ES_TYPE).set("_source",
                    message.body().set("post_date", DateUtil.getDate()));

        bus.send("realtime.elasticsearch", msg, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> messageDb) {
            // 返回最大值
            Long max = 0L;
            JsonArray analytics = message.body().getArray("analytics");
            for (int n = 0; n < analytics.length(); n++) {
              JsonObject analyticJson = (JsonObject) analytics.get(n);
              Long maxTimestamp =
                  analyticJson.getArray("timestamp").get(
                      analyticJson.getArray("timestamp").length() - 1);
              if (maxTimestamp > max) {
                max = maxTimestamp;
              }
            }
            message.reply(Json.createObject().set("status", messageDb.body().getString("status"))
                .set("ack", max));
          }
        });

      }
    });

    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }
}
