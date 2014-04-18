package com.goodow.drive.server.utils;

public class MyConstant {

  // ES中index的名称
  public static final String ES_INDEX = "goodow";

  /**
   * ES中type的名称
   */
  // 播放统计
  public static final String ES_TYPE_T_PLAYER = "T_PLAYER";
  // 地理校验
  public static final String ES_TYPE_T_GEO = "T_GEO";
  // excel中的文件
  public static final String ES_TYPE_T_FILE = "T_FILE";
  // 文件
  public static final String ES_TYPE_ATTACHMENT = "attachment";

  /**
   * 服务器监听的地址
   */
  // 前缀
  public static final String ADDR = "sid.drive.";
  // 初始化数据库
  public static final String ADDR_INIT = "db.initial";
  // 文件和标签数据导入
  public static final String ADDR_FILE = "db";
  // 播放统计
  public static final String ADDR_PLAYER = "player.analytics";
  // 地理校验
  public static final String ADDR_GEO = "auth";

  // 文件地址
  public static final String ADDR_ATTACHMENT = ADDR + "attachment";
}