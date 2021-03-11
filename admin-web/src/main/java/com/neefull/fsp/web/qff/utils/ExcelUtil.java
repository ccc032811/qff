package com.neefull.fsp.web.qff.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class ExcelUtil {

    private String[] name;
    private List<Object[]> dataList = new ArrayList<Object[]>();


    public ExcelUtil(String[] name, List<Object[]> dataList) {
        this.dataList = dataList;
        this.name = name;
    }

    public void exportExcel(SXSSFWorkbook workbook, SXSSFSheet sheet, HttpServletResponse response) throws Exception{

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");


        try{
            sheet.setDefaultColumnWidth((short) 30);

            CellStyle columnTopStyle = this.getColumnTopStyle(workbook);
            CellStyle style = this.getStyle(workbook);
            CellStyle dateStyle = this.getDateStyle(workbook);

            SXSSFRow rowRowName = sheet.createRow(0);

            rowRowName.setHeight((short) (25 * 25));

            for(int n=0;n<name.length;n++){
                SXSSFCell cellRowName = rowRowName.createCell(n);
                cellRowName.setCellValue(name[n]);
                cellRowName.setCellStyle(columnTopStyle);
            }

            for(int i=0;i<dataList.size();i++){
                Object[] obj = dataList.get(i);
                SXSSFRow row = sheet.createRow(i + 1);

                row.setHeight((short) (25 * 15));

                for(int j=0; j<obj.length; j++){
                    SXSSFCell cell = null;

                    if(!"".equals(obj[j]) && obj[j] != null){

                        if(matchYear(obj[j].toString())){
                            cell = row.createCell(j);
                            try {
                                cell.setCellValue(simpleDateFormat.parse(obj[j].toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            cell.setCellStyle(dateStyle);
                        }else{
                            cell = row.createCell(j,CellType.STRING);
                            cell.setCellValue(obj[j].toString());
                            cell.setCellStyle(style);
                        }
                    }else {
                        cell = row.createCell(j,CellType.STRING);
                        cell.setCellValue("");
                        cell.setCellStyle(style);
                    }

                }
            }

            if(workbook !=null){
                try {
                    String fileName = "Excel-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
                    String headStr = "attachment; filename=\"" + fileName + "\"";
                    response.setContentType("APPLICATION/OCTET-STREAM");
                    response.setHeader("Content-Disposition", headStr);
                    OutputStream out = response.getOutputStream();
                    workbook.write(out);
                    out.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }


    public CellStyle getColumnTopStyle(SXSSFWorkbook workbook) {

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short)15);
        font.setFontName("宋体");
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(false);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());

        return style;
    }


    public CellStyle getStyle(SXSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short)12);
        font.setFontName("宋体");
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(false);

        return style;
    }


    public CellStyle getDateStyle(SXSSFWorkbook workbook) {

        DataFormat format = workbook.createDataFormat();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short)12);
        font.setFontName("宋体");
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(false);
        style.setDataFormat(format.getFormat("yyyy/MM/dd"));
        return style;
    }


    public static boolean matchYear(String str){
        String regex = "((19|20)[0-9]{2})/(0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01])";
        if(StringUtils.isEmpty(str)){
            return false;
        }
        return Pattern.compile(regex).matcher(str).matches();
    }


}


