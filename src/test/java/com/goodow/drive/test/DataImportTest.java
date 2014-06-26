package com.goodow.drive.test;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.server.impl.VertxBus;
import com.goodow.realtime.channel.server.impl.VertxPlatform;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DataImportTest extends TestVerticle {
  private static JsonArray sids = Json.createArray();
  private static int number = 100;// 分批数值
  private static JsonArray sdcards = Json.createArray();
  private static String sdCard1 = "/mnt/sdcard";
  private static String sdCard2 = "/mnt/sdcard";
  private static String sid = "sid.drive.db";// 提示：sid已经修改为mac
  private static String testResPath = "";
  private static String fileName= "";
  private static final JsonArray insertingFiles = Json.createArray();

  private static Map<String, Integer> SUCCESS_FILE_COUNTER = new HashMap<String, Integer>();

  private static void file(final Bus bus, final JsonObject tag, final String currentSid) {
    bus.send(currentSid, tag, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().has(Constant.KEY_STATUS)
            && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
          SUCCESS_FILE_COUNTER.put(currentSid, SUCCESS_FILE_COUNTER.get(currentSid) == null ? tag
              .getArray("datas").length() : SUCCESS_FILE_COUNTER.get(currentSid)
              + tag.getArray("datas").length());
          System.out.println("当前插入" + currentSid + "文件数据量：" + SUCCESS_FILE_COUNTER.get(currentSid)
              + "/" + InitDataFormExcel.FILES_TAGS.length());
          if (SUCCESS_FILE_COUNTER.get(currentSid) < InitDataFormExcel.FILES_TAGS.length()) {
            JsonObject file =
                Json.createObject().set("action", "put").set("datas",
                    insertingFiles.getArray((SUCCESS_FILE_COUNTER.get(currentSid) / number)));
            file(bus, file, currentSid);
          } else {
            System.out.println("\r\n ======================插入" + currentSid
                + "文件数据完毕======================\r\n");
            // VertxAssert.testComplete();
            testComplete();
          }
        } else {
          System.out.println("\r\n ======================插入" + currentSid
              + "文件数据失败======================\r\n");
          // VertxAssert.testComplete();
          testComplete();
        }
      }
    });
  }

  private static void testComplete() {
    boolean flag = false;
    Set<Entry<String, Integer>> entrySet = SUCCESS_FILE_COUNTER.entrySet();
    for (Entry<String, Integer> entry : entrySet) {
      Integer value = entry.getValue();
      System.out.println(entry.getKey() + "=" + value);
      if (value < InitDataFormExcel.FILES_TAGS.length()) {
        flag = false;
        break;
      } else if (sids.length() == entrySet.size() && value >= InitDataFormExcel.FILES_TAGS.length()) {
        flag = true;
      }
    }
    if (flag) {
      VertxAssert.testComplete();
    }
  }

  private Bus bus;

  @Test
  public void importData() {
    if (InitDataFormExcel.FILES_TAGS.length() <= 0) {
      System.out.println("文件总数量：" + InitDataFormExcel.FILES_TAGS.length() + "   数据不完整");
      VertxAssert.testComplete();
      return;
    }
    System.out.println("文件总数量：" + InitDataFormExcel.FILES_TAGS.length());
    for (int i = 0; i < sids.length(); i++) {
      final String currentSid = sids.getString(i);
      bus.send(sids.getString(i), Json.createObject().set("action", "delete"),
          new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> message) {
              if (message.body().has(Constant.KEY_STATUS)
                  && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
                System.out.println("\r\n======================清除 " + currentSid + " 数据库数据成功，准备向"
                    + currentSid + "插入数据======================\r\n");

                JsonObject file =
                    Json.createObject().set("action", "put").set("datas",
                        insertingFiles.getArray(0));
                file(bus, file, currentSid);
              } else {
                System.out.println("\r\n======================清除 " + currentSid
                    + " 数据库数据失败，拒绝后续数据插入======================\r\n");
                // VertxAssert.testComplete();
                testComplete();
              }
            }
          });
    }
  }

  @Override
  public void start() {
    initialize();
    VertxPlatform.register(vertx);

    sid = System.getProperty("sid", sid);
    number = Integer.parseInt(System.getProperty("f", number + ""));
    sdCard1 = System.getProperty("sd1", sdCard1);
    sdCard2 = System.getProperty("sd2", sdCard2);
    testResPath = System.getProperty("respath", testResPath);
    fileName = "".equals(System.getProperty("filename", fileName))?"data.xlsx":System.getProperty("filename", fileName);
    System.out
        .println("命令样例:mvn clean test -D sid=mysid -D f=200 -D r=1000 -D sd1=mysd1 -D sd2=mysd2 -D respath=respath  \r\n");
    System.out.println("输入参数：sid=" + sid + "  f=" + number + "  sd1=" + sdCard1 + "   sd2="
        + sdCard2 + "   respath=" + testResPath + "\r\n");

    JsonObject config = Json.createObject();
    JsonObject web_server = Json.createObject().set("port", 8082);
    config.set("web_server", web_server);
    JsonObject realtime_store =
        Json.createObject().set("redis", Json.createObject().set("host", "realtime.goodow.com"));
    config.set("realtime_store", realtime_store);
    JsonObject realtime_search =
        Json.createObject().set("transportAddresses",
            Json.createArray().push(Json.createObject().set("host", "realtime.goodow.com")));
    config.set("realtime_search", realtime_search);
    System.out.println(config);
    container.deployModule(System.getProperty("vertx.modulename"),
        new org.vertx.java.core.json.JsonObject(config.toJsonString()),
        new AsyncResultHandler<String>() {
          @Override
          public void handle(AsyncResult<String> asyncResult) {
            VertxAssert.assertTrue(asyncResult.succeeded());
            VertxAssert.assertNotNull("deploymentID should not be null", asyncResult.result());
            bus = new VertxBus(vertx.eventBus());
            System.out.println("\r\n================== 可以发送消息了==================\r\n");
            bus.subscribe("sid.drive.db.start", new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> message) {
                JsonObject body = message.body();
                sids = body.getArray("sid");
                if (body.has("num")) {
                  number = (int) body.getNumber("num");
                }
                if (body.has("sdcard")) {
                  sdcards = body.getArray("sdcard");
                }
                InitDataFormExcel.factory(sdcards.length() >= 1 ? sdcards.getString(0) : sdCard1,
                    sdcards.length() >= 2 ? sdcards.getString(1) : sdCard2, testResPath,fileName);
                setUp();
                startTests();
                // message.reply(Json.createObject().set("status", "ok"));
              }
            });
          }
        });
  }

  private void setUp() {
    // 分组文件表数据
    int timer_file =
        InitDataFormExcel.FILES_TAGS.length() % number == 0 ? InitDataFormExcel.FILES_TAGS.length()
            / number : InitDataFormExcel.FILES_TAGS.length() / number + 1;
    for (int i = 0; i < timer_file; i++) {
      JsonArray temp = Json.createArray();
      for (int j = 0; j < number; j++) {
        if (number * i + j >= InitDataFormExcel.FILES_TAGS.length()) {
          break;
        }
        temp.push(InitDataFormExcel.FILES_TAGS.getObject(number * i + j));
      }
      insertingFiles.push(temp);
    }

  }
}
