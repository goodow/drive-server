package com.goodow.drive.server.terminal;

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

/**
 * @Author:DingPengwei
 * @Email:dingpengwei@goodow.com
 * @DateCrate:Apr 21, 2014 2:48:10 PM
 * @DateUpdate:Apr 21, 2014 2:48:10 PM
 * @Des:对终端提交的播放统计的数据进行转换和存储
 */
@GuiceVertxBinding(modules = {DriveModule.class})
public class PlayerAnalytics extends BusModBase implements MyConstant{
  @Inject
  private Bus bus;
  private int countDownLatch = 0;

  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);
    super.start();
    bus.registerHandler(ADDR_PLAYER, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> rootMessage) {
        JsonObject body = rootMessage.body();
        final String sid = body.getString("sid");
        final JsonArray analytics = body.getArray("analytics");
          JsonObject sidQuery =
                  Json.createObject().set("action", "get").set("_index",ES_INDEX).set("_type",ES_TYPE_DEVICE).set("_id", sid);
          bus.send(SEARCH_CHANNEL,sidQuery, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> rootMessage) {
                  JsonObject body = rootMessage.body();
                  final String owner = body.getObject("_source").getString("owner");
                  for (int i = 0; i < analytics.length(); i++) {
                      JsonObject object = analytics.getObject(i);
                      final String attachmentId = object.getString("attachment");
                      final JsonArray timestamps = object.getArray("timestamp");
                      JsonObject attachementQuery = Json.createObject().set("action","get").set("_index",ES_INDEX).set("_type",ES_TYPE_ATTACHMENT).set("_id", attachmentId);
                      bus.send("realtime.search",attachementQuery, new MessageHandler<JsonObject>() {
                          @Override
                          public void handle(Message<JsonObject> rootMessage) {
                              final JsonArray analyticsArray = Json.createArray();
                              String title = rootMessage.body().getObject("_source").getString("title");
                              for (int j = 0; j < timestamps.length(); j++) {
                                  JsonObject timestampObject = timestamps.getObject(j);
                                  long openTime = (long) timestampObject.getNumber("openTime");
                                  long lastTime = (long) timestampObject.getNumber("lastTime");
                                  analyticsArray.push(Json.createObject().set("userId", sid).set("attachmentId",
                                          attachmentId).set("title",title).set("open ", DateUtil.parseTimestamp(openTime)).set("duration",
                                          lastTime).set("user",owner));
                                  countDownLatch++;
                              }
                              savePlayAnalytics(rootMessage, analyticsArray);
                          }
                      });
                  }
              }
          });
      }
    });
  }

  /**
   * 保存转换后的数据
   *
   * @author:DingPengwei
   * @date:Apr 21, 2014 3:21:59 PM
   * @param rootMessage
   * @param analyticsArray
   */
  private void savePlayAnalytics(final Message<JsonObject> rootMessage, JsonArray analyticsArray) {// rx.java
    for (int i = 0; i < analyticsArray.length(); i++) {
      JsonObject msg =
          Json.createObject().set("action", "index").set("_index", ES_INDEX).set(
              "_type", ES_TYPE_PLAYER).set("source", analyticsArray.getObject(i));
      bus.send(SEARCH_CHANNEL, msg, new MessageHandler<JsonObject>() {
        @Override
        public void handle(Message<JsonObject> messageDb) {
          countDownLatch--;
          if (countDownLatch == 0) {
            rootMessage.reply(Json.createObject().set("status", "ok"));
          }
        }
      });
    }
  }
}
