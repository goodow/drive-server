package com.goodow.drive.es;

import com.goodow.drive.test.ExcelData;
import com.goodow.drive.test.InitDataFormExcel;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
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
  public void test() throws FileNotFoundException, IOException {
    URL url = InitDataFormExcel.class.getResource("/data.xlsx");
    List<List<String>> data = ExcelData.getExcelData(url.getPath());
    int rows = data.size();
    for (int i = 0; i < rows; i++) {
      if (i == 0) {
        continue;
      }
      List<String> list = data.get(i);
      JsonObject jsonObject =
          Json.createObject().set("id", list.get(0)).set("title", list.get(1)).set("contentType",
              getContentTypeBySuffix(list.get(2).trim())).set("url", list.get(2)).set("thumbnail",
              list.get(3)).set(
              "tags",
              Json.createArray().push(list.get(4)).push(list.get(5)).push(list.get(6)).push(
                  list.get(7)).push(list.get(8)).push(list.get(9)).push(list.get(10)).push(
                  list.get(11)).push(list.get(12)));
      System.out.println("bus.send(\"sid.drive.attachment\"," + jsonObject.toJsonString()
          + ",function(msg){console.log(JSON.stringify(msg.body()))})");
    }
  }
}
