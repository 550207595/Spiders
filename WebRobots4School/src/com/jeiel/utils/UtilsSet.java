package com.jeiel.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
