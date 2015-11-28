package com.jeiel.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File file = new File("Nottingham_structure/1.txt");
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
		}
	}

}
