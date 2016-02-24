package com.jeiel.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.entity.MajorForCollection;

public class UtilsSet {
	public static String extractInfoWithRegrex(String regrex, String content){
		String result = "";
		Pattern p = Pattern.compile(regrex);
		Matcher m = p.matcher(content);
		if(m.find()){
			result = m.group();
		}
		return result;
	}
	public static void sortMajorList(List<MajorForCollection>list){
		Collections.sort(list, new Comparator<MajorForCollection>() {

			@Override
			public int compare(MajorForCollection o1, MajorForCollection o2) {
				// TODO Auto-generated method stub
				return o1.getTitle().compareTo(o2.getTitle());
			}
		});
	}
	public static Map<String, Element> mapping(Elements keys, Elements values){
		Map<String, Element>map = new HashMap<String, Element>();
		for(Element key:keys){
			map.put(key.text().trim(), values.first());
			if(values.size()>0){
				values.remove(0);
			}
		}
		return map;
	}
	public static void mappingFee(List<MajorForCollection> majorList, Map<String, String>feeMap){
		if(majorList!=null){
			for(MajorForCollection major:majorList){
				for(Entry<String, String>entry:feeMap.entrySet()){
					if(major.getTitle().toLowerCase().trim().contains(entry.getKey().toLowerCase().trim())){
						major.setTuitionFee(entry.getValue().trim());
						break;
					}
				}
			}
		}
	}
	public static Map<String, String>getFeeMap(String fileName){
		Map<String, String>map = new HashMap<String, String>();
		try (FileInputStream fis= new FileInputStream(fileName);
				HSSFWorkbook book = new HSSFWorkbook(fis);){
			HSSFSheet sheet = book.getSheetAt(1);
			HSSFRow row = null;
			for(int i = 0; i<=sheet.getLastRowNum(); i++){
				row = sheet.getRow(i);
				map.put(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
		
	}
}
