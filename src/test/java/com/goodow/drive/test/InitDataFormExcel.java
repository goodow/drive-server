package com.goodow.drive.test;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该文件运行前要和excel表格对照是否是一致的 0列是编号 1列是文件现实名称 2列是文件路径 3列是文件缩略图路径 第4列是素材类别 第5列是主题分类 第6列是班级 第7列是学期
 * 第8列是主题或领域 第9列是活动 第10列是搜索一级分类 第11列是搜索二级分类 第12列是搜索关键字
 * 
 * @author dpw
 * 
 */
public class InitDataFormExcel {

  public static final JsonArray TABLE_RELATION_DATA = Json.createArray();
  public static final JsonArray TABLE_FILE_DATA = Json.createArray();
  private static final Map<String, String> mime = new HashMap<String, String>();
  private static boolean check = true;// 标记要不要校验文件属性
  private static final String SD1_PATH = "/mnt/sdcard/sd1";// 真实的sd1路径
  private static final String SD2_PATH = "/mnt/sdcrad/sd2";// 真实的sd2路径
  private static final String VIR1_PATH = "attachments/sd1";// 模拟的sd1路径
  private static final String VIR2_PATH = "attachments/sd2";// 模拟的sd2路径
  private static final List<String> suffix = new ArrayList<String>();

  static {
    mime.put("mp3", "audio/mpeg");
    mime.put("mp4", "video/mp4");
    mime.put("pdf", "application/pdf");
    mime.put("swf", "application/x-shockwave-flash");
    mime.put("jpeg", "image/jpeg");
    mime.put("jpg", "image/jpeg");
    mime.put("文本", "application/pdf");
    mime.put("图片", "image/jpeg");
    mime.put("动画", "application/x-shockwave-flash");
    mime.put("视频", "video/mp4");
    mime.put("音频", "audio/mpeg");

    suffix.add(".mp3");
    suffix.add(".mp4");
    suffix.add(".pdf");
    suffix.add(".swf");
    suffix.add(".jpeg");
    suffix.add(".jpg");

    try {
      URL url = InitDataFormExcel.class.getResource("/EXCEL.xlsx");
      URL root = InitDataFormExcel.class.getResource("/");
      List<List<String>> data = ExcelData.getExcelData(url.getPath());
      int rows = data.size();
      for (int i = 0; i < rows; i++) {
        if (i == 0) {
          continue;
        }
        List<String> list = data.get(i);

        /**
         * 文件属性检测 0列是编号 1列是文件现实名称 2列是文件路径 3列是文件缩略图路径
         */
        if (check) {
          // 判断文件属性是否缺失
          if (list.size() < 3) {
            System.out.println("alert 第" + i + "行文件属性缺失");
            i--;
            continue;
          }

          // 判断文件编号是否存在
          if (list.get(0) == null || list.get(0).trim().equals("")) {
            System.out.println("alert 第" + i + "行文件编号不能为空");
            i--;
            continue;
          }

          // 判断文件后缀是否合法
          if (list.get(1) == null
              || suffix.contains(list.get(1).trim().substring(list.get(1).trim().lastIndexOf("."),
                  list.get(1).trim().length()))) {
            System.out.println("alert 第" + i + "行文件后缀无效");
            i--;
            continue;
          }

          // 判断文件显示名称是否存在
          if (list.get(1) == null || list.get(1).trim().equals("")) {
            System.out.println("alert 第" + i + "行文件显示名称不能为空");
            i--;
            continue;
          }

          // 判断文件属性路径是否存在
          if (!new File(root.getPath() + list.get(2)).exists()) {
            System.out.println("alert 第" + i + "行文件路径" + list.get(2) + "不存在");
            i--;
            continue;
          }

          // 判断文件缩略图是否存在
          if (list.size() >= 4 && list.get(3) != null
              && !new File(root.getPath() + list.get(3)).exists()) {
            System.out.println("alert 第" + i + "行缩略图路径" + list.get(3) + "不存在");
            i--;
            continue;
          }
        }

        if (check) {
          // 检测一级搜索分类和二级搜索分类的关系是否完整 第10列是搜索一级分类 第11列是搜索二级分类
          if (list.size() >= 12) {
            // 列数足够
            if (list.get(10) != null && list.get(11) != null && mime.containsKey(list.get(10))
                || list.get(10) == null && list.get(11) == null) {
              // 仅仅在一级和二级都不存在或都存在且一级合格的情况下才同过
            } else {
              System.out.println("alert 第" + i
                  + "行一级搜索和二级搜索不合格，请检测一级搜索是否在[文本/图片/动画/视频/音频]中，且二级分类不能是空格或NULL");
              i--;
              continue;
            }
          }
        }

        /**
         * 创建文件
         */
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, list.get(0).trim());// 编号
        file.set(Constant.KEY_NAME, list.get(1).trim());// 文件名称
        file.set(Constant.KEY_URL, getTruePath(list.get(2).trim()));// 文件路径
        file.set(Constant.KEY_CONTENTLENGTH, getContentLenght(list.get(2).trim()));// 文件长度
        file.set(Constant.KEY_CONTENTTYPE, getContentTypeBySuffix(list.get(2).trim()));// 文件contentType
        if (list.size() > 3 && list.get(3) != null) {
          file.set(Constant.KEY_THUMBNAIL, getTruePath(list.get(3).trim()));// 缩略图路径
        }
        TABLE_FILE_DATA.push(file);

        /**
         * 建立文件和TAG的关系 第4列是素材类别 第5列是主题分类 第6列是班级 第7列是学期 第8列是主题或领域 第9列是活动 第10列是搜索一级分类 第11列是搜索二级分类
         * 第12列是搜索关键字
         */
        for (int j = 4; j < list.size(); j++) {
          if (list.get(j) == null) {
            // 如果当前列是NULL该文件就不和该标签建立任何关系
            continue;
          }

          if (j == 12 && list.get(12) != null) {
            // 第12列的搜索的关键字 建立文件和搜索关键字的关系
            String[] splits = list.get(12).split(",");
            for (String split : splits) {
              if (split == null || split.trim().equals("")) {
                // 忽略无效关键字
                continue;
              }
              JsonObject relationFile =
                  Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
                      list.get(0).trim()).set(Constant.KEY_LABEL, split.trim());
              TABLE_RELATION_DATA.push(relationFile);
            }
          } else {
            // 如果除搜索关键字外的标签含有四位编号就去掉
            String tag = list.get(j);
            if (tag.matches("^\\d{4}.*")) {
              tag = tag.substring(4, tag.length());
            }
            JsonObject relationFile =
                Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, tag)
                    .set(Constant.KEY_LABEL, tag.trim());
            TABLE_RELATION_DATA.push(relationFile);
          }
        }

