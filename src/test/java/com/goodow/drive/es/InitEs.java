package com.goodow.drive.es;

import com.goodow.drive.test.ExcelData;
import com.goodow.drive.test.InitDataFormExcel;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:DingPengwei
 * @Email:dingpengwei@goodow.com
 * @DateCrate:Apr 21, 2014 1:13:24 PM
 * @DateUpdate:Apr 21, 2014 1:13:24 PM
 * @Des:执行数据初始化插入
 */
public class InitEs extends TestCase {
  private static final Map<String, String> mime = new HashMap<String, String>();
  static {
    mime.put("mp3", "audio/mpeg");
    mime.put("mp4", "video/mp4");
    mime.put("pdf", "application/pdf");
    mime.put("swf", "application/x-shockwave-flash");
    mime.put("jpeg", "image/jpeg");
    mime.put("jpg", "image/jpeg");
  }

  private static String getContentTypeBySuffix(String path) {
    path = path.substring(path.lastIndexOf(".") + 1, path.length());
    return mime.get(path);
  }

  /**
   * @author:DingPengwei
   * @date:Apr 21, 2014 1:15:30 PM
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void test() throws Exception {
    URL url = InitDataFormExcel.class.getResource("/data.xlsx");
    List<List<String>> data = ExcelData.getExcelData(url.getPath());
    int rows = data.size();
    BufferedWriter bw =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
            "/home/dpw/Desktop/sqlxxx.txt"), true)));
    for (int i = 0; i < rows; i++) {
      if (i == 0) {
        continue;
      }
      List<String> list = data.get(i);
      String sql1 =
          "REPLACE INTO T_FILE(UUID,NAME,CONTENTTYPE,SIZE,FILEPATH,THUMBNAILS) VALUES('"
              + list.get(0) + "','" + list.get(1) + "','"
              + getContentTypeBySuffix(list.get(2).trim()) + "','0','" + list.get(2) + "','"
              + list.get(3) + "');";
      String sql21 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(4) + "');";
      String sql22 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(5) + "');";
      String sql23 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(6) + "');";
      String sql24 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(7) + "');";
      String sql25 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(8) + "');";
      String sql26 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(9) + "');";
      String sql27 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(10) + "');";
      String sql28 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(11) + "');";
      String sql29 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','" + list.get(0) + "','"
              + list.get(12) + "');";
      String sql31 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('tag','" + list.get(9) + "','"
              + list.get(5) + "');";
      String sql32 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('tag','" + list.get(9) + "','"
              + list.get(6) + "');";
      String sql33 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('tag','" + list.get(9) + "','"
              + list.get(7) + "');";
      String sql34 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('tag','" + list.get(9) + "','"
              + list.get(8) + "');";
      String sql35 =
          "REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('tag','" + list.get(11) + "','"
              + list.get(10) + "');";

      bw.write(sql1 + "\r\n");
      bw.write(sql21 + "\r\n");
      bw.write(sql22 + "\r\n");
      bw.write(sql23 + "\r\n");
      bw.write(sql24 + "\r\n");
      bw.write(sql25 + "\r\n");
      bw.write(sql26 + "\r\n");
      bw.write(sql27 + "\r\n");
      bw.write(sql28 + "\r\n");
      bw.write(sql29 + "\r\n");
      bw.write(sql31 + "\r\n");
      bw.write(sql32 + "\r\n");
      bw.write(sql33 + "\r\n");
      bw.write(sql34 + "\r\n");
      bw.write(sql35 + "\r\n");

      // REPLACE INTO T_FILE(UUID,NAME,CONTENTTYPE,SIZE,FILEPATH,THUMBNAILS)
      // VALUES('11','孙悟空打妖怪','application/pdf','0','attachments/sd1/goodow/drive/和谐/语言/1155孙悟空打妖怪/孙悟空打妖怪-hdsj.pdf','attachments/sd1/goodow/drive/和谐/语言/1155孙悟空打妖怪/孙悟空打妖怪-hdsj.png');
      // REPLACE INTO T_RELATION(TYPE,KEY,TAG) VALUES('attachment','11','素材-活动设计');

      JsonObject jsonObject =
          Json.createObject().set("id", list.get(0)).set("title", list.get(1)).set("contentType",
              getContentTypeBySuffix(list.get(2).trim())).set("url", list.get(2)).set("thumbnail",
              list.get(3)).set(
              "tags",
              Json.createArray().push(list.get(4)).push(list.get(5)).push(list.get(6)).push(
                  list.get(7)).push(list.get(8)).push(list.get(9)).push(list.get(10)).push(
                  list.get(11)).push(list.get(12)));
      System.out.println("bus.send(\"sid.drive.db\"," + jsonObject.toJsonString()
          + ",function(msg){console.log(JSON.stringify(msg.body()))})");
    }
    bw.flush();
    bw.close();
  }
}
