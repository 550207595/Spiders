package com.jeiel.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.jeiel.entity.Major;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONStringer;

public class Add {
	private static String postUrl = "http://www.myoffer.cn/external/api/courses";
	public static String SCHOOL_NAME = "";
	public static boolean STRUCTURE_OVERFLOW = false;
	private static int index=1;//��Ӧҳ��id
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){
		// TODO Auto-generated method stub
		try {
			System.out.println("work start");
			List<Major> list=POIReadAndPost.getData(STRUCTURE_OVERFLOW);
			for(;index<=list.size();){
				if(index<=211){
					index++;
					continue;
				}
				add(postUrl,list.get(index-1));
			}
			System.out.println("work done");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	   
	}
	
	
	public static HttpURLConnection getConnection(String postUrl) throws Exception {
		URL url = new URL(postUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setDoOutput(true);
	    connection.setDoInput(true);
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Accept", "application/json, text/plain, */*");
	    connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
	    connection.setRequestProperty("Cookie", "connect.sid=s%3AwPq_spZ1efyHGfhmD6P9gHcD925HEdSe.YLiDOc%2Br8ECkDGKqbTJXwlW8IqytwvRNQhoAeeyn2%2FE; CNZZDATA1256122972=254321286-1441087693-%7C1448504041");
	    connection.connect();
	    return connection;
	}
	
	public static void add(String postUrl,Major major){
	    
		
		try {
			System.out.println("Add "+index);
			HttpURLConnection connection = getConnection(postUrl);
			OutputStream os= connection.getOutputStream();
			
		    //�̶�ֵ
		    JSONObject entry=new JSONObject();
		    entry.put("target", "course");
		    entry.put("action", "add");

		    JSONObject course=new JSONObject();
		    course.put("school", major.getSchool());
		    course.put("level", major.getLevel());
		    course.put("title", major.getTitle());
		    course.put("type", major.getType());
		    course.put("application", major.getApplicationFee().trim().replace(",", ""));
		    course.put("tuition", major.getTuitionFee().trim().replace(",", ""));
		    course.put("academic", major.getAcademicRequirements());
		    course.put("ielts_avg", major.getIELTS_Avg().trim());
		    course.put("ielts_low", major.getIELTS_Low().trim());
		    
		    LinkedHashMap<String, String> structureMap=major.getStructure();
		    JSONObject structureItem;
		    List<JSONObject> structureList=new ArrayList<JSONObject>();
		    for(Map.Entry<String, String>e:structureMap.entrySet()){
		    	structureItem=new JSONObject();
	    		structureItem.put("category", e.getKey());
		    	structureItem.put("summary", e.getValue());
		    	structureList.add(structureItem);

		    }
		    
		    if(structureList.size()>0)
		    	course.put("structure", structureList);
		    
		    course.put("length", major.getLength());
		    course.put("month", major.getMonthOfEntry());

		    LinkedHashMap<String, String> scholarshipMap=major.getScholarship();
		    JSONObject scholarshipItem;
		    List<JSONObject> scholarshipList=new ArrayList<JSONObject>();
		   	for(Map.Entry<String, String>e:scholarshipMap.entrySet()){
		   		scholarshipItem=new JSONObject();
		   		scholarshipItem.put("name", e.getKey());
		   		scholarshipItem.put("value", e.getValue());
		   		scholarshipList.add(scholarshipItem);
		   	}
		   	if(scholarshipList.size()>0)
		   		course.put("scholarship", scholarshipList);
		   	
		   	JSONObject value=new JSONObject();
		    value.put("university", SCHOOL_NAME);
		    value.put("course", course);
		   	entry.put("value", value);
		   	System.out.println(entry);
		    os.write(entry.toString().getBytes("utf8"));
		    os.flush();
		    
		    //��ȡ��Ӧ
		    
		    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    String lines;
		    StringBuffer sb = new StringBuffer("");
		    while ((lines = reader.readLine()) != null) {
		    	lines = new String(lines.getBytes(), "utf-8");
		    	sb.append(lines);
		    }
		    
		    System.out.println(sb);
		    System.out.println("get return");
		    
		    os.close();
		    connection.disconnect();
		    reader.close();
		    System.out.println("Added");
		    index++;
		    connection.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			System.out.println("Terminated at "+index);
	    	System.out.println("Restart at "+index);
		}
		
	}

}
