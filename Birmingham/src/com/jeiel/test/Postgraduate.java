package com.jeiel.test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.test.MajorForCollection;


public class Postgraduate {
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
	public static final int MAX_THREAD_AMOUNT = 50;
	
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	
	
	public static void main(String[] args) {
		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		try {
			initExcelWriter();
			//initMajorList("http://www.bangor.ac.uk/courses/postgraduate/");
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
				//t.start();
				
			}
			pool.shutdown();//不再接收新提交的任务，但是仍在队列中的任务会被继续处理完
			pool.awaitTermination(10, TimeUnit.MINUTES);
			System.out.println("finish");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				
				exportExcel("gen_data_"+SCHOOL_NAME+"_pgt.xls");
				long endTimeInMillis=Calendar.getInstance().getTimeInMillis();
				System.out.println("Total seconds: " + (endTimeInMillis-startTimeInMillis)/1000 + "s");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static synchronized MajorForCollection nextUnhandledMajor(){//获取还未处理且还未分发的专业
		for(MajorForCollection major:majorList){
			if(!major.isDistributed()&&!major.isHandled()){
				major.setDistributed(true);
				return major;
			}
		}
		return null;
	}
	
	public static synchronized boolean hasNextUnhandledMajor(){//判断是否有还未处理的专业
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
			
			Elements es=doc.getElementById("schools").getElementsByTag("a");
			for(Element e:es){//school
				System.out.println(e.text());
				majorList.addAll(getMajors(e.attr("href")));
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
			Element e=doc.getElementById("contents");
			boolean findBegin=false;
			String school=e.getElementsByTag("h1").get(0).text();
			school=school.substring(school.indexOf(":")+1).trim();
			System.out.println(school);
			for(Element tmp:e.children()){
				//System.out.println(tmp.text());
				if(findBegin){
					if(tmp.text().contains("Research Programmes")){
						//System.out.println("break");
						break;
					}
					if(tmp.attr("id").equals("rightcol")){
						//System.out.println("continue");
						continue;
					}
					for(Element li:tmp.getElementsByTag("li")){
						//System.out.println(li.text());
						MajorForCollection major = new MajorForCollection();
						major.setSchool(school);
						major.setTitle(li.text());
						//System.out.println("title:"+"\t"+major.getTitle());
						major.setType(tmp.getElementsByTag("h3").get(0).text());
						major.setTitle(major.getTitle().replace(major.getType(), "").trim());
						//System.out.println("type:"+"\t"+major.getType());
						major.setUrl(baseUrl + li.getElementsByTag("a").get(0).attr("href"));
						//System.out.println("url:"+"\t"+major.getUrl());
						majorList.add(major);
					}
				}else{
					if(tmp.text().contains("Taught Postgraduate Courses")){
						findBegin=true;
						//System.out.println("find begin");
					}
				}
			}
			/*Elements innerES=doc.getElementById("contents").getElementsByTag("li");
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
			}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	public static void initMajorListWithData(){
		
		System.out.println("preparing majorList");
		for(String[] singleData:Data.POSTGRADUATE_DATA){
			MajorForCollection major = new MajorForCollection();
			major.setSchool(singleData[0].split("\\|")[0]);
			major.setLevel("Postgraduate");
			major.setTitle(singleData[0].split("\\|")[1]);
			major.setType(singleData[0].split("\\|")[2]);
			major.setTuitionFee(singleData[0].split("\\|")[3]);
			/*if(singleData[0].split("\\|").length==5){
				major.setLength(singleData[0].split("\\|")[4]);
			}*/
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
	
	public static synchronized void mark(MajorForCollection major, boolean handled){//标记已完成的major
		major.setHandled(handled);
		if(!handled){
			major.setDistributed(false);
		}
	}
	
	public static void getDetails(MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;
		major.setMonthOfEntry("9");//apply标签中有
		e=doc.getElementById("content");
		if(e!=null){
			String content=html2Str(e.outerHtml());
			content=replaceSpecialCharacter(content);
			if(content.indexOf("Course content")>0){
				content=content.substring(content.indexOf("Course content"));
				if(content.indexOf("Modules for the current academic year")>0){
					content=content.substring(0, content.indexOf("Modules for the current academic year"));
				}
			}
			major.setStructure(content);
		}else{
			e=doc.getElementById("overview");
			if(e!=null){
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml().replace(doc.getElementById("sidepanel").outerHtml(), ""))).trim());
			}
		}
		
		e=doc.getElementById("sidepanel");
		if(e!=null){
			if(e.getElementsByTag("ul")!=null&&e.getElementsByTag("ul").size()>0){
				e=e.getElementsByTag("ul").get(0);
				String content=e.text();
				content=replaceSpecialCharacter(content);
				if(content.indexOf("Duration")>0){
					content=content.substring(content.indexOf("Duration"));
					if(content.indexOf("year")>0){
						content=content.substring(0, content.indexOf("year")+4);
						if(content.contains("1")){
							content="12";
						}else if(content.contains("2")){
							content="24";
						}else if(content.contains("3")){
							content="36";
						}else if(content.contains("4")){
							content="48";
						}else if(content.contains("5")){
							content="60";
						}else if(content.contains("6")){
							content="72";
						}else if(content.contains("One")){
							content="12";
						}
					}
					major.setLength(content);
					
				}
			}
		}
		
		e=doc.getElementById("requirements");
		if(e!=null){
			String content=e.text();
			content=replaceSpecialCharacter(content);
			if(content.indexOf("Entry requirements")>0){
				content=content.substring(content.indexOf("Entry requirements"));
				if(content.indexOf("International Students")>0){
					content=content.substring(0, content.indexOf("International Students"));
				}else if(content.indexOf("Ask the IEC for assistance")>0){
					content=content.substring(0, content.indexOf("Ask the IEC for assistance"));
				}
			}
			
			major.setAcademicRequirements(content);
			getIELTS(content, major);
		}
		getScholarship(major);
		
		mark(major, true);
	}

