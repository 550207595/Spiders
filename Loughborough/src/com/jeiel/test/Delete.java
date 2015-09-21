package com.jeiel.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

public class Delete {
	private static String postUrl = "http://myoffer.cn/external/api/courses";
	private static int index=1;
	private static String SCHOOL_NAME = "Loughborough";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		for(;index<=127;){//indexΪ��ҳ����ʾ��id��
			
			delete(postUrl,index);
		}
	   
	}
	
	public static HttpURLConnection getConnection(String postUrl) throws IOException{
		URL url = new URL(postUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setDoOutput(true);
	    connection.setDoInput(true);
	    connection.setRequestMethod("POST");
	    connection.setUseCaches(false);
	    connection.setInstanceFollowRedirects(true);
	    connection.setRequestProperty("Accept", "application/json, text/plain, */*");
	    connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
	    connection.setRequestProperty("Referer", "http://myoffer.cn/external/course");
	    connection.setRequestProperty("Cookie", "__utma=255880599.950065990.1440817756.1440817756.1440908413.2; __utmz=255880599.1440817756.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); connect.sid=s%3AMv5yQi0MGfjM1haAjhACovD4iI0HgNm9.06d44LJ0zsyACmZbVfI5lA3M61A8vnur5MDgT1WxhRQ; CNZZDATA1256122972=2001768791-1440195715-%7C1442804107");
	    connection.setRequestProperty("Connection", "keep-alive");
	    connection.setRequestProperty("Pragma", "no-cache");
	    connection.setRequestProperty("Cache-Control", "no-cache");
	    connection.connect();
	    return connection;
	}
	
	public static void delete(String postUrl,int id) {
	    try{
	    	System.out.println("Delete "+index);
	    	HttpURLConnection connection=getConnection(postUrl);
			DataOutputStream out= new DataOutputStream(connection.getOutputStream());
			
		    //�̶�ֵ
		    JSONObject entry=new JSONObject();
		    entry.put("target", "course");
		    entry.put("action", "remove");
		    
		    //�Զ���ֵ
		   	JSONObject value=new JSONObject();
		    value.put("university", SCHOOL_NAME);
		    value.put("id", id);
		   	entry.put("value", value);
		    
		    out.writeBytes(entry.toString());
		    out.flush();
		    
		    //��ȡ��Ӧ

		    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    String lines;
		    StringBuffer sb = new StringBuffer("");
		    while ((lines = reader.readLine()) != null) {
		    	lines = new String(lines.getBytes(), "utf-8");
		    	sb.append(lines);
		    }
		    
		    System.out.println(sb);

		    out.close();
		    connection.disconnect();
		    reader.close();
		    System.out.println("Deleted");
		    index++;
	    }catch(Exception e){
	    	System.out.println("Terminated at "+index);
	    	System.out.println("Restart at "+index);
	    }
		
	}
	
}
