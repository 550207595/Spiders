package com.jeiel.utils;

import java.io.File;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebUtils {

	public static final int METHOD_POST = 0;
	public static final int METHOD_GET = 1;
	private static int TIMEOUT = 30*1000;
	
	public static Document getDocument(String url, int method, int timeout){
		Document doc = null;
		if(url!=null && url.length()>0){
			while(true){
				try{
					Connection conn = Jsoup.connect(url).timeout(timeout>0?timeout:TIMEOUT);
					switch(method){
						case METHOD_POST:
							doc = conn.post();
							break;
						case METHOD_GET:
						default:
							doc = conn.get();
					}
					return doc;
				}catch(Exception e){
					if(e.getMessage().contains("timed out")){
						System.out.println(e.getMessage());
					}else{
						e.printStackTrace();
					}
				}
			}
			
		}
		return doc;
	}
	public static Document getDocumentFromFile(String fileName){
		Document doc = null;
		File file = new File(fileName);
		if(file.exists()){
			try{
				doc = Jsoup.parse(file, "utf-8");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return doc;
	}
}
