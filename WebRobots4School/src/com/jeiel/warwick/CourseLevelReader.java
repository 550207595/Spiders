package com.jeiel.warwick;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class CourseLevelReader {
	//http://www2.warwick.ac.uk/study/undergraduate/apply/language/
	//Band A	6.5 including minimum 6.0 in each component
	//Band B	6.0 including minimum 5.5 in each component
	//Band C	7.0 including minimum 6.5 in each component
	public static final String file = "warwick_ielts_ug.xls";
	public static void main(String[] args) throws FileNotFoundException, IOException {
		for(Entry<String, String>entry:getCourseLevelMap().entrySet()){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
	}
	public static Map<String, String> getCourseLevelMap(){
		Map<String, String> courseLevelMap=new HashMap<String, String>();
		try{
			HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(file));
			HSSFSheet sheet = book.getSheetAt(0);
			HSSFRow row = null;
			for(int i = 0; i <= sheet.getLastRowNum(); i++){
				row = sheet.getRow(i);
				/*System.out.println("{\""+row.getCell(0).getStringCellValue()+"\",\""+
						row.getCell(1).getStringCellValue()+"\",\""+
						row.getCell(2).getStringCellValue()+"\",\""+
						row.getCell(3).getStringCellValue()+"\"}");*/
				/*System.out.println("{\""+row.getCell(2).getStringCellValue()+"\",\""+
						row.getCell(3).getStringCellValue()+"\"}");*/
				courseLevelMap.put(row.getCell(2).getStringCellValue(), row.getCell(3).getStringCellValue());
			}
			book.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return courseLevelMap;
	}
}
