package com.jeiel.test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.test.MajorForCollection;


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
	
	public static final String SCHOOL_NAME="Birmingham";
	public static final int MAX_THREAD_AMOUNT = 60;
	
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	
	
	public static void main(String[] args) {
		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		try {
			initExcelWriter();
			initMajorList("http://www.birmingham.ac.uk/undergraduate/courses/search.aspx?CurrentTab=AtoZ&AtoZFilter=Undergraduate");
			/*initMajorListWithData();
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
			pool.shutdown();//涓嶅啀鎺ユ敹鏂版彁浜ょ殑浠诲姟锛屼絾鏄粛鍦ㄩ槦鍒椾腑鐨勪换鍔′細琚户缁鐞嗗畬
			pool.awaitTermination(10, TimeUnit.MINUTES);
			System.out.println("finish");*/
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
	
	public static synchronized MajorForCollection nextUnhandledMajor(){//鑾峰彇杩樻湭澶勭悊涓旇繕鏈垎鍙戠殑涓撲笟
		for(MajorForCollection major:majorList){
			if(!major.isDistributed()&&!major.isHandled()){
				major.setDistributed(true);
				return major;
			}
		}
		return null;
	}
	
	public static synchronized boolean hasNextUnhandledMajor(){//鍒ゆ柇鏄惁鏈夎繕鏈鐞嗙殑涓撲笟
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
			/*String letter="&CourseComplete_AtoZ_AtoZLetter=";
			String page="&CourseComplete_atozlisting_goto=";
			Connection conn=Jsoup.connect(originalUrl+letter+"A"+page+"1");
			Document doc=conn.timeout(60000).get();
			
			Element e=doc.getElementsByAttributeValue("class", "list--unset alphabet").get(0);
			Map<String, Integer> alphabet=new LinkedHashMap<String, Integer>();
			int sum=0;
			for(Element li:e.getElementsByTag("li")){
				if(li.getElementsByTag("a").size()>0){
					Element a = li.getElementsByTag("a").get(0);
					alphabet.put(a.text(), Integer.parseInt(""+a.attr("title").subSequence(0, a.attr("title").indexOf(" "))));
					sum+=Integer.parseInt(""+a.attr("title").subSequence(0, a.attr("title").indexOf(" ")));
				}
			}
			System.out.println(sum);
			String baseUrl = "http://www.birmingham.ac.uk";
			for(Entry<String, Integer> entry:alphabet.entrySet()){
				int pageCount=entry.getValue()/20 + (entry.getValue()%20>0 ? 1 : 0);
				for(int i=1;i<=pageCount;i++){
					conn=Jsoup.connect(originalUrl+letter+entry.getKey()+page+i);
					doc=conn.timeout(10000).get();
					e=doc.getElementsByClass("tablesaw").get(0).getElementsByTag("tbody").get(0);
					for(Element tr:e.getElementsByTag("tr")){
						Elements tds=tr.getElementsByTag("td");
						MajorForCollection major = new MajorForCollection();
						if(tds.size()>=1){
							//major.setTitle(tds.get(0).text());
							//major.setType(GetType(tds.get(0).text()));
							major.setUrl(baseUrl+tds.get(0).getElementsByTag("a").get(0).attr("href"));
						}
						if(tds.size()>=4){
							major.setLength(tds.get(3).text());
							if(major.getLength().contains("year")){
								major.setLength(""+12*Integer.parseInt(major.getLength().substring(major.getLength().indexOf("year")-2, major.getLength().indexOf("year")-1)));
							}
						}
						majorList.add(major);
					}
				}
			}*/
			Connection conn=Jsoup.connect(originalUrl);
			Document doc=conn.timeout(60000).get();
			
			Elements es=doc.getElementsByClass("accordion__body");

			String baseUrl = "http://www.birmingham.ac.uk";
			String queryUrl="http://www.birmingham.ac.uk/undergraduate/courses/search.aspx?CourseKeywords=";
			for(Element div:es){
				Elements uls=div.getElementsByTag("ul");
				Elements h3s=div.getElementsByTag("h3");
				if(uls.size()!=h3s.size()){
					break;
				}
				for(Element ul:uls){
					for(Element li:ul.getElementsByTag("li")){
						MajorForCollection major =new MajorForCollection();
						major.setSchool(h3s.get(uls.indexOf(ul)).text());
						major.setTitle(li.text());
						major.setUrl(baseUrl+li.getElementsByTag("a").attr("href"));
						conn=Jsoup.connect(queryUrl+major.getSchool().replace("&", "%26").replace(" ", "+"));
						doc=conn.timeout(10000).get();
						if(doc.getElementsByTag("tbody").size()>0){
							Element tmp=doc.getElementsByTag("tbody").get(0);
							if(tmp.getElementsByTag("tr").size()>0){
								tmp=tmp.getElementsByTag("tr").get(0);
								major.setLevel(tmp.getElementsByTag("td").get(1).text());
								major.setLength(tmp.getElementsByTag("td").get(3).text());
							}
						}
						majorList.add(major);
					}
				}
			}
			
			
			System.out.println("majorList prepared");
			System.out.println("majorList size: "+majorList.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public static List<MajorForCollection> getMajors(String schoolUrl){
		List<MajorForCollection> list = new ArrayList<MajorForCollection>();
		try {
			String baseUrl="http://www.bangor.ac.uk";
			Connection conn=Jsoup.connect(baseUrl + schoolUrl);
			Document doc = conn.timeout(60000).get();
			Elements innerES=doc.getElementById("contents").getElementsByTag("li");
			String school = doc.getElementById("contents").getElementsByTag("h1").get(0).text();
			school = school.substring(school.indexOf(":") + 1).trim();
			for(Element li:innerES){
				MajorForCollection major = new MajorForCollection();
				major.setSchool(school);
				major.setTitle(li.getElementsByTag("a").get(0).text());
				major.setLevel("Undergraduate");
				if(li.getElementsByTag("span").size()>0){
					major.setType(li.getElementsByTag("span").get(0).ownText());
					if(li.getElementsByTag("em").size()>0){
						major.setLength(li.getElementsByTag("em").get(0).text().replace("(", "").replace(")", ""));
					}
				}
				major.setLevel(major.getSchool()+"|"+major.getTitle()+"|"+major.getType()+"|"+major.getLength());
				major.setUrl(baseUrl + li.getElementsByTag("a").get(0).attr("href"));
				list.add(major);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	public static void initMajorListWithData(){
		
		System.out.println("preparing majorList");
		for(String[] singleData:Data.UNDERGRADUATE_DATA){
			MajorForCollection major = new MajorForCollection();
			major.setLevel("Undergraduate");
			major.setLength(singleData[0]);
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
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	public static synchronized void mark(MajorForCollection major, boolean handled){//鏍囪宸插畬鎴愮殑major
		major.setHandled(handled);
		if(!handled){
			major.setDistributed(false);
		}
	}
	
	public static void getDetails(MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;
		
		if(doc.getElementsByClass("grid").size()>0){
			e=doc.getElementsByClass("grid").get(0);
			Elements dts=e.getElementsByTag("dt");
			Elements dds=e.getElementsByTag("dd");
			if(dts.size()!=dds.size()){
				major.setApplicationFee("size not match: dts "+dts.size()+"; dds "+dds.size());
				mark(major, true);
				return;
			}
			for(Element dt:dts){
				if(dt.text().contains("Duration")&&major.getLength().length()==0){
					major.setLength(dds.get(dts.indexOf(dt)).text());
				}else if(dt.text().contains("Fees")){
					if(dds.get(dts.indexOf(dt)).text().contains("£")){
						String str=dds.get(dts.indexOf(dt)).text();
						str=str.substring(str.lastIndexOf("£")).replace("Funding opportunities are available", "");
						major.setTuitionFee(str);
					}
				}else if(dt.text().contains("Start date")){
					major.setMonthOfEntry(dds.get(dts.indexOf(dt)).text());
					if(major.getMonthOfEntry().contains("September")){
						major.setMonthOfEntry("9");
					}else if(major.getMonthOfEntry().contains("October")){
						major.setMonthOfEntry("10");
					}
				}else if(dt.text().contains("Study options")&&major.getLength().length()==0){
					major.setLength("options "+dds.get(dts.indexOf(dt)).text());
				}else if(dt.text().contains("School")){
					major.setSchool(dds.get(dts.indexOf(dt)).text());
				}else if(dt.text().contains("School")||dt.text().contains("Department")){
					major.setSchool(dds.get(dts.indexOf(dt)).text());
				}
			}
		}
		//major.setMonthOfEntry("9");
		/*e=doc.getElementById("tab-c2");
		if(e!=null&&e.text().contains("Course Content")){
			String content=html2Str(e.outerHtml());
			if(content.indexOf("What will you study on this cours")>0){
				content=content.substring(content.indexOf("What will you study on this cours"));
				if(content.indexOf("Modules for the current academic year")>0){
					content=content.substring(0, content.indexOf("Modules for the current academic year"));
				}else if(content.indexOf("Programme Specification")>0){
					content=content.substring(0, content.indexOf("Programme Specification"));
				}
			}
			major.setStructure(replaceSpecialCharacter(content));
		}*/
		
		/*e=doc.getElementById("tab-c4");
		if(e!=null&&e.text().contains("Entry requirements")){
			String content=e.text();
			content=content.substring(content.indexOf("Entry requirements"));
			if(content.indexOf("General University entry requirements")>0){
				content=content.substring(0, content.indexOf("General University entry requirements"));
			}else if(content.indexOf("More information")>0){
				content=content.substring(0, content.indexOf("More information"));
			}
			major.setAcademicRequirements(replaceSpecialCharacter(content));
			getIELTS(content, major);
		}*/
		
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
		if(major.getSchool().contains("Creative Studies and Media")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("English Literature")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Lifelong Learning")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("History, Welsh History and Archaeology")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Linguistics and English Language")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("English Language Studies")){
				major.setIELTS_Avg("5.0");
				major.setIELTS_Low("5.0");
			}
		}else if(major.getSchool().contains("Modern Languages")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Music")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Philosophy and Religion")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Welsh")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Bangor Business School")){
			major.setIELTS_Avg("6.0");//鏍规嵁璇ュ闄㈠ぇ澶氭暟涓撲笟寰楀嚭
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Education")){
			major.setIELTS_Avg("6.0");//鏍规嵁璇ュ闄㈠ぇ澶氭暟涓撲笟寰楀嚭
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Law")){
			major.setIELTS_Avg("6.5");
			major.setIELTS_Low("6.0");
			if(major.getTitle().contains("Law with Professional English")){
				major.setIELTS_Avg("6.0");
				major.setIELTS_Low("5.5");
			}
		}else if(major.getSchool().contains("Social Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Environment, Natural Resources and Geography")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Biological Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Ocean Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Healthcare Sciences")){
			major.setIELTS_Avg("7.0");//鏍规嵁璇ュ闄㈠ぇ澶氭暟涓撲笟寰楀嚭
			major.setIELTS_Low("6.5");
			if(major.getTitle().contains("Nursing")){
				major.setIELTS_Avg("7.0");
				major.setIELTS_Low("7.0");
			}else if(major.getTitle().contains("Midwifery")){
				major.setIELTS_Avg("7.0");
				major.setIELTS_Low("7.0");
			}else if(major.getTitle().contains("Radiography")){
				major.setIELTS_Avg("7.0");
				major.setIELTS_Low("6.5");
			}else if(major.getTitle().contains("Healthcare Sciences")){
				major.setIELTS_Avg("7.0");
				major.setIELTS_Low("6.5");
			}
		}else if(major.getSchool().contains("Medical Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Psychology")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Sport, Health and Exercise Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Chemistry")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("6.0");
		}else if(major.getSchool().contains("Computer Science")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Electronic Engineering")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}
		
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

	public static String GetType(String input)//BA BEng Bsc Msc MEng 
	{
		String types="BA;BEng;BSc;BDS;BN;BVSc;MOSci;MESci;MEcol;MPhys;MMath;MMarBiol;MBChB;MChem;MSc;MEng;Double MA;Joint MA;MA;MArich;MBA;PG;Pg;EdD;MEd;Postgraduate Diploma;Postgraduate Certificate;Doctorate;Graduate Certificate;LLM;LLB;GradDip;MTh;MRes";
		
		String[] array=types.split(";");
		for(int i=0;i<array.length;i++)
		{
			if(input.contains(array[i]))
			{
				return array[i];
			}
		}
		//String result=array[array.length-1].replace(")", "");
		return "";
	}
}


