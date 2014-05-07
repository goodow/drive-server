package com.goodow.drive.test;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.impl.WebSocketBus;
import com.goodow.realtime.channel.server.VertxPlatform;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataImportTest extends TestVerticle {
  private static final Logger log = Logger.getLogger(DataImportTest.class.getName());
  private static String sid = "sid";// 提示：sid已经修改为mac
  private static final String address = ".drive.db";

  // //模拟数据
  // private static final JsonArray tagsDataTemp = InitData.RELATION_TABLE_DATA;
  // private static final JsonArray filesDataTemp = InitData.FILE_TABLE_DATA;

  // 导入数据
  private static JsonArray tagsDataTemp = InitDataFormExcel.TABLE_RELATION_DATA;
  private static JsonArray filesDataTemp = InitDataFormExcel.TABLE_FILE_DATA;

  private static int numFile = 200;// 文件分批数值
  private static int numRelation = 1000;// 对应关系分批数值
  private static final JsonArray insertingFiles = Json.createArray();
  private static final JsonArray insertingTags = Json.createArray();

  private static int SUCCESS_FILE_COUNTER = 0;
  private static int SUCCESS_TAG_COUNTER = 0;

  private static void file(final Bus bus, final JsonObject tag) {
    bus.send(sid + address, tag, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().has(Constant.KEY_STATUS)
            && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
          SUCCESS_FILE_COUNTER = SUCCESS_FILE_COUNTER + tag.getArray("data").length();
          System.out.println("当前插入文件数据量：" + SUCCESS_FILE_COUNTER + "/" + filesDataTemp.length());
          if (SUCCESS_FILE_COUNTER % numFile == 0) {
            JsonObject file =
                Json.createObject().set("action", "put").set("table", "T_FILE").set("data",
                    insertingFiles.getArray(SUCCESS_FILE_COUNTER / numFile));
            file(bus, file);
          } else {
            System.out.println("\r\n ======================插入文件测试数据完毕======================\r\n");
            // 插入文件完毕后再插入对应关系
            JsonObject relation =
                Json.createObject().set("action", "put").set("table", "T_RELATION").set("data",
                    insertingTags.getArray(0));
            relation(bus, relation);
          }
        } else {
          System.out.println("\r\n ======================插入文件测试数据失败======================\r\n");
          VertxAssert.testComplete();
        }
      }
    });
  }

  private static void handlerEventBusOpened(final Bus bus) {
    bus.send(sid + address, Json.createObject().set("action", "delete"),
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            if (message.body().has(Constant.KEY_STATUS)
                && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
              System.out
                  .println("\r\n======================数据库数据清除成功，准备插入数据======================\r\n");
              JsonObject file =
                  Json.createObject().set("action", "put").set("table", "T_FILE").set("data",
                      insertingFiles.getArray(0));
              file(bus, file);
            } else {
              System.out
                  .println("\r\n======================数据库数据清除失败，拒绝后续数据插入======================\r\n");
              VertxAssert.testComplete();
            }
          }
        });
  }

  private static void relation(final Bus bus, final JsonObject tag) {
    bus.send(sid + address, tag, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().has(Constant.KEY_STATUS)
            && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
          SUCCESS_TAG_COUNTER = SUCCESS_TAG_COUNTER + tag.getArray("data").length();
          System.out.println("当前插入对应关系数据量：" + SUCCESS_TAG_COUNTER + "/" + tagsDataTemp.length());
          if (SUCCESS_TAG_COUNTER % numRelation == 0) {
            JsonObject relation =
                Json.createObject().set("action", "put").set("table", "T_RELATION").set("data",
                    insertingTags.getArray(SUCCESS_TAG_COUNTER / numRelation));
            relation(bus, relation);
          } else {
            System.out.println("\r\n ======================插入标签测试数据完毕======================\r\n");
            System.out.println("\r\n =========================数据初始化成功======================\r\n");
            VertxAssert.testComplete();
          }
        } else {
          System.out.println("\r\n ======================插入标签映射测试数据失败======================\r\n");
          VertxAssert.testComplete();
        }
      }
    });
  }

  @Test
  public void importData() {
    if (filesDataTemp.length() <= 0 || tagsDataTemp.length() <= 0) {
      System.out.println("文件总数量：" + filesDataTemp.length() + "   对应关系总数量：" + tagsDataTemp.length()
          + "   数据不完整");
      VertxAssert.testComplete();
      return;
    }
    System.out.println("文件总数量：" + filesDataTemp.length() + "   对应关系总数量：" + tagsDataTemp.length());
    final Bus bus =
        new WebSocketBus("ws://test.goodow.com:8080/eventbus/websocket", Json.createObject().set(
            "forkLocal", true));

    bus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handlerEventBusOpened(bus);
      }
    });
    bus.registerHandler(Bus.LOCAL_ON_CLOSE, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        log.info("EventBus closed");
        VertxAssert.testComplete();
      }
    });
    bus.registerHandler(Bus.LOCAL_ON_ERROR, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        log.log(Level.SEVERE, "EventBus Error");
      }
    });
  }

  @Override
  public void start() {
    initialize();
    VertxPlatform.register(vertx);

    sid = System.getProperty("sid", "sid");
    numFile = Integer.parseInt(System.getProperty("f", "200"));
    numRelation = Integer.parseInt(System.getProperty("r", "1000"));
    String sdCard1 = System.getProperty("sd1", "/mnt/sdcard");
    String sdCard2 = System.getProperty("sd2", "/mnt/sdcard");
    InitDataFormExcel.factory(sdCard1, sdCard2);
    tagsDataTemp = InitDataFormExcel.TABLE_RELATION_DATA;
    filesDataTemp = InitDataFormExcel.TABLE_FILE_DATA;
    System.out
        .println("命令样例:mvn clean test -D sid=mysid -D f=200 -D r=1000 -D sd1=mysd1 -D sd2=mysd2 \r\n");
    System.out.println("输入参数：sid=" + sid + "  f=" + numFile + "  r=" + numRelation + "  sd1="
        + sdCard1 + "   sd2=" + sdCard2 + "\r\n");

    setUp();
    startTests();
  }

  private void setUp() {
    // 分组文件表数据
    int timer_file =
        filesDataTemp.length() % numFile == 0 ? filesDataTemp.length() / numFile : filesDataTemp
            .length()
            / numFile + 1;
    for (int i = 0; i < timer_file; i++) {
      JsonArray temp = Json.createArray();
      for (int j = 0; j < numFile; j++) {
        if (numFile * i + j >= filesDataTemp.length()) {
          break;
        }
        temp.push(filesDataTemp.getObject(numFile * i + j));
      }
      insertingFiles.push(temp);
    }

    // 分组文件表数据
    int timer_tag =
        tagsDataTemp.length() % numRelation == 0 ? tagsDataTemp.length() / numRelation
            : tagsDataTemp.length() / numRelation + 1;
    for (int i = 0; i < timer_tag; i++) {
      JsonArray temp = Json.createArray();
      for (int j = 0; j < numRelation; j++) {
        if (numRelation * i + j >= tagsDataTemp.length()) {
          break;
        }
        temp.push(tagsDataTemp.getObject(numRelation * i + j));
      }
      insertingTags.push(temp);
    }
  }

}
