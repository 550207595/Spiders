package com.jeiel.Derby;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.entity.MajorForCollection;
import com.jeiel.utils.ExcelGenerator;
import com.jeiel.utils.UtilsSet;
import com.jeiel.utils.WebUtils;


public class Postgraduate {
	public static final int MAX_THREAD_AMOUNT = 60;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	
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
		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		try {
			//initMajorList("http://www.derby.ac.uk/courses/index.php?searchtype=alpha&level%5B%5D=Postgraduate&letter=");
			initMajorListWithData();
			System.out.println("start");
			ExecutorService pool=Executors.newCachedThreadPool();
			for(int i=0;i<MAX_THREAD_AMOUNT;i++){
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						while(hasNextUnhandledMajor()){
							get(nextUnhandledMajor());
						}
					}
				};
				pool.execute(r);
				
			}
			pool.shutdown();
			pool.awaitTermination(5, TimeUnit.MINUTES);
			System.out.println("finish");
			UtilsSet.sortMajorList(majorList);
			//UtilsSet.mappingFee(majorList, UtilsSet.getFeeMap("gen_data_Queenmargaret_ug_modified.xls"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ExcelGenerator excelGenerator = new ExcelGenerator(SCHOOL_NAME, LEVEL, majorList);
				excelGenerator.exportExcel();
				long endTimeInMillis=Calendar.getInstance().getTimeInMillis();
				System.out.println("Total seconds: " + (endTimeInMillis-startTimeInMillis)/1000 + "s");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static synchronized MajorForCollection nextUnhandledMajor(){
		for(MajorForCollection major:majorList){
			if(!major.isDistributed()&&!major.isHandled()){
				major.setDistributed(true);
				return major;
			}
		}
		return null;
	}
	
	public static synchronized boolean hasNextUnhandledMajor(){
		for(MajorForCollection major:majorList){
			if(!major.isHandled()){
				return true;
			}
		}
		return false;
	}

	public static void initMajorList(String originalUrl){
		
		System.out.println("preparing majorList");
		
		majorList.clear();
		Document doc;
		Elements pages = new Elements();
		Elements es;
		for(int i=0;i<26;i++){
			char alphabet=(char)(65+i);
			System.out.println(alphabet);
			doc=WebUtils.getDocument(originalUrl+alphabet, WebUtils.METHOD_GET, 10000);
			pages.addAll(doc.select("ul.search-results-pagination > li > a:matchesOwn(^[\\s]*[\\d]*[\\s]*$)"));
			
		}
		for(Element page:pages){
			doc = WebUtils.getDocument(page.attr("abs:href"), WebUtils.METHOD_GET, 5*1000);
			es = doc.select("table.course-search-results > tbody > tr");
			for(Element e:es){
				MajorForCollection major = new MajorForCollection();
				major.setLevel(LEVEL);
				major.setTitle(e.select("td[data-value=Course]").get(0).text());
				major.setSchool(e.select("td[data-value=Subject]").get(0).text());
				major.setUrl(e.select("td[data-value=Course] > a").get(0).attr("abs:href"));
				majorList.add(major);
			}
			
		}
		
		System.out.println("majorList prepared");
		System.out.println("majorList size: "+majorList.size());
	}
	

	public static void initMajorListWithData(){
		
		System.out.println("preparing majorList");
		File file = new File("gen_data_"+SCHOOL_NAME+"_"+LEVEL.replace("Undergraduate", "ug").replace("Postgraduate", "pgt")+".xls");
		
		try(FileInputStream fis = new FileInputStream(file);
				HSSFWorkbook book = new HSSFWorkbook(fis)){
			HSSFSheet sheet = book.getSheetAt(0);
			HSSFRow row = null;
			for(int i = 1; i<=sheet.getLastRowNum(); i++){
				row = sheet.getRow(i);
				if(row.getCell(1).getStringCellValue().equals(LEVEL)){
					MajorForCollection major = new MajorForCollection();
					major.setSchool(row.getCell(0).getStringCellValue());
					major.setLevel(row.getCell(1).getStringCellValue());
					major.setTitle(row.getCell(2).getStringCellValue());
					major.setType(row.getCell(3).getStringCellValue());
					major.setApplicationFee(row.getCell(4).getStringCellValue());
					major.setTuitionFee(row.getCell(5).getStringCellValue());
					major.setAcademicRequirements(row.getCell(6).getStringCellValue());
					major.setIELTS_Avg(row.getCell(7).getStringCellValue());
					major.setIELTS_Low(row.getCell(8).getStringCellValue());
					major.setStructure(row.getCell(9).getStringCellValue());
					major.setLength(row.getCell(10).getStringCellValue());
					major.setMonthOfEntry(row.getCell(11).getStringCellValue());
					major.setScholarship(row.getCell(12).getStringCellValue());
					major.setUrl(row.getCell(13).getStringCellValue());
					majorList.add(major);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("majorList prepared");
		System.out.println("majorList size: "+majorList.size());
	}

	public static void get(MajorForCollection major){
		if(major==null)return;
		try{
			System.out.println((majorList.indexOf(major)+1)+"\t"+major.getUrl());
			getDetails(major);
		}catch(Exception e){
			mark(major, false);
			System.out.println("Failed at "+(majorList.indexOf(major)+1)+"\t"+major.getUrl());
			/*if(e.getMessage().contains("Read timed out")){
				System.out.println(e.getMessage());
			}else if(e.getMessage().contains("connect timed out")){
				System.out.println(e.getMessage());
			}else if(e.getMessage().contains("Too many redirects")){
				major.setApplicationFee(e.getMessage());
				mark(major, true);
				System.out.println(e.getMessage());
			}else if(e.getMessage().contains("HTTP error fetching URL")){
				major.setApplicationFee(e.getMessage());
				mark(major, true);
				System.out.println(e.getMessage());
			}else{
				e.printStackTrace();
			}*/
			if(e.getMessage()!=null&&e.getMessage().contains("timed out")){
				System.out.println(e.getMessage());
			}else{
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized void mark(MajorForCollection major, boolean handled){//鏍囪宸插畬鎴愮殑major
		major.setHandled(handled);
		if(!handled){
			major.setDistributed(false);
		}
	}
	
	public static void getDetails(MajorForCollection major) throws Exception {
		Document doc=WebUtils.getDocument(major.getUrl(), WebUtils.METHOD_GET, 30000);
		Element e = null;
		
		if(doc.select("li.accordion-item > p:has(strong:matchesOwn(^[\\s]*Course length:[\\s]*$))").size()>0){
			e = doc.select("li.accordion-item > p:has(strong:matchesOwn(^[\\s]*Course length:[\\s]*$))").get(0);
			major.setLength(getLength(e.text()));
		}
		
		if(doc.select("li.accordion-title:has(a:matchesOwn(^[\\s]*Entry requirements[\\s]*$))").size()>0){
			e = doc.select("li.accordion-title:has(a:matchesOwn(^[\\s]*Entry requirements[\\s]*$))").get(0);
			major.setAcademicRequirements(e.text().trim());
			getIELTS(e.text(), major);
		}

//		if(doc.select("div.course-award").size()>0){
//			e = doc.select("div.course-award").get(0);
//			major.setType(e.text());
//		}
		
		if(doc.select("li.accordion-item > p:has(strong:matchesOwn(^[\\s]*Start date:[\\s]*$))").size()>0){
			e = doc.select("li.accordion-item > p:has(strong:matchesOwn(^[\\s]*Start date:[\\s]*$))").get(0);
			major.setMonthOfEntry(getMonthOfEntry(e.text()));
		}
		
		if(doc.select("li.accordion-item > p:has(strong:matchesOwn(^[\\s]*College:[\\s]*$))").size()>0){
			e = doc.select("li.accordion-item > p:has(strong:matchesOwn(^[\\s]*College:[\\s]*$))").get(0);
			major.setSchool(e.ownText());
		}
		
		if(major.getSchool().isEmpty()){
			if(doc.select("h4:matchesOwn(^[\\s]*Related Links[\\s]*$) + ul > li >a").size()>0){
				e = doc.select("h4:matchesOwn(^[\\s]*Related Links[\\s]*$) + ul > li > a").get(0);
				major.setSchool(e.text());
			}
		}
		
		if(doc.select("li.accordion-title:has(a:matchesOwn(^[\\s]*What you will cover[\\s]*$))").size()>0){
			e = doc.select("li.accordion-title:has(a:matchesOwn(^[\\s]*What you will cover[\\s]*$))").get(0);
			major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
		}
		
		if(doc.select("#fees-finance > tbody").size()>0){
			e = doc.select("#fees-finance > tbody").get(0);
			major.setTuitionFee(getFee(e.text()));
		}
		
		mark(major, true);
	}


	public static String getLastYear(String content){
		content = content.toLowerCase()
				.replace("one", "1")
				.replace("two", "2")
				.replace("three", "3")
				.replace("four", "4")
				.replace("five", "5")
				.replace("six", "6");
		if(content.contains("sixth year")||content.contains("year 6")||content.contains("semester 12")){
			return "72";
		}else if(content.contains("semester 11")){
			return "66";
		}else if(content.contains("fifth year")||content.contains("year 5")||content.contains("semester 10")){
			return "60";
		}else if(content.contains("semester 9")){
			return "54";
		}else if(content.contains("fourth year")||content.contains("year 4")||content.contains("semester 8")){
			return "48";
		}else if(content.contains("semester 7")){
			return "42";
		}else if(content.contains("third year")||content.contains("year 3")||content.contains("semester 6")){
			return "36";
		}else if(content.contains("semester 5")){
			return "30";
		}else if(content.contains("second year")||content.contains("year 2")||content.contains("semester 4")){
			return "24";
		}else if(content.contains("semester 3")){
			return "18";
		}else if(content.contains("first year")||content.contains("year 1")||content.contains("semester 2")){
			return "12";
		}else if(content.contains("semester 1")){
			return "6";
		}
		return "";
	}
	
	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}

	public static String replaceSpecialCharacter(String content){
		String result="";
		if(content!=null){
			result=content.replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").replace("&pound;", "£");
		}
		return result;
	}
	
	public static void getIELTS(String content,MajorForCollection major){
		
		if(content.contains("IELTS")){
			if(content.contains("8.5")){
				major.setIELTS_Avg("8.5");
				if(content.contains("8.0")){
					major.setIELTS_Low("8.0");
				}else if(content.contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(content.contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(content.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(content.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(content.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("8.0")){
				major.setIELTS_Avg("8.0");
				if(content.contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(content.contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(content.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(content.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(content.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("7.5")){
				major.setIELTS_Avg("7.5");
				if(content.contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(content.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(content.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(content.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("7.0")){
				major.setIELTS_Avg("7.0");
				if(content.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(content.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(content.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("6.5")){
				major.setIELTS_Avg("6.5");
				if(content.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(content.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("6.0")){
				major.setIELTS_Avg("6.0");
				if(content.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("5.5")){
				major.setIELTS_Avg("5.5");
				if(content.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(content.contains("5.0")){
				major.setIELTS_Avg("5.0");
				major.setIELTS_Low("5.0");
			}
		}
	}

	public static void getScholarship(MajorForCollection major){
		major.setScholarship("Heriot-Watt Foundation Bursary$2250;"+
		"Heriot-Watt University Bursary$3000;"+
		"Heriot-Watt Academic Scholarship$1000");
		/*if(major.getSchool().contains("Automatic Control and Systems Engineering")){
		major.setScholarship(major.getScholarship()+";"+"Academic Achievement Scholarship$3000");
		}
		if(major.getSchool().contains("Bioengineering")){
		major.setScholarship(major.getScholarship()+";"+"Academic Achievement Scholarship$2000");
		}
		if(major.getSchool().contains("Chemical and Biological Engineering")){
		major.setScholarship(major.getScholarship()+";"+"Academic Achievement Scholarship$2000");
		}
		if(major.getSchool().contains("Computer Science")){
		major.setScholarship(major.getScholarship()+";"+"Academic Achievement Scholarship$2000");
		}
		if(major.getSchool().contains("Electronic and Electrical Engineering")){
		major.setScholarship(major.getScholarship()+";"+"Academic Achievement Scholarship$2000");
		}
		if(major.getSchool().contains("Materials Science and Engineering")){
		major.setScholarship(major.getScholarship()+";"+"Academic Achievement Scholarship$3000");
		}*/
	}

	public static String toUpperCase(String content){
		StringBuilder strBuilder=new StringBuilder();
		for(String word:content.split(" ")){
			if(word.equals("and")){
				strBuilder.append(word + " ");
			}else{
				if(word.charAt(0)>=65&&word.charAt(0)<=90){
					strBuilder.append(word + " ");
				}else{
					strBuilder.append((char)(word.charAt(0) - 32) + word.substring(1) + " ");
				}
			}
		}
		return strBuilder.toString().trim();
	}
	
	public static String getFee(String content){
		String fee="";
		Pattern p = Pattern.compile("[£$][0-9]+");
    	Matcher m = p.matcher(content.replace(",", "").replace(" ", ""));
    	List<Integer> moneyList=new ArrayList<Integer>();
    	while (m.find()) 
    	{
    		moneyList.add(Integer.parseInt(m.group().replace("£", "").replace("$", "")));
    	}
    	int max = 0;
    	for(int tmpFee : moneyList)
		{
	    	if(tmpFee > max)
    		{
    	    	max = tmpFee;
    	    	fee = max + "";
    	    }
	    }
		return fee;
	}

	public static String getLength(String content){
		String length = "";
		content=content.toLowerCase().replaceAll("\\s", "")
				.replace("one", "1").replace("two", "2").replace("three", "3")
				.replace("four", "4").replace("five", "5").replace("six", "6");
		Pattern pY = Pattern.compile("[1-9]((-|/){1}[1-9]){0,1}year");
		Pattern pM = Pattern.compile("[1-9][0-9]*((-|/){1}[1-9][0-9]*){0,1}month");
		Pattern pW = Pattern.compile("[1-9][0-9]*((-|/){1}[1-9][0-9]*){0,1}week");
		Matcher m = pW.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("week", "");
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}else if(tmp.contains("/")){
				tmp=tmp.substring(0, tmp.indexOf("/"));
			}
			length = "" + Integer.parseInt(tmp)/4;
			return length;
		}
		m = pM.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("month", "");
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}else if(tmp.contains("/")){
				tmp=tmp.substring(0, tmp.indexOf("/"));
			}
			length = "" + Integer.parseInt(tmp);
			return length;
		}
		m = pY.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("year", "");
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}else if(tmp.contains("/")){
				tmp=tmp.substring(0, tmp.indexOf("/"));
			}
			length = "" + Integer.parseInt(tmp)*12;
			return length;
		}
		return length;
	}
	
	public static String getMonthOfEntry(String content){
		String month=content;
		if(content.toLowerCase().contains("august")){
			month = "8";
		}else if(content.toLowerCase().contains("september")||content.toLowerCase().contains("septembe")){
			month = "9";
		}else if(content.toLowerCase().contains("october")){
			month = "10";
		}else if(content.toLowerCase().contains("november")){
			month = "11";
		}else if(content.toLowerCase().contains("december")){
			month = "12";
		}else if(content.toLowerCase().contains("january")){
			month = "1";
		}else if(content.toLowerCase().contains("february")){
			month = "2";
		}
		return month;
	}

	
	public static String requestFee(String url){
		String fee = "";
		Document doc = WebUtils.getDocument(url, WebUtils.METHOD_GET, 10000);
		if(doc.select("#block-system-main > table > tbody > tr:nth-child(2) > td:nth-child(4)").size()>0){
			fee = getFee(doc.select("#block-system-main > table > tbody > tr:nth-child(2) > td:nth-child(4)").text());
		}else{
			fee = url;
		}
		return fee;
	}
	
	public static String getType(Document doc){
		String type="";
		if(doc.select("#unistats-widget-frame").size()>0){//iframe id
			Element e = doc.select("#unistats-widget-frame").get(0);
			StringBuilder typeURL = new StringBuilder();
			/*typeURL.append("http://widget.unistats.ac.uk/Widget/");
			typeURL.append(e.attr("data-institution")+"/");
			typeURL.append(e.attr("data-course")+"/");
			typeURL.append(e.attr("data-orientation")+"/");
			typeURL.append("null/");
			typeURL.append(e.attr("data-language")+"/");
			typeURL.append(e.attr("data-kismode"));*/
			typeURL.append(e.attr("src"));
			
			Document tmpDoc = WebUtils.getDocument(typeURL.toString(), WebUtils.METHOD_GET, 10000);
			if(tmpDoc.select("#kisWidget > div.widgetCourse > h1").size()>0){
				e = tmpDoc.select("#kisWidget > div.widgetCourse > h1").get(0);
				type = e.text().trim().indexOf(" ")>0?
						e.text().trim().substring(0,e.text().trim().indexOf(" ")):e.text().trim();
			}
		}
		return type;
	}
	
}


