package com.jeiel.heriotwatt;

public class Data {
	public static String[][] getData(String level){
		if(level.equals("Undergraduate")){
			return UNDERGRADUATE_DATA;
		}else if(level.equals("Postgraduate")){
			return POSTGRADUATE_DATA;
		}else{
			return new String[][]{};
		}
	}
	
	private static String[][] UNDERGRADUATE_DATA=new String[][]{
		
	};

	private static String[][] POSTGRADUATE_DATA=new String[][]{
		
	};
	
}


