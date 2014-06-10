package com.goodow.drive.test;

import junit.framework.TestCase;

public class DataCheckTest extends TestCase {
  private static final String VIR1_PATH = "attachments/sd1";// 模拟的sd1路径
  private static final String VIR2_PATH = "attachments/sd2";// 模拟的sd2路径
  private static String testResPath = "";
  private static String fileName= "";
  public void test() {
      testResPath = System.getProperty("respath", testResPath);
      fileName = System.getProperty("filename", fileName);
      InitDataFormExcel.factory(VIR1_PATH, VIR2_PATH, testResPath,fileName);
  }
}
