package com.jeiel.test;

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

import java.util.*;

public class POIReadAndPost {
	
	public static String StructureDir="C:\\Users\\Administrator\\Desktop\\wyl\\structure_pgt";

	public static String filepath="C:\\Users\\Administrator\\Desktop\\wyl\\gen_data_strath_pgt.xls";
	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws IOException{
		System.out.println(getStructureFromFile("structure_1.txt"));
	}
	public static List<Major> getData(String[] args) throws Exception  {
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
            //major.setStructure(splitStructure(String.valueOf(hssfRow.getCell(9))));
            major.setStructure(splitStructure(getStructureFromFile(String.valueOf(hssfRow.getCell(9)))));
            major.setLength(String.valueOf(hssfRow.getCell(10)));
            major.setMonthOfEntry(String.valueOf(hssfRow.getCell(11)));
            //major.setScholarship(String.valueOf(hssfRow.getCell(10)));
            list.add(major);
        }
        hssfWorkbook.close();
        is.close();
        System.out.println("rowCount:"+list.size());
        return list;

	}
	
	public static String getStructureFromFile(String fileName) throws IOException{
		File file=new File(StructureDir,fileName);
		FileInputStream fis=new FileInputStream(file);
		byte[] bytes=new byte[fis.available()];
		fis.read(bytes);
		fis.close();
		return new String(bytes,"utf8");
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
			if(line.equals("Year 1")||line.equals("Year 2")||line.equals("Year 3")||line.equals("Year 4")||line.equals("Final Year"))//Final Year

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
	
}


