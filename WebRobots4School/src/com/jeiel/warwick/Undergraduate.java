package com.jeiel.warwick;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.functions.Match;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.entity.Major;
import com.jeiel.entity.MajorForCollection;


public class Undergraduate {
	public static final int SCHOOL=0;
	public static final int LEVEL=1;
	public static final int TITLE=2;
	public static final int TYPE=3;
	public static final int APPLICATION_FEE=4;
	public static final int TUITION_FEE=5;
	public static final int ACADEMIC_ENTRY_REQUIREMENT=6;
	public static final int IELTS_AVERAGE_REQUIREMENT=7;
	public static final int IELTS_LOWEST_REQUIREMENT=8;
	public static final int STRUCTURE=9;
	public static final int LENGTH_MONTHS=10;
	public static final int MONTH_OF_ENTRY=11;
	public static final int SCHOLARSHIP=12;
	public static final int URL=13;
	
	public static final String SCHOOL_NAME="Warwick";
	public static final int MAX_THREAD_AMOUNT = 60;
	
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	public static Map<String, String>courseLevelMap=CourseLevelReader.getCourseLevelMap();
	
	
	public static void main(String[] args) {
		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		try {
			initExcelWriter();
			initMajorList("http://www2.warwick.ac.uk/study/undergraduate/courses/");
			//initMajorListWithData();
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
			pool.awaitTermination(10, TimeUnit.MINUTES);
			System.out.println("finish");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				exportExcel("gen_data_"+SCHOOL_NAME+"_ug.xls");
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
		try {
			System.out.println("preparing majorList");
			Connection conn=Jsoup.connect(originalUrl);
			Document doc=conn.timeout(60000).get();
			
			Elements es=doc.getElementsByClass("sb-glossary-terms").get(0).getElementsByTag("p");
			String baseUrl="http://www2.warwick.ac.uk/study/undergraduate/courses/";
			for(Element e:es){
				MajorForCollection major = new MajorForCollection();
				major.setTitle(removeCourseCode(e.text(), major));
				getIELTS(null, major);
				major.setTuitionFee(getFee(major.getTuitionFee()));
				major.setLevel("Undergraduate");
				if(major.getTitle().equals("Spanish, see Hispanic Studies")){
					major.setUrl(baseUrl + "r400/");
				}else{
					major.setUrl(e.getElementsByTag("a").get(0).attr("href").contains("http")?
							e.getElementsByTag("a").get(0).attr("href"):
							baseUrl+e.getElementsByTag("a").get(0).attr("href").replace("/", ""));
				}
				major.setMonthOfEntry("9");
				majorList.add(major);
			}
			
			System.out.println("majorList prepared");
			System.out.println("majorList size: "+majorList.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	

	public static void initMajorListWithData(){
		
		System.out.println("preparing majorList");
		for(String[] singleData:Data.UNDERGRADUATE_DATA){
			MajorForCollection major = new MajorForCollection();
			major.setSchool(singleData[0].split("\\|")[0]);
			major.setLevel("Undergraduate");
			major.setTitle(singleData[0].split("\\|")[1]);
			major.setType(singleData[0].split("\\|")[2]);
			major.setTuitionFee(singleData[0].split("\\|")[3]);
			if(singleData[0].split("\\|").length==5){
				major.setLength(""+Integer.parseInt(singleData[0].split("\\|")[4].replace("years", "").trim())*12);
				
			}
			major.setUrl(singleData[1]);
			majorList.add(major);
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
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}
	}
	
	public static void mark(MajorForCollection major, boolean handled){//鏍囪宸插畬鎴愮殑major
		major.setHandled(handled);
		if(!handled){
			major.setDistributed(false);
		}
	}
	
	public static void getDetails(MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e = doc.getElementById("course-tab-1");
		if(e!=null){
			if(e.getElementsByTag("a").size()>0){
				for(Element tmp:e.getElementsByTag("a")){
					if(!tmp.text().contains("FAQs")){
						major.setSchool(tmp.text());
						break;
					}
				}
			}
		}
		/*e=doc.getElementById("course-tab-2");
		if(e!=null){
			boolean find=false;
			for(Element tmp:e.children()){
				if(tmp.tagName().contains("h")){
					find=false;
					if(tmp.text().contains("What will I learn")){
						find=true;
					}
				}else{
					if(find&&(tmp.text().toLowerCase().contains("year")||tmp.text().toLowerCase().contains("month"))){
						major.setLength(major.getLength()+";"+tmp.text());
					}
				}
			}
			
		}*/
		e=doc.getElementById("course-tab-3");
		if(e!=null){
			boolean find=false;
			for(Element tmp:e.children()){
				if(tmp.tagName().contains("h")){
					find=false;
					if(tmp.text().contains("Entry Requirements")){
						find=true;
					}
				}else{
					if(find){
						major.setAcademicRequirements(major.getAcademicRequirements()+";"+tmp.text());
					}
				}
			}
			if(major.getAcademicRequirements().startsWith(";")){
				major.setAcademicRequirements(major.getAcademicRequirements().substring(1));
			}
		}
		e=doc.getElementById("course-tab-4");
		if(e!=null){
			if(e.text().contains("Find our more about the")){
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml().substring(0, e.outerHtml().indexOf("Find our more about the")))));
			}else{
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())));
			}
		}
		
		if(doc.getElementsByAttributeValue("title", "Unistats KIS Widget").size()>0){
			e=doc.getElementsByAttributeValue("title", "Unistats KIS Widget").get(0);
			String url = "http:"+e.attr("src").replace("http:", "");
			if(!url.contains("youtube")){
				boolean gotFrame=false;
				while(!gotFrame){
					try{
						conn = Jsoup.connect(url);
						doc=conn.timeout(5000).get();
						if(doc.getElementsByTag("h1").size()>0){
							e=doc.getElementsByTag("h1").get(0);
							if(e.text().indexOf("(")>0){
								major.setType(e.text().substring(0, e.text().indexOf("(")).trim());
								major.setTitle(e.text().substring(e.text().indexOf(")")+1).trim());
							}else{
								major.setType(e.text());
							}
							
						}
						if(doc.getElementsByClass("widgetCourse").size()>0){
							e = doc.getElementsByClass("widgetCourse").get(0);
							for(Element tmp:e.children()){
								if(tmp.tagName().equals("p")&&(tmp.text().toLowerCase().contains("year")||tmp.text().toLowerCase().contains("month"))){
									major.setLength(tmp.text());
								}
							}
						}
						gotFrame=true;
					}catch(Exception e1){
						e1.printStackTrace();
					}
				}
			}else{
				major.setType(url);
			}
		}
		
		//getScholarship(major);
		
		mark(major, true);
	}

	public static void initExcelWriter()
			throws Exception {
		

        book = new HSSFWorkbook(); 

         
		if(sheet==null){
			sheet = book.createSheet("Sheet1");
		}
		row = sheet.createRow((short) 0);

        row.createCell(0).setCellValue("School");
        row.createCell(1).setCellValue("Level");
        row.createCell(2).setCellValue("Title");
        row.createCell(3).setCellValue("Type");
        row.createCell(4).setCellValue("Application Fee");
        row.createCell(5).setCellValue("Tuition Fee");
        row.createCell(6).setCellValue("Academic Entry Requirement");
        row.createCell(7).setCellValue("IELTS Average Requirement");
        row.createCell(8).setCellValue("IELTS Lowest Requirement");
        row.createCell(9).setCellValue("Structure");
        row.createCell(10).setCellValue("Length (months)");
        row.createCell(11).setCellValue("Month of Entry");
        row.createCell(12).setCellValue("Scholarship");
        row.createCell(13).setCellValue("Url"); 
	}

	public static void addToSheet(int rowNum, int col, String content)
			throws Exception {
		if(row==null||row.getRowNum()!=rowNum){
			row = sheet.createRow((short)rowNum);
		}
		
		row.createCell(col).setCellValue(content);
	}

	public static void exportExcel(String fileName) throws Exception {
		for(int row =0;row<majorList.size();row++){
			addToSheet(row+1, SCHOOL, majorList.get(row).getSchool());
			addToSheet(row+1, LEVEL, majorList.get(row).getLevel());
			addToSheet(row+1, TITLE, majorList.get(row).getTitle());
			addToSheet(row+1, TYPE, majorList.get(row).getType());
			addToSheet(row+1, APPLICATION_FEE, majorList.get(row).getApplicationFee());
			addToSheet(row+1, TUITION_FEE, majorList.get(row).getTuitionFee());
			addToSheet(row+1, ACADEMIC_ENTRY_REQUIREMENT, majorList.get(row).getAcademicRequirements());
			addToSheet(row+1, IELTS_AVERAGE_REQUIREMENT, majorList.get(row).getIELTS_Avg());
			addToSheet(row+1, IELTS_LOWEST_REQUIREMENT, majorList.get(row).getIELTS_Low());
			addToSheet(row+1, STRUCTURE, majorList.get(row).getStructure());
			addToSheet(row+1, LENGTH_MONTHS, majorList.get(row).getLength());
			addToSheet(row+1, MONTH_OF_ENTRY, majorList.get(row).getMonthOfEntry());
			addToSheet(row+1, SCHOLARSHIP, majorList.get(row).getScholarship());
			addToSheet(row+1, URL, majorList.get(row).getUrl());
		}
		
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos=new FileOutputStream(file);
		book.write(fos);
		fos.close();
	}

	public static String getLastYear(Element e){
		if(e.outerHtml().toLowerCase().contains("fifth year")||e.outerHtml().toLowerCase().contains("year 5")){
			return "60";
		}else if(e.outerHtml().toLowerCase().contains("fourth year")||e.outerHtml().toLowerCase().contains("year 4")){
			return "48";
		}else if(e.outerHtml().toLowerCase().contains("third year")||e.outerHtml().toLowerCase().contains("year 3")){
			return "36";
		}else if(e.outerHtml().toLowerCase().contains("second year")||e.outerHtml().toLowerCase().contains("year 2")){
			return "24";
		}
		return "";
	}
	
	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}

	public static String replaceSpecialCharacter(String content){
		String result="";
		if(content!=null){
			result=content.replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"");
		}
		return result;
	}
	
	public static void getIELTS(String content,MajorForCollection major){
		if(major.getIELTS_Avg().length()>0&&major.getIELTS_Avg().equals("Band A")){
			major.setIELTS_Avg("6.5");
			major.setIELTS_Low("6.0");
		}else if(major.getIELTS_Avg().length()>0&&major.getIELTS_Avg().equals("Band B")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getIELTS_Avg().length()>0&&major.getIELTS_Avg().equals("Band C")){
			major.setIELTS_Avg("7.0");
			major.setIELTS_Low("6.5");
		}
	}

	public static void getScholarship(MajorForCollection major){
		major.setScholarship("Merit Scholarship$15000;"+
		"Alumni/Existing Students/Family Discount$11400;"+
		"British Council GREAT Scholarship$18000");
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
		if(content.equals("Band A")){
			return "16620";
		}else if(content.equals("Band B")){
			return "21200";
		}else if(content.equals("Band C")){
			return "21200";
		}
		return "";
		/*String fee="";
		Pattern p = Pattern.compile("£[0-9]+");
    	Matcher m = p.matcher(content.replace(",", "").replace(" ", ""));
    	List<Integer> moneyList=new ArrayList<Integer>();
    	while (m.find()) 
    	{
    		moneyList.add(Integer.parseInt(m.group().replace("£", "")));
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
		return fee;*/
	}

	public static String getLength(String content){
		String length = "";
		content=content.toLowerCase().replace(" ", "").replace(",", "")
				.replace("one", "1").replace("two", "2").replace("three", "3")
				.replace("four", "4").replace("five", "5").replace("six", "6");
		Pattern pY = Pattern.compile("[1-9](-[1-9]){0,1}year");
		Pattern pM = Pattern.compile("[1-9][0-9](-[1-9][0-9]){0,1}month");
		Pattern pW = Pattern.compile("[1-9][0-9]*(-[1-9][0-9]*){0,1}week");
		Matcher m = pW.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("week", "");
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}
			length = "" + Integer.parseInt(tmp)/4;
			return length;
		}
		m = pM.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("month", "");
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}
			length = "" + Integer.parseInt(tmp);
			return length;
		}
		m = pY.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("year", "");
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}
			length = "" + Integer.parseInt(tmp)*12;
			return length;
		}
		return length;
	}
	
	public static String getMonthOfEntry(String content){
		String month=content;
		if(month.contains("NOV")){
			month="11";
		}else if(month.contains("DEC")){
			month="12";
		}
		return month;
	}
	
	public static String removeCourseCode(String content, MajorForCollection major){
		Pattern p = Pattern.compile("\\((?![0-9]+\\))(?![a-zA-Z]+\\))[a-zA-Z0-9]+\\)");
		Matcher m = p.matcher(content);
		if(m.find()){
			String code=m.group().replace("(", "").replace(")", "").trim();
			if(courseLevelMap.containsKey(code)){
				major.setIELTS_Avg(courseLevelMap.get(code));
				major.setTuitionFee(courseLevelMap.get(code));
			}else{
				major.setIELTS_Avg(code);
			}
		}
		return content.replaceAll("\\((?![0-9]+\\))(?![a-zA-Z]+\\))[a-zA-Z0-9]+\\)", "");
	}

}


