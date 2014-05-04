package com.goodow.drive.server.attachment;

import com.goodow.drive.server.bootstrap.DriveModule;
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
 * @DateCrate:Apr 16, 2014 5:55:39 PM
 * @DateUpdate:Apr 16, 2014 5:55:39 PM
 * @Des:description
 */
@GuiceVertxBinding(modules = {DriveModule.class})
public class AttachmentInfo extends BusModBase {
  @Inject
  private Bus bus;
  JsonArray searchHit;

  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);
    super.start();
    bus.registerHandler(MyConstant.ADDR_ATTACHMENT, new MessageHandler<JsonObject>() {

      @Override
      public void handle(final Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        if ("delete".equals(action)) {
          message.reply(Json.createObject().set("status", "ok"));
        }
        if ("put".equals(action)) {
          JsonArray datas = body.getArray("datas");
          for (int i = 0; i < datas.length(); i++) {
            JsonObject data = datas.getObject(i);
            String id = data.getString("_id");
            data.remove("_id");
            // 执行插入
            JsonObject msgNew =
                Json.createObject().set("action", "index").set("_index", MyConstant.ES_INDEX).set(
                    "_type", MyConstant.ES_TYPE_ATTACHMENT).set("_id", id).set("source", data);
            bus.send("realtime.search", msgNew, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> resultMessage) {
                message.reply(Json.createObject().set("status", "ok"));
              }
            });
          }
        }
      }
    });
  }
}
