package com.jeiel.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.jeiel.entity.MajorForCollection;

public class ExcelGenerator {
	public static final int SCHOOL=0;
	public static final int LEVEL=1;
	public static final int TITLE=2;
	public static final int TYPE=3;
	public static final int APPLICATION_FEE=4;
	public static final int TUITION_FEE=5;
	public static final int ACADEMIC_ENTRY_REQUIREMENT=6;
	public static final int IELTS_AVERAGE_REQUIREMENT=7;
	public static final int IELTS_LOWEST_REQUIREMENT=8;
	public static final int STRUCTURE=9;
	public static final int LENGTH_MONTHS=10;
	public static final int MONTH_OF_ENTRY=11;
	public static final int SCHOLARSHIP=12;
	public static final int URL=13;
	public static final int COUNT_RESULT=14;
	
	
	private HSSFWorkbook book=null;
	private HSSFSheet sheet =null; 
	private HSSFRow row=null;
	private List<MajorForCollection> majorList=null;
	
	public ExcelGenerator(List<MajorForCollection> majorList){
		this.majorList = majorList;
	}
	
	public void initExcelWriter()
			throws Exception {
		

        book = new HSSFWorkbook(); 

         
		if(sheet==null){
			sheet = book.createSheet("Sheet1");
		}
		row = sheet.createRow((short) 0);

        row.createCell(0).setCellValue("School");
        row.createCell(1).setCellValue("Level");
        row.createCell(2).setCellValue("Title");
        row.createCell(3).setCellValue("Type");
        row.createCell(4).setCellValue("Application Fee");
        row.createCell(5).setCellValue("Tuition Fee");
        row.createCell(6).setCellValue("Academic Entry Requirement");
        row.createCell(7).setCellValue("IELTS Average Requirement");
        row.createCell(8).setCellValue("IELTS Lowest Requirement");
        row.createCell(9).setCellValue("Structure");
        row.createCell(10).setCellValue("Length (months)");
        row.createCell(11).setCellValue("Month of Entry");
        row.createCell(12).setCellValue("Scholarship");
        row.createCell(13).setCellValue("Url"); 
	}
	
	public void addToSheet(int rowNum, int col, String content)
		throws Exception {
		if(row==null||row.getRowNum()!=rowNum){
			row = sheet.createRow((short)rowNum);
		}
		
		if(col==COUNT_RESULT){
			row.createCell(col).setCellFormula(content);
		}else{
			row.createCell(col).setCellValue(content);
		}
		
	}
	
	public void exportExcel(String fileName) throws Exception {
		for(int row =0;row<majorList.size();row++){
			addToSheet(row+1, SCHOOL, majorList.get(row).getSchool());
			addToSheet(row+1, LEVEL, majorList.get(row).getLevel());
			addToSheet(row+1, TITLE, majorList.get(row).getTitle());
			addToSheet(row+1, TYPE, majorList.get(row).getType());
			addToSheet(row+1, APPLICATION_FEE, majorList.get(row).getApplicationFee());
			addToSheet(row+1, TUITION_FEE, majorList.get(row).getTuitionFee());
			addToSheet(row+1, ACADEMIC_ENTRY_REQUIREMENT, majorList.get(row).getAcademicRequirements());
			addToSheet(row+1, IELTS_AVERAGE_REQUIREMENT, majorList.get(row).getIELTS_Avg());
			addToSheet(row+1, IELTS_LOWEST_REQUIREMENT, majorList.get(row).getIELTS_Low());
			addToSheet(row+1, STRUCTURE, majorList.get(row).getStructure());
			addToSheet(row+1, LENGTH_MONTHS, majorList.get(row).getLength());
			addToSheet(row+1, MONTH_OF_ENTRY, majorList.get(row).getMonthOfEntry());
			addToSheet(row+1, SCHOLARSHIP, majorList.get(row).getScholarship());
			addToSheet(row+1, URL, majorList.get(row).getUrl());
			/*addToSheet(row+1, COUNT_RESULT, "IF(COUNT(A"+(row+2)+",B"+(row+2)+",C"+(row+2)+",D"+(row+2)+
													",F"+(row+2)+",G"+(row+2)+",H"+(row+2)+",J"+(row+2)+
													",K"+(row+2)+",L"+(row+2)+")=10,\"\",\"Missed\")");*/
		}
		
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos=new FileOutputStream(file);
		book.write(fos);
		fos.close();
	}

}
