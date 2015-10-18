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
	private static String postUrl = "http://www.myoffer.cn/external/api/courses";
	private static int index=1;
	private static String SCHOOL_NAME = "Sheffield";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		for(;index<=152;){//indexΪ��ҳ����ʾ��id��
			
			delete(postUrl,index);
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
	    connection.setRequestProperty("Cookie", "__utma=255880599.950065990.1440817756.1440817756.1440908413.2; __utmz=255880599.1440817756.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); connect.sid=s%3AOcedKsPgerNTlD5s6Oj7CrnTSwc3-J59.ckOjvW%2Buc053uYxZY34Z5WJaagGE%2FiwYM0FVz5sSiGY; CNZZDATA1256122972=1457945077-1443749818-%7C1445134038");
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
		    connection.disconnect();
	    }catch(Exception e){
	    	System.out.println("Terminated at "+index);
	    	System.out.println("Restart at "+index);
	    }
		
	}
	
}
