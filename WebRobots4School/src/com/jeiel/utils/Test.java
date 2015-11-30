package com.jeiel.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;

public class Test {
	public static final String SCHOOL_NAME;
	public static final String LEVEL;
	
	static{
		String className = new Object(){
			public String getClassName(){
				String className = this.getClass().getName();
				className = className.replaceAll("\\$[\\s\\S]*", "");
				return className;
			}
		}.getClassName();
		LEVEL = className.substring(className.lastIndexOf(".")+1);
		String tmpStr = className.substring("com.jeiel.".length(),className.lastIndexOf("."));
		SCHOOL_NAME = tmpStr.substring(0, 1).toUpperCase() + tmpStr.substring(1);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*File file = new File("Nottingham_structure/1.txt");
		try(FileInputStream fis = new FileInputStream(file)){
			StringBuilder sb = new StringBuilder();
			byte[] bytes = new byte[10240];
			int len = 0;
			while((len=fis.read(bytes))>0){
				sb.append(new String(bytes, 0, len));
			}
			System.out.println(sb);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		System.out.println(SCHOOL_NAME);
		System.out.println(LEVEL);
	}

}
