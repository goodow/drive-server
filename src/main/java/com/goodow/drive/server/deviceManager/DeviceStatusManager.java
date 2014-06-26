package com.goodow.drive.server.deviceManager;

import com.alienos.guice.GuiceVerticleHelper;
import com.alienos.guice.GuiceVertxBinding;
import com.goodow.drive.server.bootstrap.DriveModule;
import com.goodow.drive.server.utils.MyConstant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;
import com.google.inject.Inject;
import org.vertx.java.busmods.BusModBase;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.Json;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by gj on 5/12/14.
 */
@GuiceVertxBinding(modules = {DriveModule.class})
public class DeviceStatusManager extends BusModBase {

  @Inject
  private Bus bus;

  @Override
  public void start() {

    GuiceVerticleHelper.inject(this, vertx, container);
    super.start();

    bus.subscribe(MyConstant.DEVICE_STATUS, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> rootMessage) {
        JsonObject body = rootMessage.body();
        //get info
        final String deviceId = body.getString("deviceId");
        //final String owner = body.getString("owner");
        final JsonArray coordinates = body.getArray("coordinates");
        final String status = body.getString("status");
        final double radius = body.getNumber("radius");
        //get owner by deviceId
        JsonObject obj_getOwner = Json.createObject()
          .set("action", "get")
          .set("_index",MyConstant.ES_INDEX)
          .set("_type", MyConstant.ES_TYPE_DEVICE)
          .set("_id", deviceId);
        bus.send(MyConstant.SEARCH_CHANNEL, obj_getOwner, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body_getOwner = message.body();
            JsonObject ownerObj = body_getOwner.getObject("_source");
            if (ownerObj != null && !"".equals(ownerObj.getString("owner"))) {
              final String owner = ownerObj.getString("owner");
              boolean bool = true;
              if (bool && ("".equals(deviceId) || "".equals(owner) || "".equals(status) ||
                coordinates.length() == 0 || radius == 0)) {
                bool = false;
              }
              if (bool) {
                //get _id
                JsonObject obj_check = Json.createObject()
                  .set("action", "search")
                  .set("_index", MyConstant.ES_INDEX)
                  .set("_type", MyConstant.ES_TYPE_DEVICESTATUS)
                  .set("source", Json.createObject()
                    .set("query", Json.createObject()
                      .set("term", Json.createObject()
                        .set("deviceId", deviceId)
                        .set("owner", owner))));
                bus.send(MyConstant.SEARCH_CHANNEL, obj_check, new MessageHandler<JsonObject>() {
                  @Override
                  public void handle(Message<JsonObject> message) {
                    JsonObject obj = message.body();
                    int dataSize = obj.getObject("hits").getArray("hits").length();
                    String _id = "";
                    if (dataSize > 0) {
                      JsonObject nowObj = obj.getObject("hits").getArray("hits").get(0);
                      _id = nowObj.getString("_id");
                    }
                    JsonObject obj_opdb = Json.createObject()
                      .set("action", "index")
                      .set("_index", MyConstant.ES_INDEX)
                      .set("_type", MyConstant.ES_TYPE_DEVICESTATUS)
                      .set("source", Json.createObject()
                        .set("deviceId", deviceId)
                        .set("owner", owner)
                        .set("coordinates", coordinates)
                        .set("status", status)
                          .set("radius", radius));
                    if (!"".equals(_id)) {
                      obj_opdb.set("_id", _id);
                    }
                    bus.send(MyConstant.SEARCH_CHANNEL, obj_opdb, null);
                  }
                });
              }
            }
          }
        });
      }
    });
  }
}