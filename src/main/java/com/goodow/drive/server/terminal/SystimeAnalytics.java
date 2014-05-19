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
 * @DateCrate:Apr 21, 2014 4:18:54 PM
 * @DateUpdate:Apr 21, 2014 4:18:54 PM
 * @Des:对终端提交的开关机时间统计的数据进行转换和存储
 */
@GuiceVertxBinding(modules = {DriveModule.class})
public class SystimeAnalytics extends BusModBase implements MyConstant {
  @Inject
  private Bus bus;
  private int countDownLatch = 0;

  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);
    super.start();
    bus.registerHandler(ADDR_SYSTEM, new MessageHandler<JsonObject>() {

      @Override
      public void handle(Message<JsonObject> rootMessage) {
        JsonObject body = rootMessage.body();
        final String sid = body.getString("sid");
        final JsonArray analytics = body.getArray("timestamp");
        JsonObject deviceQuery =
            Json.createObject().set("action", "get").set("_index", ES_INDEX).set("_type",
                ES_TYPE_DEVICE).set("_id", sid);
        bus.send(SEARCH_CHANNEL, deviceQuery, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonArray analyticsArray = Json.createArray();
            String devicesOwner = message.body().getObject("_source").getString("owner");
            for (int i = 0; i < analytics.length(); i++) {
              JsonObject object = analytics.getObject(i);
              long openTime = (long) object.getNumber("openTime");
              long lastTime = (long) object.getNumber("lastTime");
              analyticsArray.push(Json.createObject().set("deviceId", sid)
                  .set("user", devicesOwner).set("open", DateUtil.parseTimestamp(openTime)).set(
                      "duration", lastTime));
              countDownLatch++;
            }
            savePlayAnalytics(message, analyticsArray);
          }
        });
      }
    });
  }

  /**
   * 保存转换后的数据
   * 
   * @author:DingPengwei
   * @date:Apr 21, 2014 4:25:32 PM
   * @param rootMessage
   * @param analyticsArray
   */
  private void savePlayAnalytics(final Message<JsonObject> rootMessage, JsonArray analyticsArray) {
    for (int i = 0; i < analyticsArray.length(); i++) {
      JsonObject msg =
          Json.createObject().set("action", "index").set("_index", ES_INDEX).set("_type",
              ES_TYPE_DEVICEACTIVITY).set("source", analyticsArray.getObject(i));
      bus.send("realtime.search", msg, new MessageHandler<JsonObject>() {
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
