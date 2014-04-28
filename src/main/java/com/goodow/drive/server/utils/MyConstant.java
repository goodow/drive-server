package com.goodow.drive.server.utils;

public class MyConstant {

  // ES中index的名称
  public static final String ES_INDEX = "drive_test";

  /**
   * ES中type的名称
   */
  // 播放统计
  public static final String ES_TYPE_PLAYER = "attachmentActivity";
  // 地理校验
  public static final String ES_TYPE_GEO = "deviceActivity";
  // device
  public static final String ES_TYPE_DEVICE = "device";
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
  public static final String ADDR_PLAYER = ADDR + "player.analytics";
  // 开关机统计
  public static final String ADDR_SYSTEM = ADDR + "systime.analytics";
  // 地理校验
  public static final String ADDR_GEO = ADDR + "auth";
  // 文件地址
  public static final String ADDR_ATTACHMENT = ADDR + "attachment";

}
