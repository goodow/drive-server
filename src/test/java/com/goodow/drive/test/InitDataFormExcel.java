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
import java.util.logging.Logger;

/**
 * 该文件运行前要和excel表格对照是否是一致的 0列是编号 1列是文件现实名称 2列是文件路径 3列是文件缩略图路径 第4列是素材类别 第5列是主题分类 第6列是班级 第7列是学期
 * 第8列是主题或领域 第9列是活动 第10列是搜索一级分类 第11列是搜索二级分类 第12列是搜索关键字
 *
 * @author dpw
 */
public class InitDataFormExcel {

  public static final Logger log = Logger.getLogger(InitDataFormExcel.class.getName());
  public static final JsonArray FILES_TAGS = Json.createArray();
  private static final Map<String, String> mime = new HashMap<String, String>();
  private static boolean check = true;// 标记要不要校验文件属性
  private static boolean replace = true;// 标记要不要本地替换文件模拟路径
  private static String SD1_PATH = "/mnt/sdcard";// 真实的sd1路径
  private static String SD2_PATH = "/mnt/sdcrad/sd2";// 真实的sd2路径
  private static final String VIR1_PATH = "attachments/sd1";// 模拟的sd1路径
  private static final String VIR2_PATH = "attachments/sd2";// 模拟的sd2路径
  private static final List<String> suffix = new ArrayList<String>();
  private static final JsonArray ERRORS = Json.createArray();
  private static final JsonArray repeatInfo = Json.createArray();
  private static final Map<String, String> repeatIdNames = new HashMap<>();
  // 所有的素材
  private static final List<String> catagories = Arrays.asList("素材-活动设计", "素材-文学作品", "素材-说明文字",
      "素材-背景知识", "素材-乐谱", "素材-教学图片", "素材-动态图", "素材-参考图", "素材-挂图", "素材-轮廓图", "素材-头饰", "素材-手偶",
      "素材-胸牌", "素材-动画", "素材-电子书", "素材-视频", "素材-音频", "素材-音效");
  private static final Map<String, List<String>> searchGradeRelation = new HashMap<>();
  private static final List<String> themes = Arrays.asList("和谐", "托班", "示范课", "入学准备", "安全教育",
      "早期阅读");
  // 所有的班级
  private static final List<String> grades = Arrays.asList("小班", "中班", "大班", "学前班");
  // 所有的学期
  private static final List<String> terms = Arrays.asList("上学期", "下学期");
  // 所有的主题
  private static final List<String> topics = Arrays.asList("健康", "语言", "社会", "科学", "数学", "艺术(音乐)",
      "早期阅读", "艺术(美术)", "我有一个幼儿园", "找找,藏藏", "飘飘,跳跳,滚滚", "我会...", "小小手", "好吃哎", "汽车嘀嘀嘀", "快乐红色",
      "暖暖的...", "思维", "阅读与书写", "习惯与学习品质", "冰波童话", "快乐宝贝", "其他", "幼儿用书", "教师用书", "我想长大", "亲亲热热一家人",
      "小动物来了", "绿绿的...", "快乐的声音", "大大小小", "从头玩到脚", "特别喜欢你", "清凉一夏", "托班");

  static {
    // 文件后缀和MIME的对应关系
    mime.put("mp3", "audio/mpeg");
    mime.put("mp4", "video/mp4");
    mime.put("pdf", "application/pdf");
    mime.put("swf", "application/x-shockwave-flash");
    mime.put("jpeg", "image/jpeg");
    mime.put("jpg", "image/jpeg");
    // 一级搜索和MIME的对应关系
    mime.put("活动设计", "application/pdf");
    mime.put("图片", "image/jpeg");
    mime.put("动画", "application/x-shockwave-flash");
    mime.put("电子书", "application/x-shockwave-flash");
    mime.put("视频", "video/mp4");
    mime.put("音频", "audio/mpeg");

    // 支持文件后缀
    suffix.add(".mp3");
    suffix.add(".mp4");
    suffix.add(".pdf");
    suffix.add(".swf");
    suffix.add(".jpeg");
    suffix.add(".jpg");

    // 搜索的一级和二级对应关系
    searchGradeRelation.put("活动设计", Arrays.asList(new String[]{
        "健康", "托班", "语言", "社会", "数学", "科学", "艺术(美术)", "艺术(音乐)"}));

    searchGradeRelation.put("图片", Arrays.asList(new String[]{
        "教学图片", "参考图", "挂图", "轮廓图", "头饰", "手偶", "胸牌"}));

    searchGradeRelation.put("动画", Arrays
        .asList(new String[]{"文学作品动画", "音乐作品动画", "数学教学动画", "其他动画"}));

    searchGradeRelation.put("视频", Arrays.asList(new String[]{"教学用视频", "教学示范课", "音乐表演视频"}));

    searchGradeRelation.put("音频", Arrays.asList(new String[]{"音乐作品音频", "文学作品音频", "音效"}));

    searchGradeRelation.put("电子书", Arrays.asList(new String[]{"早期阅读", "安全教育", "托班"}));
  }

  public static void factory(String sdCard1, String sdCard2, String path, String fileName) {
    log.info("sdCard1:" + sdCard1 + "  sdCard2:" + sdCard2
        + "  path:" + path + "  fileName:" + fileName);
    FILES_TAGS.clear();
    ERRORS.clear();
    repeatInfo.clear();
    repeatIdNames.clear();
    SD1_PATH = sdCard1;
    SD2_PATH = sdCard2;
    try {
      URL url = null;
      URL root = null;

      String fName = "".equals(fileName) ? "data.xlsx" : fileName;
      log.info("fName:" + fName);
      if (path.length() == 0) {
        url = InitDataFormExcel.class.getResource("/" + fName);
        root = InitDataFormExcel.class.getResource("/");
      } else {
        url = new URL("file:" + path + "/" + fName);
        root = new URL("file:" + path + "/");
      }
      log.info("url:" + url.toString() + "    root:" + root.toString());
      List<List<String>> data = ExcelData.getExcelData(url.getPath());
      int rows = data.size();

      //数据校验
      boolean bool = ValidateUtil.validateFun(data, root.getPath());
      if (bool) {
        for (int i = 0; i < rows; i++) {
          if (i == 0) {
            continue;
          }
          List<String> list = data.get(i);
          JsonObject jsonObject =
              Json.createObject().set("_id", list.get(0).trim()).set("title", list.get(1).trim())
                  .set("contentType", getContentTypeBySuffix(list.get(2).trim())).set(
                  "contentLength", getContentLenght(list.get(2).trim()));
          if (replace) {
            jsonObject.set("url", getTruePath(list.get(2).trim()));// 文件路径
            if (list.size() > 3 && list.get(3) != null) {
              jsonObject.set("thumbnail", getTruePath(list.get(3).trim()));// 缩略图路径
            }
          } else {
            jsonObject.set("url", list.get(2).trim());
            if (list.size() > 3 && list.get(3) != null) {
              jsonObject.set("thumbnail", list.get(3).trim());
            }
          }
          jsonObject.set("tags", Json.createArray().push(list.get(4)).push(list.get(5)).push(
              list.get(6)).push(list.get(18)).push(list.get(17)).push(list.get(9)).push(
              getString(list.get(10))).push(getString(list.get(11))).push(
              getString(list.get(12))).push(getString(list.get(13))).push(
              getString(list.get(14))).push(list.get(15)));

          FILES_TAGS.push(jsonObject);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * null转换""
   *
   * @param str
   * @return
   */
  private static String getString(String str) {
    if (null == str || "null".equals(str)) {
      return "";
    } else {
      return str;
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
