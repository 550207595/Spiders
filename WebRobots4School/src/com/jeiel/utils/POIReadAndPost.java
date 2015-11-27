package com.jeiel.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.jeiel.entity.Major;

import java.util.*;

public class POIReadAndPost {
	private static String SCHOOL_NAME = Add.SCHOOL_NAME;
	public static String filepath="gen_data_"+SCHOOL_NAME+"_ug_modified.xls";
	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws IOException{
		try {
			getData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<Major> getData() throws Exception  {
		// TODO Auto-generated method stub
		
		InputStream is = new FileInputStream(filepath);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);//The first Sheet.
        
        List<Major> list=new ArrayList<Major>();
        Major major;
            // ѭ����Row
        for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
        	
            HSSFRow hssfRow = hssfSheet.getRow(rowNum);
            if (hssfRow == null) {
                continue;
            }
            major=new Major();

//0.School;1.Level;2.Title;3.Type;4.ApplicationFee;5.Tuition Fee;6.Academic Requirements;
//7.IELTS Average;8.IELTS Low;9.Structure;10.Length(month);11.Month of Entry;12.Scholarship                
            major.setSchool(String.valueOf(hssfRow.getCell(0)));
            major.setLevel(String.valueOf(hssfRow.getCell(1)));
            major.setTitle(String.valueOf(hssfRow.getCell(2)));
            major.setType(String.valueOf(hssfRow.getCell(3)));
            major.setApplicationFee(String.valueOf(hssfRow.getCell(4)));
            major.setTuitionFee(String.valueOf(hssfRow.getCell(5)));
            major.setAcademicRequirements(String.valueOf(hssfRow.getCell(6)));
            major.setIELTS_Avg(String.valueOf(hssfRow.getCell(7)));
            major.setIELTS_Low(String.valueOf(hssfRow.getCell(8)));
            major.setStructure(splitStructure(String.valueOf(hssfRow.getCell(9))));
            major.setLength(String.valueOf(hssfRow.getCell(10)));
            major.setMonthOfEntry(String.valueOf(hssfRow.getCell(11)));
            major.setScholarship(splitScholarship(String.valueOf(hssfRow.getCell(12)),major.getTuitionFee()));
            list.add(major);
            //ug System.out.println("{\""+major.getSchool()+"|"+major.getTitle()+"|"+major.getType()+"|"+(major.getTuitionFee().indexOf(".")<0?major.getTuitionFee():major.getTuitionFee().substring(0, major.getTuitionFee().indexOf(".")))+"|"+major.getLength()+"\",\""+hssfRow.getCell(13).getStringCellValue()+"\"},");
            //pgt System.out.println("{\""+major.getSchool()+"|"+major.getTitle()+"|"+major.getType()+"|"+(major.getTuitionFee().indexOf(".")<0?major.getTuitionFee():major.getTuitionFee().substring(0, major.getTuitionFee().indexOf(".")))+"\",\""+hssfRow.getCell(13).getStringCellValue()+"\"},");
        }
        hssfWorkbook.close();
        is.close();
        System.out.println("rowCount:"+list.size());
        return list;

	}
	
	
	public static LinkedHashMap<String,String> splitStructure(String structure) throws Exception
	{
		LinkedHashMap<String,String> result=new LinkedHashMap<String,String>();
		FileOutputStream o=new FileOutputStream(new File("d:/temp.txt"));
		o.write(structure.getBytes());
		o.close();
		BufferedReader fis = new BufferedReader(new FileReader("d:/temp.txt"));
		String title="";
		String text="";
		String line="";
		int index=0;
		while((line=fis.readLine())!=null)
		{
			line=line.replace("\t", " ").trim();
			if(line.equals(" ")||line.equals("\r"))
				continue;
			if(line.toLowerCase().equals("first year")||line.toLowerCase().equals("second year")||line.toLowerCase().equals("third year")||line.toLowerCase().equals("fourth year")||
					line.toLowerCase().equals("semester 1")||line.toLowerCase().equals("semester 2")||line.toLowerCase().equals("semester 3")||line.toLowerCase().equals("semester 4")||
					line.toLowerCase().equals("semester 5")||line.toLowerCase().equals("semester 6")||line.toLowerCase().equals("semester 7")||line.toLowerCase().equals("semester 8")||
					line.toLowerCase().equals("year 1")||line.toLowerCase().equals("year 2")||line.toLowerCase().equals("year 3")||line.toLowerCase().equals("year 4")||line.equals("final year"))//Final Year

			{
				if(index!=0)
				{
					
					result.put(title, FilterToHTML.filter(text));
					text="";
				}
				title=line;
				index++;
				
			}
			else
			{
				text+=line+"\n";
			}
		}

		result.put(title, FilterToHTML.filter(text));
		
		return result;
	}
	
	
	public static LinkedHashMap<String,String> splitScholarship(String scholarship,String tuition){
		LinkedHashMap<String,String> result=new LinkedHashMap<String,String>();
		if(scholarship!=null||scholarship.equals(""))return result;
		
		for(String scholarshipItem:scholarship.split(";")){
			if(scholarshipItem.indexOf("$")<0){
				System.out.println(scholarshipItem);
				continue;
			}
			if(scholarshipItem.substring(scholarshipItem.indexOf("$")+1).equals("tuitionfee")){
				result.put(scholarshipItem.substring(0,scholarshipItem.indexOf("$")), 
						tuition);
			}else{
				result.put(scholarshipItem.substring(0,scholarshipItem.indexOf("$")), 
						scholarshipItem.substring(scholarshipItem.indexOf("$")+1));
			}
			
		}
		return result;
	}
	
	
}


