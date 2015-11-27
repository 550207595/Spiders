package com.jeiel.ucl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	
	public static final String SCHOOL_NAME="UCL";
	public static final int MAX_THREAD_AMOUNT = 60;
	
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	
	
	public static void main(String[] args) {
		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		try {
			initExcelWriter();
			initMajorList("http://www.ucl.ac.uk/prospective-students/undergraduate/degrees");
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
			
			Elements es=doc.select("table.rt.cf.degree-list__table > tbody > tr");
			String baseUrl="http://www.ucl.ac.uk";
			for(Element e:es){//major
				MajorForCollection major = new MajorForCollection();
				major.setTitle(e.select("a").get(0).ownText());
				major.setLevel("Undergraduate");
				major.setType(e.select("td").get(1).text());
				major.setApplicationFee(e.select("td").get(2).text());
				major.setUrl("http:"+e.select("a").get(0).attr("href"));
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
	
	public static synchronized void mark(MajorForCollection major, boolean handled){//鏍囪宸插畬鎴愮殑major
		major.setHandled(handled);
		if(!handled){
			major.setDistributed(false);
		}
	}
	
	public static void getDetails(MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e = null;
		
		if(doc.select("#index > div.site-content.wrapper > div.hero > div > div > ul.pills.list-inline > li:nth-child(2) > a").size()>0){
			e=doc.select("#index > div.site-content.wrapper > div.hero > div > div > ul.pills.list-inline > li:nth-child(2) > a").get(0);
			major.setSchool(e.text());
		}

		if(doc.select("div.hero__body.hero__body--background > ul > li").size()>0){
			e=doc.select("div.hero__body.hero__body--background > ul > li").get(0);
			major.setMonthOfEntry(e.ownText().contains("September")?"9":e.ownText());
		}
		
		if(doc.select("#key-information > div > div.col--1-2-lg-content > dl").size()>0){
			e=doc.select("#key-information > div > div.col--1-2-lg-content > dl").get(0);
			major.setLength(getLength(e.text()));
		}
		
		if(doc.select("#entry-requirements").size()>0){
			e=doc.select("#entry-requirements").get(0);
			major.setAcademicRequirements(e.text().indexOf("UK applicants qualifications")>0?
					e.text().substring(0, e.text().indexOf("UK applicants qualifications")):e.text());
			if(e.text().indexOf("English language level")>0){
				if(e.text().indexOf("Standard", e.text().indexOf("English language requirements"))>0){
					major.setIELTS_Avg("6.5");
					major.setIELTS_Low("6.0");
				}else if(e.text().indexOf("Good", e.text().indexOf("English language requirements"))>0){
					major.setIELTS_Avg("7.0");
					major.setIELTS_Low("6.0");
				}else if(e.text().indexOf("Advanced", e.text().indexOf("English language requirements"))>0){
					major.setIELTS_Avg("7.5");
					major.setIELTS_Low("6.5");
				}
			}
		}
		
		if(doc.select("div.tabs__content.island").size()>0){
			e=doc.select("div.tabs__content.island").get(0);
			major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
		}
		
		if(doc.select("#fees-and-funding").size()>0){
			e=doc.select("#fees-and-funding").get(0);
			major.setTuitionFee(getFee(e.text().substring(e.text().indexOf("Overseas fee"))));
		}
		
		if(doc.select("#fees-and-funding > div > div").size()>0){
			e=doc.select("#fees-and-funding > div > div").get(0);
			for(Element h:e.select("h4")){
				if(getFee(e.select("dl").get(e.select("h4").indexOf(h)).text()).length()==0)continue;
				major.setScholarship(major.getScholarship()+";"+
						h.text()+"$"+getFee(e.select("dl").get(e.select("h4").indexOf(h)).text()));
			}
			if(major.getScholarship().length()>0){
				major.setScholarship(major.getScholarship().substring(1));
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
		/*if(major.getSchool().contains("Creative Studies and Media")){
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
			major.setIELTS_Avg("6.0");//鏍规嵁璇ュ闄㈠ぇ澶氭暟涓撲笟寰�?��
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Education")){
			major.setIELTS_Avg("6.0");//鏍规嵁璇ュ闄㈠ぇ澶氭暟涓撲笟寰�?��
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
			major.setIELTS_Avg("7.0");//鏍规嵁璇ュ闄㈠ぇ澶氭暟涓撲笟寰�?��
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
		}*/
		
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
		return fee;
	}

	public static String getLength(String content){
		String length = "";
		content=content.toLowerCase().replace(" ", "").replace(",", "")
				.replace("one", "1").replace("two", "2").replace("three", "3")
				.replace("four", "4").replace("five", "5").replace("six", "6");
		Pattern pY = Pattern.compile("[1-9](-[1-9]){0,1}year");
		Pattern pM = Pattern.compile("[1-9][0-9]*(-[1-9][0-9]*){0,1}month");
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

}


