package com.jeiel.nottingham;
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
			//initMajorList("http://www.nottingham.ac.uk/UGstudy/courses/a-zsearch.aspx?AZListing_AtoZLetter=");
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
		
		for(int i = 1;i<=26;i++){
			boolean finish = false;
			List<MajorForCollection> list = new ArrayList<MajorForCollection>();
			do{
				try {
					list.clear();
					Connection conn=Jsoup.connect(originalUrl + (char)(i+96));
					System.out.println(originalUrl + (char)(i+96));
					Document doc=conn.timeout(10000).get();
					Elements es=doc.select("#AZListing_List > div.sys_itemslist a");
					String baseUrl="http://www.nottingham.ac.uk";
					for(Element e:es){//major
						MajorForCollection major = new MajorForCollection();
						major.setApplicationFee((char)(i+96)+"");
						major.setUrl(e.attr("href").startsWith("http")?e.attr("href"):baseUrl + e.attr("href"));
						list.add(major);
					}
					finish=true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(!finish);
			majorList.addAll(list);
		}
		
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
		Document doc=conn.timeout(10000).get();
		Element e = null;
		
		if(doc.select("div.sys_factfileItem.sys_factfileUcas").size()>0){
			e=doc.select("div.sys_factfileItem.sys_factfileUcas").get(0);
			major.setApplicationFee(e.ownText());
		}
		
		if(doc.select("#ugStudyFactfile > div.parentSchool > *:nth-child(2)").size()>0){
			e=doc.select("#ugStudyFactfile > div.parentSchool > *:nth-child(2)").get(0);
			major.setSchool(e.text().trim());
		}
		
		if(doc.select("div.sys_factfileItem.sys_factfileName, div.sys_factfileItem.sys_factfileQualname").size()>0){
			e=doc.select("div.sys_factfileItem.sys_factfileName, div.sys_factfileItem.sys_factfileQualname").get(0);
			major.setTitle(e.ownText());
		}
		
		if(doc.select("div.sys_factfileItem.sys_factfileQualification").size()>0){
			e=doc.select("div.sys_factfileItem.sys_factfileQualification").get(0);
			major.setType(replaceSpecialCharacter(e.ownText().indexOf(" ")>0?e.ownText().substring(0, e.ownText().indexOf(" ")).trim():e.ownText().trim()));
		}
		
		if(doc.select("div.sys_factfileItem.sys_factfileType").size()>0){
			e=doc.select("div.sys_factfileItem.sys_factfileType").get(0);
			major.setLength(getLength(e.ownText()));
		}

		if(doc.select("#EntryRequirements").size()>0){
			e=doc.select("#EntryRequirements").get(0);
			major.setAcademicRequirements(e.text().indexOf("English language requirements")>0?
					e.text().substring(0, e.text().indexOf("English language requirements")):e.text());
			getIELTS(e.text(), major);
		}
		
		if(doc.select("#Modules").size()>0){
			e=doc.select("#Modules").get(0);
			major.setStructure(replaceSpecialCharacter(html2Str(e.outerHtml())).trim());
		}
		
		/*if(doc.text().indexOf("starts in")>0){
			major.setMonthOfEntry(doc.text().substring(doc.text().indexOf("starts in"),doc.text().indexOf("starts in")+20));
		}*/
		
		major.setMonthOfEntry("9");
		
		
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
		major.setScholarship("Edinburgh Global Undergraduate Maths Scholarships$1000;"+
		"Deutsche Post DHL Undergraduate Scholarships$2000");
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
		Pattern pY = Pattern.compile("(([1-9](-[1-9]){0,1})|([1-9]-))year");
		Pattern pM = Pattern.compile("(([1-9][0-9]*(-[1-9][0-9]*){0,1})|([1-9][0-9]*-))month");
		Pattern pW = Pattern.compile("(([1-9][0-9]*(-[1-9][0-9]*){0,1})|([1-9][0-9]*-))week");
		Matcher m = pY.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("year", "");
			System.out.println(tmp);
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}
			length = "" + Integer.parseInt(tmp)*12;
			return length;
		}
				
		m = pM.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("month", "");
			System.out.println(tmp);
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}
			length = "" + Integer.parseInt(tmp);
			return length;
		}		
				
		m = pW.matcher(content);
		if(m.find()){
			String tmp=m.group().replace("week", "");
			System.out.println(tmp);
			if(tmp.contains("-")){
				tmp=tmp.substring(0, tmp.indexOf("-"));
			}
			length = "" + (Integer.parseInt(tmp)/4+Integer.parseInt(tmp)%4>0?1:0);
			return length;
		}
		return length;
	}
	
	public static String getMonthOfEntry(String content){
		String month=content;
		if(content.toLowerCase().contains("august")){
			month = "8";
		}else if(content.toLowerCase().contains("september")){
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
}


