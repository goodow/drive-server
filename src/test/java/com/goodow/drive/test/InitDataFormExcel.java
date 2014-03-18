package com.goodow.drive.test;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
  private static boolean replace = true;// 标记要不要本地替换文件模拟路径
  private static final String SD1_PATH = "/mnt/sdcard";// 真实的sd1路径
  private static final String SD2_PATH = "/mnt/sdcrad/sd2";// 真实的sd2路径
  private static final String VIR1_PATH = "attachments/sd1";// 模拟的sd1路径
  private static final String VIR2_PATH = "attachments/sd2";// 模拟的sd2路径
  private static final List<String> suffix = new ArrayList<String>();
  private static final JsonArray ERRORS = Json.createArray();
  private static final JsonArray repeatInfo = Json.createArray();
  private static final Map<String, String> repeatIdNames = new HashMap<>();
  private static final List<String> catagories = Arrays.asList("素材-活动设计", "素材-文学作品", "素材-说明文字",
      "素材-背景知识", "素材-乐谱", "素材-教学图片", "素材-动态图", "素材-参考图", "素材-挂图", "素材-轮廓图", "素材-头饰", "素材-手偶",
      "素材-胸牌", "素材-动画", "素材-电子书", "素材-视频", "素材-音频", "素材-音效");
  private static final Map<String, List<String>> searchGradeRelation = new HashMap<>();
  private static final List<String> themes = Arrays.asList("和谐", "托班", "示范课", "入学准备", "安全教育",
      "早期阅读");
  private static final List<String> grades = Arrays.asList("小班", "中班", "大班", "学前班");
  private static final List<String> terms = Arrays.asList("上学期", "下学期");
  private static final List<String> topics = Arrays.asList("健康", "语言", "社会", "科学", "数学", "艺术(音乐)",
      "艺术(美术)", "我有一个幼儿园", "找找,藏藏", "飘飘,跳跳,滚滚", "我会……", "小小手", "好吃哎", "汽车嘀嘀嘀", "快乐红色", "暖暖的……",
      "思维", "阅读与书写", "习惯与学习品质", "冰波童话", "快乐宝贝", "其他");

  static {
    mime.put("mp3", "audio/mpeg");
    mime.put("mp4", "video/mp4");
    mime.put("pdf", "application/pdf");
    mime.put("swf", "application/x-shockwave-flash");
    mime.put("jpeg", "image/jpeg");
    mime.put("jpg", "image/jpeg");
    mime.put("活动设计", "application/pdf");
    mime.put("图片", "image/jpeg");
    mime.put("动画", "application/x-shockwave-flash");
    mime.put("电子书", "application/x-shockwave-flash");
    mime.put("视频", "video/mp4");
    mime.put("音频", "audio/mpeg");

    suffix.add(".mp3");
    suffix.add(".mp4");
    suffix.add(".pdf");
    suffix.add(".swf");
    suffix.add(".jpeg");
    suffix.add(".jpg");

    searchGradeRelation.put("活动设计", Arrays.asList(new String[] {
        "健康", "托班", "语言", "社会", "数学", "科学", "艺术(美术)", "艺术(音乐)"}));

    searchGradeRelation.put("图片", Arrays.asList(new String[] {
        "教学图片", "参考图", "挂图", "轮廓图", "头饰", "手偶", "胸牌"}));

    searchGradeRelation.put("动画", Arrays
        .asList(new String[] {"文学作品动画", "音乐作品动画", "数学教学动画", "其他动画"}));

    searchGradeRelation.put("视频", Arrays.asList(new String[] {"教学用视频", "教学示范课", "音乐表演视频"}));

    searchGradeRelation.put("音频", Arrays.asList(new String[] {"音乐作品音频", "文学作品音频", "音效"}));

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
         * 文件属性检测 0列是编号 1列是文件现实名称 2列是文件路径 3列是文件缩略图路径 第四列是素材类别 第10列是搜索一级分类
         */
        if (check) {
          // 判断文件属性是否缺失
          if (list.size() < 11) {
            ERRORS.push("error 第" + i + "行文件属性缺失[文件编号|文件显示名称|文件路径|素材类别|搜索一级分类]都不能为空");
          }

          // 判断文件编号是否存在
          if (list.get(0) == null || list.get(0).trim().equals("")) {
            ERRORS.push("error 第" + i + "行文件编号不能为空");
          }

          // 判断文件显示名称是否存在
          if (list.get(1) == null || list.get(1).trim().equals("")) {
            ERRORS.push("error 第" + i + "行文件显示名称不能为空");
          }

          // 检测ID和文件名的重复性
          if (list.get(0) != null && list.get(1) != null) {
            if (repeatIdNames.containsKey((list.get(0).trim()))) {
              ERRORS.push("error 第" + i + "行文件编号已经存在");
            }
            if (repeatIdNames.containsValue((list.get(1).trim()))) {
              repeatInfo.push("alert 第" + i + "行文件名称有重复");
            }
            repeatIdNames.put(list.get(0).trim(), list.get(1).trim());
          }

          // 判断文件后缀是否合法
          if (list.get(2) == null
              || !suffix.contains(list.get(2).trim().substring(list.get(2).trim().lastIndexOf("."),
                  list.get(2).trim().length()))) {
            ERRORS.push("error 第" + i + "行文件后缀无效");
          }

          // 判断文件属性路径是否存在
          if (!new File(root.getPath() + list.get(2).trim()).exists()) {
            ERRORS.push("error 第" + i + "行文件路径" + list.get(2).trim() + "不存在");
          }

          // 判断文件缩略图是否存在
          if (list.get(3) != null && !new File(root.getPath() + list.get(3).trim()).exists()) {
            ERRORS.push("error 第" + i + "行缩略图路径" + list.get(3).trim() + "不存在");
          }

          // 判断文件素材类别
          if (list.get(4) == null || !catagories.contains(list.get(4).trim())) {
            ERRORS.push("error 第" + i + "行素材类别" + list.get(4).trim() + "不存在，或素材类别不再19个分类中");
          }

          // 第5列是主题分类 第6列是班级 第7列是学期 第8列是主题或领域
          if (list.get(5) != null && !themes.contains(list.get(5).trim())) {
            ERRORS.push("error 第" + i + "行主题[" + list.get(5).trim() + "]不在" + themes.toString()
                + "中");
          }
          if (list.get(6) != null && !grades.contains(list.get(6).trim())) {
            ERRORS.push("error 第" + i + "行班级[" + list.get(6).trim() + "]不在" + grades.toString()
                + "中");
          }
          if (list.get(7) != null && !terms.contains(list.get(7).trim())) {
            ERRORS.push("error 第" + i + "行学期[" + list.get(7).trim() + "]不在" + terms.toString()
                + "中");
          }
          if (list.get(8) != null && !topics.contains(list.get(8).trim())) {
            ERRORS.push("error 第" + i + "行领域[" + list.get(8).trim() + "]不在" + topics.toString()
                + "中");
          }

          // 检测一级搜索分类和二级搜索分类的关系是否完整 第10列是搜索一级分类 第11列是搜索二级分类
          if (list.get(10) != null && catagories.contains(list.get(10).trim())
              && list.get(4) != null && !list.get(10).trim().equals(list.get(4).trim())) {
            ERRORS.push("error 第" + i + "行一级搜索不合格，一级搜索在" + catagories.toString()
                + "中时,要和素材类别一致,否则会造成重复显示");
          }
          if (list.get(10) == null || !mime.containsKey(list.get(10).trim())) {
            ERRORS.push("error 第" + i + "行一级搜索不合格，请检测一级搜索是否在[活动设计/图片/动画/视频/音频/电子书]中");
          } else {
            if (list.size() > 11 && list.get(11) != null) {
              // 有二级分类
              if (!searchGradeRelation.get(list.get(10).trim()).contains(list.get(11).trim())) {
                // 但和一级分类不符合
                ERRORS.push("error 第" + i
                    + "行一级搜索和二级搜索对应关系不合格，请检测一级搜索是否在[活动设计/图片/动画/视频/音频/电子书]中，且对应关系是否符合19个分类图中的对应关系");
              }

              if (list.get(4) != null && !list.get(11).trim().equals(list.get(4))
                  && catagories.contains(list.get(11).trim())) {
                // 二级分类和素材类别不一样且二级分类又在18个类别中
                ERRORS.push("error 第" + i + "二级分类和素材类别不一样且二级分类又在18个类别中，会造成重复显示");
              }
            }
          }
        }
      }

      // 打印警告
      if (repeatInfo.length() > 0) {
        for (int i = 0; i < repeatInfo.length(); i++) {
          System.out.println(repeatInfo.getString(i));
        }
        System.out.println("\r\n==========EXCEL文件采集信息出现以上警告，请确认数据的正确性=======\r\n ");
      }

      if (ERRORS.length() > 0) {
        for (int i = 0; i < ERRORS.length(); i++) {
          System.out.println(ERRORS.getString(i));
        }
        System.out.println("\r\n==========EXCEL文件采集信息出现以上错误，数据初始化终止=======\r\n ");
      } else {
        for (int i = 0; i < rows; i++) {
          if (i == 0) {
            continue;
          }
          List<String> list = data.get(i);
          /**
           * 创建文件
           */
          JsonObject file = Json.createObject();
          file.set(Constant.KEY_ID, list.get(0).trim());// 编号
          file.set(Constant.KEY_NAME, list.get(1).trim());// 文件名称
          if (replace) {
            file.set(Constant.KEY_URL, getTruePath(list.get(2).trim()));// 文件路径
          } else {
            file.set(Constant.KEY_URL, list.get(2).trim());
          }
          file.set(Constant.KEY_CONTENTLENGTH, getContentLenght(list.get(2).trim()));// 文件长度
          file.set(Constant.KEY_CONTENTTYPE, getContentTypeBySuffix(list.get(2).trim()));// 文件contentType
          if (list.size() > 3 && list.get(3) != null) {
            if (replace) {
              file.set(Constant.KEY_THUMBNAIL, getTruePath(list.get(3).trim()));// 缩略图路径
            } else {
              file.set(Constant.KEY_THUMBNAIL, list.get(3).trim());
            }
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
              String tag = list.get(j).trim();
              if (tag.matches("^\\d{4}.*")) {
                tag = tag.substring(4, tag.length());
              }
              JsonObject relationFile =
                  Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
                      list.get(0).trim()).set(Constant.KEY_LABEL, tag.trim());
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
                    Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
                        tag.trim()).set(Constant.KEY_LABEL, list.get(j).trim());
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
   * 通过后缀获取文件MIME
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