	public static void initExcelWriter()throws Exception {
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
			if(major.getTitle().contains("Filmmaking: Concept to Screen")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
		}else if(major.getSchool().contains("English Literature")){
			major.setIELTS_Avg("6.5");
			major.setIELTS_Low("6.0");
		}else if(major.getSchool().contains("Lifelong Learning")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("History, Welsh History and Archaeology")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Linguistics and English Language")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Modern Languages")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Translation Studies")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
		}else if(major.getSchool().contains("Music")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Philosophy and Religion")){
			major.setIELTS_Avg("7.0");
			major.setIELTS_Low("6.5");
		}else if(major.getSchool().contains("Welsh")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Bangor Business School")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Education")){
			major.setIELTS_Avg("6.0");//根据该学院大多数专业得出
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Law")){
			major.setIELTS_Avg("6.5");
			major.setIELTS_Low("6.0");
		}else if(major.getSchool().contains("Social Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Criminology and Law")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
		}else if(major.getSchool().contains("Environment, Natural Resources and Geography")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Distance Learning")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
		}else if(major.getSchool().contains("Biological Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Ocean Sciences")){
			major.setIELTS_Avg("6.0");//根据该学院大多数专业得出
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Marine Biology")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getTitle().contains("Marine Environmental Protection")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getTitle().contains("Applied Marine Geoscience")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getTitle().contains("Physical Oceanography")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
		}else if(major.getSchool().contains("Healthcare Sciences")){
			major.setIELTS_Avg("6.0");//根据该学院大多数专业得出
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Public Health and Health Promotion")){
				major.setIELTS_Avg("6.0");
				major.setIELTS_Low("5.5");
			}else if(major.getTitle().contains("Nursing")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getTitle().contains("Midwifery")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getTitle().contains("Non-clinical")){
				major.setIELTS_Avg("7.0");
				major.setIELTS_Low("6.5");
			}
		}else if(major.getSchool().contains("Medical Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Psychology")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Psychological Research")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getTitle().contains("Neuroimaging")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}else if(major.getType().contains("MRes")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
		}else if(major.getSchool().contains("Sport, Health and Exercise Sciences")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Chemistry")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getType().contains("MRes")){
				major.setIELTS_Avg("7.0");
				major.setIELTS_Low("6.5");
			}
		}else if(major.getSchool().contains("Computer Science")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
		}else if(major.getSchool().contains("Electronic Engineering")){
			major.setIELTS_Avg("6.0");
			major.setIELTS_Low("5.5");
			if(major.getTitle().contains("Nanotechnology and Microfabrication")){
				major.setIELTS_Avg("6.5");
				major.setIELTS_Low("6.0");
			}
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
		major.setScholarship("Merit Scholarship$tuitionfee;"+
		"British Council GREAT Scholarship$8000;"+
		"Santander$5000;"+
		"Commonwealth Scholarship$12000;"+
		"Commonwealth Shared Scholarship$tuitionfee");
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

}


