package com.goodow.drive.test;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitDataFormExcel {

  public static final JsonArray TABLE_RELATION_DATA = Json.createArray();
  public static final JsonArray TABLE_FILE_DATA = Json.createArray();
  private static final Map<String, String> mime = new HashMap<>();

  static {
    mime.put("mp3", "audio/mpeg");
    mime.put("mp4", "video/mp4");
    mime.put("pdf", "application/pdf");
    mime.put("swf", "application/x-shockwave-flash");
    mime.put("jpeg", "image/jpeg");
    mime.put("jpg", "image/jpeg");
    try {
      URL url = InitDataFormExcel.class.getResource("/EXCEL.xlsx");
      List<List<String>> data = ExcelData.getExcelData(url.getPath());
      int rows = data.size();
      for (int i = 0; i < rows; i++) {
        if (i == 0) {
          continue;
        }
        List<String> list = data.get(i);
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, list.get(0));// 编号
        file.set(Constant.KEY_NAME, list.get(1));// 文件名称
        file.set(Constant.KEY_URL, list.get(2));// 文件路径
        file.set(Constant.KEY_THUMBNAIL, list.get(3));// 缩略图路径
        file.set(Constant.KEY_CONTENTLENGTH, getContentLenght(list.get(2)));// 文件长度
        file.set(Constant.KEY_CONTENTTYPE, getContentTypeBySuffix(list.get(2)));// 文件contentType
        TABLE_FILE_DATA.push(file);

        // 建立文件和TAG的关系
        for (int j = 4; j < list.size(); j++) {
          if (list.get(j) == null) {
            continue;
          }
          if (j == 6) {
            String[] splits = list.get(6).split(",");
            for (String split : splits) {
              JsonObject relationFile =
                  Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
                      list.get(0)).set(Constant.KEY_LABEL, split);
              TABLE_RELATION_DATA.push(relationFile);
            }
          } else {
            JsonObject relationFile =
                Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
                    list.get(0)).set(Constant.KEY_LABEL, list.get(j));
            TABLE_RELATION_DATA.push(relationFile);
          }
        }

        // 建立TAG和TAG的关系
        if (list.get(4) != null && list.get(5) != null) {// 搜索二级分类和搜索一级分类的关系

        }

        // 主题分类, 班级, 学期, 主题或领域, 活动
        if (list.get(7) != null) {
          for (int j = 7; j < list.size() - 1; j++) {
            if (list.get(j) != null) {
              JsonObject relationTag =
                  Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
                      list.get(list.size() - 1)).set(Constant.KEY_LABEL, list.get(j));
              TABLE_RELATION_DATA.push(relationTag);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * 获取文件长度
   * 
   * @param path
   * 
   * @return
   */
  private static long getContentLenght(String path) {
    if (new File(path).exists()) {
      return new File(path).length();
    }
    return 0;
  }

  /**
   * 获取文件MIME
   */
  private static String getContentTypeBySuffix(String path) {
    path = path.substring(path.lastIndexOf(".") + 1, path.length());
    return mime.get(path);
  }
}
