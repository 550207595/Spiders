package com.jeiel.utils;

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
	private static String SCHOOL_NAME = "";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		for(;index<=362;){//indexΪ��ҳ����ʾ��id��
			
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
	    connection.setRequestProperty("Cookie", "tencentSig=4079742976; CNZZDATA1256122972=343858778-1451182154-%7C1453273881; connect.sid=s%3A0xYUYnCtq0m-Nfs7_qzmZQ7ayyUTKvux.97%2Frydtt%2Bh90G%2Fq%2FS%2FA9LvQNuU6Dw3ir%2FEnus%2Bbdg6g; Hm_lvt_7b2d81bba29516af3254cc73cbff78b1=1456193359; Hm_lpvt_7b2d81bba29516af3254cc73cbff78b1=1456216302; Hm_lvt_f2d08716d77a6692d1510d26ea9b72d1=1456193360; Hm_lpvt_f2d08716d77a6692d1510d26ea9b72d1=1456216302; __utmt_UA-72589077-1=1; __utma=255880599.2123860011.1456193455.1456193455.1456216350.2; __utmb=255880599.1.10.1456216350; __utmc=255880599; __utmz=255880599.1456193455.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); fromBD=false");
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
