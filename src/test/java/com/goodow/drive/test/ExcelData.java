package com.goodow.drive.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 读取Excel工具
 * 
 * @author leiguorui
 * 
 */
public class ExcelData {
  public static final Logger log = Logger.getLogger(ExcelData.class.getName());
  /**
   * 获取行中单元格的多少
   * 
   * @param firstRow
   * @return
   */
  public static int getCellsCount(XSSFRow firstRow) {
    int cellsCount = 0;
    for (int n = firstRow.getPhysicalNumberOfCells(); n > 0; n--) {
      XSSFCell cell = firstRow.getCell(n);
      if (cell != null && cell.toString().trim().length() > 0) {
        cellsCount = n;
        break;
      }
    }
    return cellsCount + 1;
  }

  public static List<List<String>> getExcelData(String path) throws Exception {
    log.info("start load data...........");
    List<List<String>> datas = new ArrayList<List<String>>();
    XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(path));

    // 遍历sheet
    for (int k = 0; k < 1; k++) {
      XSSFSheet sheet = wb.getSheetAt(k);
      int rows = sheet.getPhysicalNumberOfRows();
      int cells = getCellsCount(sheet.getRow(0));
      // 遍历行
      for (int r = 0; r < rows; r++) {
        List<String> rowlist = new ArrayList<String>();
        int blankCell = 0; // 空白单元格的个数
        XSSFRow row = sheet.getRow(r);
        if (row == null) {
          continue;
        }
        // 遍历单元格
        for (int c = 0; c < cells; c++) {
          String cell = null;
          if (row.getCell(c) == null || row.getCell(c).toString().trim().length() == 0) {
            blankCell++;
          } else {

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            CellValue cellValue = evaluator.evaluate(row.getCell(c));

            switch (cellValue.getCellType()) {
              case Cell.CELL_TYPE_BOOLEAN:
                cell = cellValue.getBooleanValue() + "";
                break;
              case Cell.CELL_TYPE_NUMERIC:
                cell = cellValue.getNumberValue() + "";
                cell = cell.substring(0, cell.indexOf("."));
                break;
              case Cell.CELL_TYPE_STRING:
                cell = cellValue.getStringValue() + "";
                break;
              case Cell.CELL_TYPE_BLANK:
                break;
              case Cell.CELL_TYPE_ERROR:
                break;

              // CELL_TYPE_FORMULA will never happen
              case Cell.CELL_TYPE_FORMULA:
                break;
            }
          }
          rowlist.add(cell);
        }
        if (blankCell == cells) { // 如果空白单元格的个数与单元格总数相等，则说明这行为空白，不添加到datas
          continue;
        } else {
          datas.add(rowlist);
        }
      }
    }
    log.info("end load data...........");
    return datas;
  }

}
