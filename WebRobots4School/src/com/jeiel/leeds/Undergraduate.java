package com.jeiel.leeds;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.entity.MajorForCollection;
import com.jeiel.utils.ExcelGenerator;


public class Undergraduate {
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
			initMajorList("http://courses.leeds.ac.uk/a-z?type=UG");
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
		
		boolean finish = false;
		do{
			try {
				majorList.clear();
				Connection conn=Jsoup.connect(originalUrl);
				Document doc=conn.timeout(10000).get();
				Elements es=doc.select("table.tablesaw > tbody > tr");
				for(Element e:es){//major
					MajorForCollection major = new MajorForCollection();
					major.setLevel(LEVEL);
					major.setTitle(e.select("a").get(0).text().trim());
					major.setType(e.select("td").get(1).text().trim());
					major.setLength(getLength(e.select("td").get(2).text()).trim());
					major.setUrl(e.select("a").get(0).attr("href"));
					if(major.getUrl().equals("http://www.see.leeds.ac.uk/fn8c")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/ba-environment-and-business/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f851")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/bsc-environmental-science/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f856")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/menv-bsc-environmental-science-international-year-abroad/");
					}else if(major.getUrl().equals("http://www.geog.leeds.ac.uk/f800")){
						major.setUrl("http://www.geog.leeds.ac.uk/study/undergraduate/courses/bsc/");
					}else if(major.getUrl().equals("http://www.geog.leeds.ac.uk/l700")){
						major.setUrl("http://www.geog.leeds.ac.uk/study/undergraduate/courses/ba/");
					}else if(major.getUrl().equals("http://www.geog.leeds.ac.uk/l7n9")){
						major.setUrl("http://www.geog.leeds.ac.uk/study/undergraduate/courses/geotrans/");
					}else if(major.getUrl().equals("http://www.geog.leeds.ac.uk/ff68")){
						major.setUrl("http://www.geog.leeds.ac.uk/study/undergraduate/courses/geoggeol/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f600/")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/bsc-geological-sciences/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f601")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/mgeol-bsc-geological-sciences-international-year-abroad/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f640")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-deg");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f641")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/mgeophys-bsc-geophysical-sciences-international-year-abroad/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f790")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/bsc-meteorology-and-climate-science/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f791")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/menv-bsc-meteorology-and-climate-science-international-year-abroad/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f7m0")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/bsc-sustainability-and-environmental-management/");
					}else if(major.getUrl().equals("http://www.see.leeds.ac.uk/f750")){
						major.setUrl("http://www.see.leeds.ac.uk/admissions-and-study/undergraduate-degrees/courses/menv-bsc-sustainability-and-environmental-management-international-year-abroad/");
					}
					
					majorList.add(major);
				}
				finish=true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while(!finish);
		
		System.out.println("majorList prepared");
		System.out.println("majorList size: "+majorList.size());
	}
	

	public static void initMajorListWithData(){
		
		System.out.println("preparing majorList");
		for(String[] singleData:Data.getData(LEVEL)){
			MajorForCollection major = new MajorForCollection();
			major.setLevel(LEVEL);
			major.setApplicationFee(singleData[0]);
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
			if(e.getMessage().contains("Read timed out")){
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
			}
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
		Document doc=conn.timeout(10000).followRedirects(true).get();
		Element e = null;
		
		if(major.getUrl().startsWith("http://courses.leeds.ac.uk/")){//http://courses.leeds.ac.uk/
			if(doc.select("#container > div > nav.breadcrumb > ul > li:nth-child(1) > a").size()>0){
				e=doc.select("#container > div > nav.breadcrumb > ul > li:nth-child(1) > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#tab2 > dl > dd:nth-child(2)").size()>0){
				e=doc.select("#tab2 > dl > dd:nth-child(2)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#tab3 > dl > dd:nth-child(2)").size()>0){
				e=doc.select("#tab3 > dl > dd:nth-child(2)").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://business.leeds.ac.uk/")){//http://business.leeds.ac.uk/
			if(doc.select("#wrapper > div > ul > li:nth-child(1) > a").size()>0){
				e=doc.select("#wrapper > div > ul > li:nth-child(1) > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#wrapper > section > div > aside > div.key-item > dl > dd:nth-child(10)").size()>0){
				e=doc.select("#wrapper > section > div > aside > div.key-item > dl > dd:nth-child(10)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#wrapper > section > div > article > dl > dd:nth-child(4)").size()>0){
				e=doc.select("#wrapper > section > div > article > dl > dd:nth-child(4)").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
			if(doc.select("#wrapper > section > div > aside > div.key-item > dl > dd:nth-child(4)").size()>0){
				e=doc.select("#wrapper > section > div > aside > div.key-item > dl > dd:nth-child(4)").get(0);
				major.setMonthOfEntry(getMonthOfEntry(e.text()));
			}
			if(doc.select("#wrapper > section > div > aside > div.key-item > dl > dd:nth-child(14) > a").size()>0){
				e=doc.select("#wrapper > section > div > aside > div.key-item > dl > dd:nth-child(14) > a").get(0);
				major.setTuitionFee(getFee(e.text()));
			}
		}else if(major.getUrl().startsWith("http://www.maths.leeds.ac.uk/")){//http://www.maths.leeds.ac.uk/
			if(doc.select("#bread > li:nth-child(1) > a").size()>0){
				e=doc.select("#bread > li:nth-child(1) > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#mytabs-4 > div").size()>0){
				e=doc.select("#mytabs-4 > div").get(0);
				major.setAcademicRequirements(e.text().trim().indexOf("Alternative qualifications")>0?
						e.text().trim().substring(0, e.text().trim().indexOf("Alternative qualifications")):e.text().trim());
			}
			if(doc.select("#mytabs-3 > div").size()>0){
				e=doc.select("#mytabs-3 > div").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://www.engineering.leeds.ac.uk/")){//http://www.engineering.leeds.ac.uk/
			if(doc.select("#breadcrumb > li:nth-child(2) > a:nth-child(1),#breadcrumb > li:nth-child(1) > a").size()>0){
				e=doc.select("#breadcrumb > li:nth-child(2) > a:nth-child(1),#breadcrumb > li:nth-child(1) > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#tab705-4 > div").size()>0){
				e=doc.select("#tab705-4 > div").get(0);
				major.setAcademicRequirements(e.text().trim().indexOf("Access to Leeds")>0?
						e.text().trim().substring(0, e.text().trim().indexOf("Access to Leeds")):e.text().trim());
			}
			if(doc.select("#tab705-2 > div").size()>0){
				e=doc.select("#tab705-2 > div").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
			if(doc.select("#tab705-1 > div").size()>0){
				e=doc.select("#tab705-1 > div").get(0);
				major.setMonthOfEntry(getMonthOfEntry(e.text().substring(e.text().indexOf("Start date:")>0?e.text().indexOf("Start date:"):0)));
				major.setTuitionFee(getFee(e.text().substring(e.text().indexOf("Course fees (per year):")>0?e.text().indexOf("Course fees (per year):"):0)));
			}
		}else if(major.getUrl().startsWith("http://www.llc.leeds.ac.uk/")){//http://www.llc.leeds.ac.uk/
			if(doc.select("#container > div.content > h1 > a").size()>0){
				e=doc.select("#container > div.content > h1 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("ul.tabbed-nav > li:nth-child(4) >a").size()>0){
				e=doc.select("ul.tabbed-nav > li:nth-child(4) >a").get(0);
				boolean finish = false;
				do{
					try{
						Connection tmpConn = Jsoup.connect(e.attr("href"));
						Document tmpDoc = tmpConn.timeout(10000).get();
						if(tmpDoc.select("div.entry-content").size()>0){
							e=tmpDoc.select("div.entry-content").get(0);
							major.setAcademicRequirements(e.text().trim());
						}
						finish = true;
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}while(!finish);
				
			}
			if(doc.select("ul.tabbed-nav > li:nth-child(2) >a").size()>0){
				e=doc.select("ul.tabbed-nav > li:nth-child(2) >a").get(0);
				boolean finish = false;
				do{
					try{
						Connection tmpConn = Jsoup.connect(e.attr("href"));
						Document tmpDoc = tmpConn.timeout(10000).get();
						if(tmpDoc.select("div.entry-content").size()>0){
							e=tmpDoc.select("div.entry-content").get(0);
							major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
						}
						finish = true;
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}while(!finish);
				
			}
		}else if(major.getUrl().startsWith("http://www.fbs.leeds.ac.uk/")){//http://www.fbs.leeds.ac.uk/
			if(doc.select("div.page-header h1:nth-child(1)").size()>0){
				e=doc.select("div.page-header h1:nth-child(1)").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#ug_tabset_maintab2").size()>0){
				e=doc.select("#ug_tabset_maintab2").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://medhealth.leeds.ac.uk/")){//http://medhealth.leeds.ac.uk/
			if(doc.select("#container > div > nav.breadcrumb > ul > li:nth-child(1) > a").size()>0){
				e=doc.select("#container > div > nav.breadcrumb > ul > li:nth-child(1) > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#container > div > div > p:nth-child(5)").size()>0){
				e=doc.select("#container > div > div > p:nth-child(5)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#container > div > div > ul:nth-child(10)").size()>0){
				e=doc.select("#container > div > div > ul:nth-child(10)").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://www.see.leeds.ac.uk/")){//http://www.see.leeds.ac.uk/
			if(doc.select("#headerfoe > a").size()>0){
				e=doc.select("#headerfoe > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#sbtab > ul > li:nth-child(2) > a").size()>0){
				e=doc.select("#sbtab > ul > li:nth-child(2) > a").get(0);
				boolean finish = false;
				do{
					try{
						Connection tmpConn = Jsoup.connect("http://www.see.leeds.ac.uk/"+e.attr("href"));
						Document tmpDoc = tmpConn.timeout(10000).get();
						if(tmpDoc.select("#c12159").size()>0){
							e=tmpDoc.select("#c12159").get(0);
							major.setAcademicRequirements(e.text().trim());
						}
						finish = true;
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}while(!finish);
				
			}
			if(doc.select("#sbtab > ul > li:nth-child(3) > a").size()>0){
				e=doc.select("#sbtab > ul > li:nth-child(3) > a").get(0);
				boolean finish = false;
				do{
					try{
						Connection tmpConn = Jsoup.connect("http://www.see.leeds.ac.uk/"+e.attr("href"));
						Document tmpDoc = tmpConn.timeout(10000).get();
						if(tmpDoc.select("#c12156").size()>0){
							e=tmpDoc.select("#c12156").get(0);
							major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
						}
						finish = true;
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}while(!finish);
				
			}
		}else if(major.getUrl().startsWith("http://www.fine-art.leeds.ac.uk/")){//http://www.fine-art.leeds.ac.uk/
			if(doc.select("body > div.header > h2 > a").size()>0){
				e=doc.select("body > div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#entry-requirements-1").size()>0){
				e=doc.select("#entry-requirements-1").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#overview-1").size()>0){
				e=doc.select("#overview-1").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
				major.setStructure(major.getStructure().indexOf("What you study")>0?
						major.getStructure().substring(major.getStructure().indexOf("What you study")):major.getStructure());
				major.setStructure(major.getStructure().indexOf("Current modules taught")>0?
						major.getStructure().substring(0, major.getStructure().indexOf("Current modules taught")):major.getStructure());
			}
		}else if(major.getUrl().startsWith("http://www.food.leeds.ac.uk/")){//http://www.food.leeds.ac.uk/
			if(doc.select("#header > h2 > a").size()>0){
				e=doc.select("#header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#mytabs-4").size()>0){
				e=doc.select("#mytabs-4").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#mytabs-3").size()>0){
				e=doc.select("#mytabs-3").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
			if(doc.select("#mytabs-1 > div").size()>0){
				e=doc.select("#mytabs-1 > div").get(0);
				major.setTuitionFee(getFee(e.text()));
			}
		}else if(major.getUrl().startsWith("http://www.chem.leeds.ac.uk/")){//http://www.chem.leeds.ac.uk/
			if(doc.select("#header > h2 > a").size()>0){
				e=doc.select("#header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#mytabs-4").size()>0){
				e=doc.select("#mytabs-4").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#mytabs-3").size()>0){
				e=doc.select("#mytabs-3").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
			if(doc.select("#mytabs-1 > div").size()>0){
				e=doc.select("#mytabs-1 > div").get(0);
				major.setTuitionFee(getFee(e.text()));
			}
		}else if(major.getUrl().startsWith("http://www.education.leeds.ac.uk/")){//http://www.education.leeds.ac.uk/
			if(doc.select("div.header > h2 > a").size()>0){
				e=doc.select("div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#tab1 > div:nth-child(1) > ul > li:nth-child(3)").size()>0){
				e=doc.select("#tab1 > div:nth-child(1) > ul > li:nth-child(3)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#tab2").size()>0){
				e=doc.select("#tab2").get(0);
				if(doc.select("#tab2 > div.sidebar-block").size()>0){
					major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml()
							.replace(doc.select("#tab2 > div.sidebar-block").get(0).outerHtml(), ""))).trim());
				}else{
					major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
				}
			}
		}else if(major.getUrl().startsWith("http://www.geog.leeds.ac.uk/")){//http://www.geog.leeds.ac.uk/
			if(doc.select("#header > h2 > a").size()>0){
				e=doc.select("#header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#entry-requirements").size()>0){
				e=doc.select("#entry-requirements").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#course-structure").size()>0){
				e=doc.select("#course-structure").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://www.geog.leeds.ac.uk/")){//http://www.geog.leeds.ac.uk/
			if(doc.select("#container > div.header > h2 > a").size()>0){
				e=doc.select("#container > div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#tab1 > div:nth-child(1) > p > strong:nth-child(3)").size()>0){
				e=doc.select("#tab1 > div:nth-child(1) > p > strong:nth-child(3)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#tab2").size()>0){
				e=doc.select("#tab2").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://www.physics.leeds.ac.uk/")){//http://www.physics.leeds.ac.uk/
			if(doc.select("#header > h2 > a").size()>0){
				e=doc.select("#header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#mytabs-4").size()>0){
				e=doc.select("#mytabs-4").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#mytabs-3").size()>0){
				e=doc.select("#mytabs-3").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://media.leeds.ac.uk/")){//http://media.leeds.ac.uk/
			if(doc.select("body > div.header > h2 > a").size()>0){
				e=doc.select("body > div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#entry-requirements").size()>0){
				e=doc.select("#entry-requirements").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#modules").size()>0){
				e=doc.select("#modules").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://www.law.leeds.ac.uk/")){//http://www.law.leeds.ac.uk/
			if(doc.select("div.header > h2 > a").size()>0){
				e=doc.select("div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#tab1 > div.tab-sidebar > div:nth-child(2) > ul > li:nth-child(4)").size()>0){
				e=doc.select("#tab1 > div.tab-sidebar > div:nth-child(2) > ul > li:nth-child(4)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#tab2").size()>0){
				e=doc.select("#tab2").get(0);
				if(doc.select("#tab2 > div.sidebar-block").size()>0){
					major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml()
							.replace(doc.select("#tab2 > div.sidebar-block").get(0).outerHtml(), ""))).trim());
				}else{
					major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
				}
			}
		}else if(major.getUrl().startsWith("http://www.sociology.leeds.ac.uk/")){//http://www.sociology.leeds.ac.uk/
			if(doc.select("#container > div.header > h2 > a").size()>0){
				e=doc.select("#container > div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#tab1 > div:nth-child(1) > p > strong:nth-child(3)").size()>0){
				e=doc.select("#tab1 > div:nth-child(1) > p > strong:nth-child(3)").get(0);
				major.setAcademicRequirements(e.text().trim());
			}
			if(doc.select("#tab2").size()>0){
				e=doc.select("#tab2").get(0);
				if(doc.select("#tab2 > div.sidebar-block").size()>0){
					major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml()
							.replace(doc.select("#tab2 > div.sidebar-block").get(0).outerHtml(), ""))).trim());
				}else{
					major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
				}
			}
		}else if(major.getUrl().startsWith("http://www.pci.leeds.ac.uk/")){//http://www.pci.leeds.ac.uk/
			if(doc.select("body > div.header > h2 > a").size()>0){
				e=doc.select("body > div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#applying-fees-funding-1").size()>0){
				e=doc.select("#applying-fees-funding-1").get(0);
				major.setAcademicRequirements(e.text().trim().indexOf("Alternative EPQ qualifications")>0?
						e.text().trim().substring(0, e.text().trim().indexOf("Alternative EPQ qualifications")):e.text().trim());
				major.setAcademicRequirements(major.getAcademicRequirements().replace("Applying, Fees, Funding", "").trim());
			}
			if(doc.select("#course-content-1").size()>0){
				e=doc.select("#course-content-1").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}else if(major.getUrl().startsWith("http://www.design.leeds.ac.uk/")){//http://www.design.leeds.ac.uk/
			if(doc.select("body > div.header > h2 > a").size()>0){
				e=doc.select("body > div.header > h2 > a").get(0);
				major.setSchool(e.text().trim());
			}
			if(doc.select("#applying-fees-funding-1").size()>0){
				e=doc.select("#applying-fees-funding-1").get(0);
				major.setAcademicRequirements(e.text().trim().indexOf("How to apply")>0?
						e.text().trim().substring(0, e.text().trim().indexOf("How to apply")):e.text().trim());
				major.setAcademicRequirements(major.getAcademicRequirements().replace("Applying, Fees, Funding", "").trim());
			}
			if(doc.select("#course-content-1").size()>0){
				e=doc.select("#course-content-1").get(0);
				major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
			}
		}
		
		
		if(major.getLength().length()==0){
			major.setLength(getLength(major.getStructure()));
		}
		getIELTS(major.getAcademicRequirements(), major);
		
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
		boolean finish = false;
		String fee = "";
		do{
			try{
				Connection conn = Jsoup.connect(url);
				Document doc = conn.timeout(5000).get();
				if(doc.select("#block-system-main > table > tbody > tr:nth-child(2) > td:nth-child(4)").size()>0){
					fee = getFee(doc.select("#block-system-main > table > tbody > tr:nth-child(2) > td:nth-child(4)").text());
				}else{
					fee = url;
				}
				finish = true;
			}catch(IOException e){
				System.out.println("requestFee : " + e.getMessage());
			}
		}while(!finish);
		return fee;
	}
	
	public static String getType(Document doc){
		String type="";
		if(doc.select("#kw").size()>0){
			Element e = doc.select("#kw").get(0);
			StringBuilder typeURL = new StringBuilder();
			typeURL.append("http://widget.unistats.ac.uk/Widget/");
			typeURL.append(e.attr("data-institution")+"/");
			typeURL.append(e.attr("data-course")+"/");
			typeURL.append(e.attr("data-orientation")+"/");
			typeURL.append("null/");
			typeURL.append(e.attr("data-language")+"/");
			typeURL.append(e.attr("data-kismode"));
			boolean finishe = false;
			try{
				do{
					Connection tmpConn = Jsoup.connect(typeURL.toString());
					Document tmpDoc = tmpConn.timeout(10000).get();
					if(tmpDoc.select("#kisWidget > div.widgetCourse > h1").size()>0){
						e = tmpDoc.select("#kisWidget > div.widgetCourse > h1").get(0);
						type = e.text().trim().indexOf(" ")>0?
								e.text().trim().substring(0,e.text().trim().indexOf(" ")):e.text().trim();
					}
					finishe = true;
				}while(!finishe);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return type;
	}
}


