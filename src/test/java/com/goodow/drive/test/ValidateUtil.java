package com.goodow.drive.test;


import com.goodow.realtime.json.*;
import com.goodow.realtime.json.util.Xml;
import com.goodow.realtime.json.util.Yaml;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Logger;


/**
 * Created by gj on 6/23/14.
 * 校验Excel格式
 */
public class ValidateUtil {

  /**
   * 日志输出对象
   */
  public static final Logger log = Logger.getLogger(ValidateUtil.class.getName());

  /**
   * 保存错误信息对象
   */
  private static final JsonArray ERRORS = Json.createArray();

  /**
   * 保存警告信息对象
   */
  private static final JsonArray WARNS = Json.createArray();

  /**
   * 保存yaml对象转换的json对象
   */
  private static JsonObject yamlJsonObj = null;

  /**
   * 保存活动ID和文件显示名称对象，用于校验是否出现重复
   */
  private static final Map<String, String> activityIdAndFileName = new HashMap<String, String>();

  /**
   * 搜索分类数组长度
   */
  private static final int SEARCHCLASSIFYLENGTH = 5;

  /**
   * 读取yaml配置文件得到JSON对象
   */
  private static JsonObject loadYaml() {

    try {
      String yaml = new Scanner(ValidateUtil.class.getResourceAsStream("/validate.yaml"))
          .useDelimiter("\\Z").next();
      yamlJsonObj = Yaml.parse(yaml);
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
    return yamlJsonObj;
  }


  /**
   * 校验
   *
   * @param data
   * @param excelFileFolderPath
   * @return
   */
  public static boolean validateFun(List<List<String>> data, String excelFileFolderPath) {

    //定义变量标识校验结果
    boolean bool = true;
    //重置错误警告信息
    ERRORS.clear();
    WARNS.clear();
    try {
      //获取yaml配置文件信息并转换成json对象
      loadYaml();
      //校验开始
      if (data != null && data.size() > 0) {
        //判断excel表结构正确行
        bool = check_excelFormat(data.get(0));
        if (bool) {
          for (int i = 1; i < data.size(); i++) {//第0行为表格列表头部不做判断
            //定义行号
            int line = (i + 1);
            //获取当前行数据
            List<String> dataList = data.get(i);
            log.info("第" + line + "行数据：" + dataList.toString());
            //校验活动编号和文件显示名称：活动编号 0列，文件显示名称 16列（平台用）
            check_activityIdAndFileName(dataList.get(0), dataList.get(16), line);
            //校验文件路径是否存在以及文件后缀名是否合法 2列
            check_filePathAndFileExtension(dataList.get(2), excelFileFolderPath, line);
            //校验缩略图文件路径 3列
            check_thumbnailsPath(dataList.get(3), excelFileFolderPath, line);
            //校验素材类别 4列
            check_materialType(dataList.get(4), line);
            //校验分类 5列
            check_classify(dataList.get(5), line);
            //校验班级 6列
            check_class(dataList.get(6), line);
            //校验学期 18列（平台用）
            check_term(dataList.get(18), line);
            //校验主题或领域 17列（平台用）
            check_themesOrAreas(dataList.get(17), line);
            //组合搜索分类数组
            String[] searchClassify = new String[SEARCHCLASSIFYLENGTH];
            searchClassify[0] = null != dataList.get(10) ? dataList.get(10).trim() : "";
            searchClassify[1] = null != dataList.get(11) ? dataList.get(11).trim() : "";
            searchClassify[2] = null != dataList.get(12) ? dataList.get(12).trim() : "";
            searchClassify[3] = null != dataList.get(13) ? dataList.get(13).trim() : "";
            searchClassify[4] = null != dataList.get(14) ? dataList.get(14).trim() : "";
            //校验搜索分类
            check_searchClassify(searchClassify, line);
          }
        }
      } else {
        System.out.println("Excel数据为空！！！");
      }
      //打印错误信息及警告信息
      if (WARNS.length() > 0) {
        System.out.println("==========================打印警告信息-开始===========================");
        for (int k = 0; k < WARNS.length(); k++) {
          System.out.println(WARNS.getString(k));
        }
        System.out.println("==========================打印警告信息-结束===========================");
      } else {
        System.out.println("=============================没有警告信息=============================");
      }

      if (ERRORS.length() > 0) {
        System.out.println("==========================打印错误信息-开始===========================");
        for (int k = 0; k < ERRORS.length(); k++) {
          System.out.println(ERRORS.getString(k));
        }
        System.out.println("==========================打印错误信息-结束===========================");
        bool = false;
      } else {
        System.out.println("==========================Excle数据校验通过===========================");
        bool = true;
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
    return bool;
  }

  /**
   * 判断excel表格式是否正确
   *
   * @param dataList
   * @return
   */
  public static boolean check_excelFormat(List<String> dataList) {
    boolean bool = true;
    try {
      int size = dataList.size();
      if (size != 20) {
        //如果数据长度小于20将空列补全
        if (size < 20) {
          int k = (20 - size);
          for (int i = 0; i < k; i++) {
            dataList.add("");
          }
        }
        bool = false;
        ERRORS.push("Excel格式错误");
        if (dataList.get(0) == null || "".equals(dataList.get(0)) ||
            !"活动编号".equals(dataList.get(0).trim())) {
          ERRORS.push("第1列应为[活动编号]:A");
        }
        if (dataList.get(1) == null || "".equals(dataList.get(1)) ||
            !"文件显示名称".equals(dataList.get(1).trim())) {
          ERRORS.push("第2列应为[文件显示名称]:B");
        }
        if (dataList.get(2) == null || "".equals(dataList.get(2)) ||
            !"文件路径".equals(dataList.get(2).trim())) {
          ERRORS.push("第3列应为[文件路径]:C");
        }
        if (dataList.get(3) == null || "".equals(dataList.get(3)) ||
            !"缩略图文件路径".equals(dataList.get(3).trim())) {
          ERRORS.push("第4列应为[缩略图文件路径]:D");
        }
        if (dataList.get(4) == null || "".equals(dataList.get(4)) ||
            !"素材类别".equals(dataList.get(4).trim())) {
          ERRORS.push("第5列应为[素材类别]:E");
        }
        if (dataList.get(5) == null || "".equals(dataList.get(5)) ||
            !"分类".equals(dataList.get(5).trim())) {
          ERRORS.push("第6列应为[分类]:F");
        }
        if (dataList.get(6) == null || "".equals(dataList.get(6)) ||
            !"班级".equals(dataList.get(6).trim())) {
          ERRORS.push("第7列应为[班级]:G");
        }
        if (dataList.get(7) == null || "".equals(dataList.get(7)) ||
            !"学期".equals(dataList.get(7).trim())) {
          ERRORS.push("第8列应为[学期]:H");
        }
        if (dataList.get(8) == null || "".equals(dataList.get(8)) ||
            !"主题或领域".equals(dataList.get(8).trim())) {
          ERRORS.push("第9列应为[主题或领域]:I");
        }
        if (dataList.get(9) == null || "".equals(dataList.get(9)) ||
            !"活动".equals(dataList.get(9).trim())) {
          ERRORS.push("第10列应为[活动]:J");
        }
        if (dataList.get(10) == null || "".equals(dataList.get(10)) ||
            !"搜索一级分类".equals(dataList.get(10).trim())) {
          ERRORS.push("第11列应为[搜索一级分类]:K");
        }
        if (dataList.get(11) == null || "".equals(dataList.get(11)) ||
            !"搜索二级分类".equals(dataList.get(11).trim())) {
          ERRORS.push("第12列应为[搜索二级分类]:L");
        }
        if (dataList.get(12) == null || "".equals(dataList.get(12)) ||
            !"搜索三级分类".equals(dataList.get(12).trim())) {
          ERRORS.push("第13列应为[搜索三级分类]:M");
        }
        if (dataList.get(13) == null || "".equals(dataList.get(13)) ||
            !"搜索四级分类".equals(dataList.get(13).trim())) {
          ERRORS.push("第14列应为[搜索四级分类]:N");
        }
        if (dataList.get(14) == null || "".equals(dataList.get(14)) ||
            !"搜索五级分类".equals(dataList.get(14).trim())) {
          ERRORS.push("第15列应为[搜索五级分类]:O");
        }
        if (dataList.get(15) == null || "".equals(dataList.get(15)) ||
            !"搜索分类关键字".equals(dataList.get(15).trim())) {
          ERRORS.push("第16列应为[搜索分类关键字]:P");
        }
        if (dataList.get(16) == null || "".equals(dataList.get(16)) ||
            !"文件显示名称(平台用)".equals(dataList.get(16).trim())) {
          ERRORS.push("第17列应为[文件显示名称(平台用)]:Q");
        }
        if (dataList.get(17) == null || "".equals(dataList.get(17)) ||
            !"主题或领域(平台用)".equals(dataList.get(17).trim())) {
          ERRORS.push("第18列应为[主题或领域(平台用)]:R");
        }
        if (dataList.get(18) == null || "".equals(dataList.get(18)) ||
            !"学期(平台用)".equals(dataList.get(18).trim())) {
          ERRORS.push("第19列应为[学期(平台用)]:S");
        }
        if (dataList.get(19) == null || "".equals(dataList.get(19)) ||
            !"素材类别".equals(dataList.get(19).trim())) {
          ERRORS.push("第20列应为[素材类别]:T");
        }
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
    return bool;
  }

  /**
   * 校验活动编号和文件显示名称
   *
   * @param activityId
   * @param filename
   * @param line
   * @return
   */
  private static void check_activityIdAndFileName(String activityId, String filename, int line) {

    log.info("校验活动编号和文件显示名称");
    log.info("活动编号：" + activityId + "   文件显示名称：" + filename);

    try {
      if (activityId == null || "".equals(activityId.trim())) {
        ERRORS.push("error : 第" + line + "行 活动编号不能为空");
      }
      if (filename == null || "".equals(filename.trim())) {
        ERRORS.push("error : 第" + line + "行 文件显示内容不能为空");
      }
      if (activityIdAndFileName.containsValue(filename)) {
        WARNS.push("warn : 第" + line + "行 文件显示名称重复");
      }
      if (activityIdAndFileName.containsKey(activityId)) {
        ERRORS.push("error : 第" + line + "行 活动编号重复");
      } else {
        activityIdAndFileName.put(activityId, filename);
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验文件路径是否存在以及文件扩展名是否合法
   *
   * @param filePath
   * @param folderPath
   * @param line
   * @return
   */
  private static void check_filePathAndFileExtension(String filePath, String folderPath, int line) {

    log.info("校验文件路径是否存在以及文件扩展名是否合法");
    log.info("文件路径：" + filePath);

    //文件后缀名
    String fileExtension = "";
    try {

      if (filePath == null || "".equals(filePath.trim())) {
        ERRORS.push("error : 第" + line + "行 文件路径为空");
      }
      if (filePath.indexOf(".") <= 0) {
        ERRORS.push("error : 第" + line + "行 文件路径错误，缺少文件扩展名");
      } else {
        fileExtension = filePath.substring((filePath.indexOf(".") + 1), filePath.length());
        log.info("文件扩展名：" + fileExtension);
      }
      //获取yamlJsonObj中文件扩展名类别
      JsonArray obj = yamlJsonObj.get("fileExtension");
      if (obj.indexOf(fileExtension) < 0) {
        ERRORS.push("error : 第" + line + "行 文件路径错误，" +
            "文件扩展名[" + fileExtension + "]不在" + obj.toString() + "中");
      }
      if (!new File(folderPath + filePath).exists()) {
        ERRORS.push("error : 第" + line + "行 文件路径错误，文件路径不存在");
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验缩略图文件路径
   *
   * @param thumbnailsPath
   * @param folderPath
   * @param line
   * @return
   */
  private static void check_thumbnailsPath(String thumbnailsPath, String folderPath, int line) {

    log.info("校验缩略图文件路径");
    log.info("缩略图文件路径：" + thumbnailsPath);

    try {
      if (thumbnailsPath == null || "".equals(thumbnailsPath.trim())) {
        WARNS.push("warn : 第" + line + "行 缩略图文件路径为空");
      }
      if (!new File(folderPath + thumbnailsPath).exists()) {
        ERRORS.push("error : 第" + line + "行 缩略图文件路径错误，文件路径不存在");
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }

  }

  /**
   * 校验素材分类
   *
   * @param materialType
   * @param line
   * @return
   */
  private static void check_materialType(String materialType, int line) {

    log.info("校验素材分类");
    log.info("素材分类：" + materialType);

    try {
      if (materialType == null || "".equals(materialType.trim())) {
        ERRORS.push("error : 第" + line + "行 素材分类为空");
      }
      //获取yamlJsonObj中文件扩展名类别
      JsonArray obj = yamlJsonObj.get("materialType");
      if (obj.indexOf(materialType) < 0) {
        ERRORS.push("error : 第" + line + "行 " +
            "素材分类[" + materialType + "]不在" + obj.toString() + "中");
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验分类
   *
   * @param classify
   * @param line
   * @return
   */
  private static void check_classify(String classify, int line) {

    log.info("校验分类");
    log.info("分类：" + classify);

    try {
      if (classify == null || "".equals(classify.trim())) {
        ERRORS.push("error : 第" + line + "行 分类为空");
      }
      //获取yamlJsonObj中分类
      JsonArray obj = yamlJsonObj.get("classify");
      if (obj.indexOf(classify) < 0) {
        ERRORS.push("error : 第" + line + "行 分类[" + classify + "]不在" + obj.toString() + "中");
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验班级
   *
   * @param classes
   * @param line
   * @return
   */
  private static void check_class(String classes, int line) {

    log.info("校验班级");
    log.info("班级：" + classes);

    try {
      if (classes == null || "".equals(classes.trim())) {
        WARNS.push("warn : 第" + line + "行 班级为空");
      }

      if (classes != null && !"".equals(classes.trim())) {
        //获取yamlJsonObj中班级
        JsonArray obj = yamlJsonObj.get("class");
        if (obj.indexOf(classes) < 0) {
          ERRORS.push("error : 第" + line + "行 班级[" + classes + "]不在" + obj.toString() + "中");
        }
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验学期
   *
   * @param term
   * @param line
   * @return
   */
  private static void check_term(String term, int line) {

    log.info("校验学期");
    log.info("学期：" + term);

    try {
      if (term == null || "".equals(term.trim())) {
        WARNS.push("warn : 第" + line + "行 学期为空");
      }

      if (term != null && !"".equals(term.trim())) {
        //获取yamlJsonObj中学期
        JsonArray obj = yamlJsonObj.get("term");
        if (obj.indexOf(term) < 0) {
          ERRORS.push("error : 第" + line + "行 学期[" + term + "]不在" + obj.toString() + "中");
        }
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验主题或领域
   *
   * @param themesOrAreas
   * @param line
   * @return
   */
  private static void check_themesOrAreas(String themesOrAreas, int line) {

    log.info("校验主题或领域");
    log.info("主题或领域：" + themesOrAreas);

    try {
      if (themesOrAreas == null || "".equals(themesOrAreas.trim())) {
        ERRORS.push("error : 第" + line + "行 主题或领域为空");
      }
      //获取yamlJsonObj中主题或领域
      JsonArray obj = yamlJsonObj.get("themesOrAreas");
      if (obj.indexOf(themesOrAreas) < 0) {
        ERRORS.push("error : 第" + line + "行 " +
            "主题或领域[" + themesOrAreas + "]不在" + obj.toString() + "中");
      }
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
  }

  /**
   * 校验搜索分类
   *
   * @return
   */
  private static void check_searchClassify(String[] searchClassify, int line) {

    log.info("校验搜索分类");
    StringBuilder sb = new StringBuilder();
    sb.append("搜索分类：[");
    if (searchClassify != null && searchClassify.length > 0) {
      for (String str : searchClassify) {
        if (!"".equals(str)) {
          sb.append(" '" + str + "' ");
        } else {
          sb.append(" 'null' ");
        }
      }
    }
    sb.append("]");
    log.info("搜索分类：" + sb.toString());

    //搜索一级分类不能为空
    if (searchClassify[0] == null || "".equals(searchClassify[0])) {
      ERRORS.push("error : 第" + line + "行 搜索一级分类不能为空");
    }
    //校验搜索分类
    if (searchClassify[0] != null || !"".equals(searchClassify[0])) {
      //获取搜索分类json对象
      JsonElement jsonElement = yamlJsonObj.get("searchClassify");
      for (int i = 0; i < SEARCHCLASSIFYLENGTH; i++) {
        //获取当前搜索分类内容
        String nowSearchClassify = searchClassify[i];
        if (!"".equals(nowSearchClassify)) {
          if (jsonElement.isObject()) {//当前json对象是map
            //转型
            JsonObject jsonObject = (JsonObject) jsonElement;
            //获取map的keys
            JsonArray keysArray = jsonObject.keys();
            //判断当前分类是否正确
            if (keysArray.indexOf(nowSearchClassify) < 0) {
              ERRORS.push("error : 第" + line + "行 " +
                  "搜索" + (i + 1) + "级分类[" + nowSearchClassify + "] 不在" +
                  keysArray.toString() + "中");
              break;
            }
            //获取下一级分类内容
            jsonElement = jsonObject.get(nowSearchClassify);
          } else {//当前json对象是array
            //转型
            JsonArray jsonArray = (JsonArray) jsonElement;
            //判断当前分类是否正确
            if (jsonArray.length() == 0 && !"".equals(nowSearchClassify)) {
              ERRORS.push("error : 第" + line + "行 " +
                  "搜索" + (i) + "级分类下不应有" + (i + 1) + "分类[" + nowSearchClassify + "]");
            }

            if (jsonArray.indexOf(nowSearchClassify) < 0) {
              ERRORS.push("error : 第" + line + "行 " +
                  "搜索" + (i + 1) + "级分类[" + nowSearchClassify + "] 不在" +
                  jsonArray.toString() + "中 index:" + jsonArray.indexOf(nowSearchClassify));
              break;
            }
          }
        }
      }
    }
  }


  /**
   * 处理Exception信息方法
   *
   * @param ex
   * @return
   */
  private static String getExceptionInfo(Exception ex) {
    String exception = "";
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      ex.printStackTrace(pw);
      exception = "\r\n" + sw.toString() + "\r\n";
    } catch (Exception e) {
      log.info(getExceptionInfo(e));
    }
    return exception;
  }

  /**
   * 测试
   *
   * @param args
   */
  public static void main(String[] args) {

  }

}
