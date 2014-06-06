package com.goodow.drive.test;

import junit.framework.TestCase;

public class DataCheckTest extends TestCase {
  private static final String VIR1_PATH = "attachments/sd1";// 模拟的sd1路径
  private static final String VIR2_PATH = "attachments/sd2";// 模拟的sd2路径
  private static String testResPath = "";
  public void test() {
      testResPath = System.getProperty("respath", testResPath);
      InitDataFormExcel.factory(VIR1_PATH, VIR2_PATH, testResPath);
  }
}
