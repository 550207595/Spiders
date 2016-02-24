package com.jeiel.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jeiel.entity.Major;

import net.sf.json.JSONObject;

public class Add {
	private static String postUrl = "http://www.myoffer.cn/external/api/courses";
	public static String SCHOOL_NAME = "";//read from modified excel
	public static String LEVEL="";//ug,pgt
	public static String filepath="gen_data_"+SCHOOL_NAME+"_"+LEVEL+"_modified.xls";
	private static int index=1;//web id start from 1
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){
		// TODO Auto-generated method stub
		try {
			System.out.println("work start");
			List<Major> list=POIReadAndPost.getData();
			for(;index<=list.size();){
				/*if(index<=248){
					index++;
					continue;
				}*/
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
	    connection.setRequestProperty("Cookie", "tencentSig=4079742976; CNZZDATA1256122972=343858778-1451182154-%7C1453273881; connect.sid=s%3A0xYUYnCtq0m-Nfs7_qzmZQ7ayyUTKvux.97%2Frydtt%2Bh90G%2Fq%2FS%2FA9LvQNuU6Dw3ir%2FEnus%2Bbdg6g; Hm_lvt_7b2d81bba29516af3254cc73cbff78b1=1456193359; Hm_lpvt_7b2d81bba29516af3254cc73cbff78b1=1456216302; Hm_lvt_f2d08716d77a6692d1510d26ea9b72d1=1456193360; Hm_lpvt_f2d08716d77a6692d1510d26ea9b72d1=1456216302; __utmt_UA-72589077-1=1; __utma=255880599.2123860011.1456193455.1456193455.1456216350.2; __utmb=255880599.1.10.1456216350; __utmc=255880599; __utmz=255880599.1456193455.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); fromBD=false");
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
		    
		    //get Return Msg
		    
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			System.out.println("Terminated at "+index);
	    	System.out.println("Restart at "+index);
		}
		
	}

}