        /**
         * 建立主题分类, 班级, 学期, 主题或领域和活动的映射 第5列是主题分类 第6列是班级 第7列是学期 第8列是主题或领域 第9列是活动 list.size() >= 10
         * 且list.get(9) != null才有活动的概念 兼容主题分类, 班级, 学期, 主题或领域是否为空
         */
        if (list.size() >= 10 && list.get(9) != null && list.get(5) != null) {
          for (int j = 5; j < 9; j++) {
            if (list.get(j) != null) {
              String tag = list.get(9).trim();
              if (tag.matches("^\\d{4}.*")) {
                tag = tag.substring(4, tag.length());
              }
              JsonObject relationTag =
                  Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, tag).set(
                      Constant.KEY_LABEL, list.get(j).trim());
              TABLE_RELATION_DATA.push(relationTag);
            }
          }
        }

        /**
         * 建立一级搜索分类和二级搜索分类的关系 第10列是搜索一级分类 第11列是搜索二级分类
         */
        if (list.size() >= 12 && list.get(10) != null && list.get(11) != null) {
          JsonObject relationTag =
              Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
                  list.get(11).trim()).set(Constant.KEY_LABEL, mime.get(list.get(10).trim()));
          TABLE_RELATION_DATA.push(relationTag);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 获取文件长度
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

  /**
   * 获取文件的真实路径
   * 
   * @return
   */
  private static String getTruePath(String path) {
    if (path.contains(VIR1_PATH)) {
      return path.replace(VIR1_PATH, SD1_PATH);
    } else if (path.contains(VIR2_PATH)) {
      return path.replace(VIR2_PATH, SD2_PATH);
    }
    return path;
  }
}
