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
	private static String SCHOOL_NAME = "Sheffield";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		for(;index<=70;){//index为网页上显示的id号
			
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
	    connection.setRequestProperty("Cookie", "connect.sid=s%3AZYPBPmZHwWQHWYkUkUrh7WzRsk2yB9fN.24BEpcJ34iSjpykHSA2eBhAiP%2F4RGKYwm%2BhZaxpOx8I; CNZZDATA1256122972=254321286-1441087693-%7C1441846761");
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
			
		    //固定值
		    JSONObject entry=new JSONObject();
		    entry.put("target", "course");
		    entry.put("action", "remove");
		    
		    //自定义值
		   	JSONObject value=new JSONObject();
		    value.put("university", SCHOOL_NAME);
		    value.put("id", id);
		   	entry.put("value", value);
		    
		    out.writeBytes(entry.toString());
		    out.flush();
		    
		    //读取响应

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
