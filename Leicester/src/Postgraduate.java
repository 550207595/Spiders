import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;




import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeiel.test.FilterToHTML;
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
	
	public static final String SCHOOL_NAME="Leicester";

	
	public static boolean finish=false;
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static int rowNum=1;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	public static Map<String, String>monthMap=new HashMap<String, String>();
	static{
		monthMap.put("April", "4");
		monthMap.put("September", "9");
		monthMap.put("October", "10");
		monthMap.put("November", "11");
	}
	
	public static void main(String[] args) {
		try {
			//getFee(null);
			System.out.println(new Date());
			initExcelWriter();
			initMajorList("https://le.ac.uk/courses?q=&level=Postgraduate+Course&Page=");
			System.out.println("start");
			while(!finish){
				System.out.println("tryGet "+rowNum);
				tryGet();
			}
			System.out.println("finish");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				exportExcel("gen_data_"+SCHOOL_NAME+"_ptg.xls");
				System.out.println(new Date());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void tryGet(){
		try{
			run(rowNum);
		}catch(Exception e){
			System.out.println("terminate in "+rowNum+"\t"+majorList.get(rowNum-1).getUrl());
			e.printStackTrace();
		}
	}
	
	public static void initMajorList(String originalUrl){
		
		try {
			System.out.println("preparing majorList");
			Connection conn;
			Document doc;
			Elements links;
			for(int i=1;i<=16;i++){
				conn=Jsoup.connect(originalUrl+i);
				doc=conn.timeout(60000).get();
				links=doc.getElementsByClass("search-result-list");
				MajorForCollection major;
				for(Element link:links){//get majors
					link=link.getElementsByTag("li").get(0);
					major=new MajorForCollection();
					major.setTitle(link.getElementsByTag("h4").get(0).getElementsByTag("a").get(0).text());
					major.setType(major.getTitle().substring(major.getTitle().lastIndexOf(" ")+1));
					major.setTitle(major.getTitle().substring(0, major.getTitle().lastIndexOf(" ")));
					major.setLevel("Postgraduate");
					major.setLength(link.getElementsByClass("course-meta-desc").get(0).text().split("\\|")[2].trim());
					if(major.getLength().contains("year")){
						major.setLength(Integer.parseInt(major.getLength().substring(0, 1))*12+"");
					}else if(major.getLength().contains("month")){
						major.setLength(major.getLength().substring(0, 2));
					}
					major.setUrl(link.getElementsByTag("h4").get(0).getElementsByTag("a").get(0).attr("href"));
					majorList.add(major);
				}
			}
			
			
			
			System.out.println("majorList prepared");
			System.out.println("majorList size: "+majorList.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	
	public static void run(int beginIndex) throws Exception{
		
		rowNum=1;
		for(MajorForCollection major:majorList){
			if(rowNum<beginIndex){
				rowNum++;
				continue;
			}
			getDetails(rowNum,major);
			System.out.println(rowNum+"\t"+major.getUrl());
			rowNum++;
			/*if(rowNum>20)
				break;*/
		}
		finish=true;
	}

	public static void getDetails(int row,MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;
		
		e=doc.getElementById("tab__international-students");
		if(e!=null){
			major.setTuitionFee(e.text());
			if(e.text().contains("£")){
				if(e.text().indexOf(" ", e.text().indexOf("£"))>0){
					major.setTuitionFee(e.text().substring(e.text().indexOf("£"), e.text().indexOf(" ", e.text().indexOf("£"))).replace("£", "").replace(",", ""));
				}else{
					major.setTuitionFee(e.text().substring(e.text().indexOf("£")));
				}
				
			}
		}
		
		
		e=doc.getElementsByClass("requirements__column").get(0);
		major.setAcademicRequirements(e.text());
		
		e=doc.getElementsByClass("requirements__column").get(1);
		if(e.text().contains("IELTS")){
			if(e.text().contains("7.0")){
				major.setIELTS_Avg("7.0");
				if(e.text().contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(e.text().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(e.text().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("6.5")){
				major.setIELTS_Avg("6.5");
				if(e.text().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(e.text().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("6.0")){
				major.setIELTS_Avg("6.0");
				if(e.text().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("5.5")){
				major.setIELTS_Avg("5.5");
				if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("5.0")){
				major.setIELTS_Avg("5.0");
				major.setIELTS_Low("5.0");
			}
		}
		
		e=doc.getElementsByClass("tab-content__list").get(0);
		if(e!=null){
			major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\""));
		}
			
		Elements es = doc.getElementsByClass("key-facts__heading");
		for(Element tmp:es){
			if(tmp.text().contains("Start date")){
				major.setMonthOfEntry(" "+doc.getElementsByClass("key-facts__content").get(es.indexOf(tmp)).text());
				major.setMonthOfEntry(major.getMonthOfEntry().substring(
						major.getMonthOfEntry().lastIndexOf(" ", major.getMonthOfEntry().indexOf(" each year")-1), 
						major.getMonthOfEntry().indexOf(" each year")).trim());
				major.setMonthOfEntry(monthMap.get(major.getMonthOfEntry()));
			}else if(tmp.text().contains("Department")){
				major.setSchool(doc.getElementsByClass("key-facts__content").get(es.indexOf(tmp)).text());
			}
		}
		major.setScholarship("Santander Scholarship$5000;"+
							"Masters Excellence Studentships$5500");
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
}


