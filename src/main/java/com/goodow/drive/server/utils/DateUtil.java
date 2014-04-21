package com.goodow.drive.server.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 */
public class DateUtil {

  /**
   * 获取当前时间戳，返回毫秒值
   */
  public static String getCurrentTimeMillis() {
    return String.valueOf(System.currentTimeMillis());
  }

  /**
   * yyyy-MM-dd hh:mm:ss
   * 
   * @return
   */
  public static String getDate() {
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");// 设置日期时间格式
    return df.format(date);
  }

  /**
   * 解析时间戳
   * 
   * @author:DingPengwei
   * @date:Apr 21, 2014 3:14:48 PM
   * @param timestamp
   * @return
   */
  public static String parseTimestamp(long timestamp) {
    Date date = new Date(timestamp);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    return format.format(date);
  }
}
