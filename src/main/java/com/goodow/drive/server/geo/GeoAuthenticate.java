package com.goodow.drive.server.geo;

import com.goodow.drive.server.bootstrap.DriveModule;
import com.goodow.drive.server.utils.DateUtil;
import com.goodow.drive.server.utils.MyConstant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import com.alienos.guice.GuiceVerticleHelper;
import com.alienos.guice.GuiceVertxBinding;
import com.google.inject.Inject;

import org.vertx.java.busmods.BusModBase;

/**
 * 基于地理位置，控制激活设备
 * 
 * @author leiguorui
 * 
 */
@GuiceVertxBinding(modules = {DriveModule.class})
public class GeoAuthenticate extends BusModBase {
  @Inject private Bus bus;
  private static final String ADDR_GEO = MyConstant.ADDR + MyConstant.ADDR_GEO;
  private static final String ES_INDEX = MyConstant.ES_INDEX;
  private static final String ES_TYPE = MyConstant.ES_TYPE_T_GEO;
  private static final double EFFECTIVE_DISTANCE = 10; // 设备移动的有效距离
  private static final String ACTION_UNLOCKED = "unlocked"; // 解锁设备
  private static final int ERRORCODE = 161; // 正确的码
  JsonArray searchHit;

  /**
   * 解锁设备数据失效
   */
  public void expiredUnlocked() {
    JsonObject source = searchHit.getObject(0).getObject("_source");
    source.remove("expired").set("expired", "true");
    final JsonObject msgUpdate =
        Json.createObject().set("action", "index").set("_index", ES_INDEX).set("_type", ES_TYPE)
            .set("_id", searchHit.getObject(0).getString("_id")).set("_source", source);

    bus.send("realtime.search", msgUpdate, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> messageDb) {
      }
    });
  }

  /**
   * 查询有效的数据
   * 
   * @param message
   * @return
   */
  public JsonObject getQuery(Message<JsonObject> message) {
    JsonObject sidTerm =
        Json.createObject().set("term",
            Json.createObject().set("sid", message.body().getString("sid")));
    JsonObject expiredTerm =
        Json.createObject().set("term", Json.createObject().set("expired", "false"));
    JsonObject query =
        Json.createObject().set("bool",
            Json.createObject().set("must", Json.createArray().push(sidTerm).push(expiredTerm)));
    JsonObject msg =
        Json.createObject().set("action", "search").set("_index", ES_INDEX).set("_type", ES_TYPE)
            .set("query", query);
    return msg;
  }

  /**
   * 本次请求保存到ES
   * 
   * @param message
   */
  public void putData(Message<JsonObject> message) {
    final JsonObject msgNew =
        Json.createObject().set("action", "index").set("_index", ES_INDEX).set("_type", ES_TYPE)
            .set("_source",
                message.body().set("expired", "false").set("post_date", DateUtil.getDate()));
    bus.send("realtime.search", msgNew, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> messageDb) {
      }
    });
  }

  /**
   * 保存一条数据，下次请求时，返回解锁命令
   * 
   * @param message
   */
  public void putUnlock(Message<JsonObject> message) {
    final JsonObject toUnlocked =
        Json.createObject().set("action", "index").set("_index", ES_INDEX).set("_type", ES_TYPE)
            .set(
                "_source",
                Json.createObject().set("sid", message.body().getString("sid")).set("unlocked",
                    "false").set("expired", "false"));
    bus.send("realtime.search", toUnlocked, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> messageDb) {
      }
    });
  }

  @Override
  public void start() {
    GuiceVerticleHelper.inject(this, vertx, container);

    bus.registerHandler(ADDR_GEO, new MessageHandler<JsonObject>() {
      @Override
      public void handle(final Message<JsonObject> message) {

        if (ACTION_UNLOCKED.equals(message.body().getString("action"))) { // 如果收到解锁命令,把sid前面加unlocked

          final JsonObject msg = getQuery(message);

          expiredData(msg, message);

          message.reply(Json.createObject().set("status", "ok"));
        } else {
          final JsonObject msg = getQuery(message);

          bus.send("realtime.search", msg, new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> messageDb) {
              searchHit = messageDb.body().getObject("hits").getArray("hits");
              // 获取ES中该sid的保存记录数
              int saveTimes = searchHit.length();
              // 当该sid的记录为1，且未解锁
              if (saveTimes == 1
                  && "false".equals(searchHit.getObject(0).getObject("_source").getString(
                      "unlocked"))) {
                expiredUnlocked();
                message.reply(Json.createObject().set("status", "unlocked"));
                return;
              }
              if (saveTimes == 0) { // 第一次
                if (ERRORCODE == message.body().getNumber("errorcode")) {
                  putData(message);
                  message.reply(Json.createObject().set("status", "ok"));
                } else {
                  message.reply(Json.createObject().set("status", "unlocked"));
                }
              } else {
                try {
                  if (message.body().getNumber("distance") > EFFECTIVE_DISTANCE) { // distance 超出范围
                    message.reply(Json.createObject().set("status", "reject").set("content",
                        config.getObject("callback").getString("distance_overflow")));
                  } else {
                    putData(message);
                    message.reply(Json.createObject().set("status", "ok"));
                  }
                } catch (java.lang.NullPointerException e) { // distance未收到
                  message.reply(Json.createObject().set("status", "Distance is Null"));
                }
              }
            }
          });

        }
      }
    });

    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  /**
   * 递归修改解锁的数据状态
   * 
   * @param msg
   */
  private void expiredData(final JsonObject msg, final Message<JsonObject> message) {
    bus.send("realtime.search", msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> messageDb) {
        searchHit = messageDb.body().getObject("hits").getArray("hits");
        // 将数据置为过期
        if (searchHit != null && searchHit.length() > 0) {
          for (int i = 0; i < searchHit.length(); i++) {
            JsonObject source = searchHit.getObject(i).getObject("_source");
            source.remove("expired").set("expired", "true");
            final JsonObject msgUpdate =
                Json.createObject().set("action", "index").set("_index", ES_INDEX).set("_type",
                    ES_TYPE).set("_id", searchHit.getObject(i).getString("_id")).set("_source",
                    source);

            bus.send("realtime.search", msgUpdate, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> message) {
              }
            });
          }
          expiredData(msg, message);
        } else {
          // 保存一条数据，下次请求时，返回解锁命令
          putUnlock(message);
          return;
        }
      }
    });
  }
}