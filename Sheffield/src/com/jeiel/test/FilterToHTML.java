package com.jeiel.test;

public class FilterToHTML {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
    //��excel����֮������structure�еĽϳ�Ԫ��
	public static String filter(String a)
	{
		String result=a;
		result=result.replace(">", "&gt;");
		result=result.replace("<", "&lt;");
		result=result.replace("\r\n", "<br>");
		result=result.replace("\n", "<br>");
		return result;
		
	}
}
