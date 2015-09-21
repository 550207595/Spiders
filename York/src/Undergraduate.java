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
import java.util.HashSet;
import java.util.List;




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
	
	public static final String SCHOOL_NAME="York";

	
	public static boolean finish=false;
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static int rowNum=1;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	public static JSONArray feeArray;
	public static boolean getFeeSuccessed;
	
	public static void main(String[] args) {
		try {
			//getFee(null);
			initExcelWriter();
			initMajorList("http://www.york.ac.uk/study/undergraduate/courses/all?level=undergraduate&q=");
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
				exportExcel("gen_data_"+SCHOOL_NAME+"_ug.xls");
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
			Connection conn=Jsoup.connect(originalUrl);
			Document doc=conn.timeout(60000).get();
			Elements links=doc.getElementById("results").getElementsByTag("tbody").get(0).getElementsByTag("tr");
			
			MajorForCollection major;
			for(Element link:links){//get majors
				if(!link.outerHtml().contains("detail"))continue;
				major=new MajorForCollection();
				major.setTitle(link.getElementsByTag("a").get(0).text());
				major.setLevel("Undergraduate");
				major.setType(link.getElementsByClass("detail").get(0).getElementsByTag("li").get(0).text());
				major.setLength(Integer.parseInt(link.getElementsByClass("detail").get(0).getElementsByTag("li").get(1).text().substring(0,1))*12+"");
				major.setUrl(link.getElementsByTag("a").get(0).attr("href").replace("////", "//"));
				majorList.add(major);
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
		/*if(doc.text().contains("IELTS")){
			System.out.println(doc.text());
		}
		e=doc.getElementById("main-header-block");
		if(e!=null){
			major.setSchool(e.getElementsByTag("h5").get(0).text());
		}
		
		if(doc.getElementById("options")!=null){
			e=doc.getElementById("options").getElementsByClass("courseinfo").get(0);
			major.setAcademicRequirements(e.text());
		}
		
		if(!doc.text().contains("IELTS")){
			major.setIELTS_Avg("6.5");
			major.setIELTS_Low("6.0");
		}
		if(major.getSchool().equals("School of Business and Economics")){
			major.setIELTS_Avg("7.0");
			major.setIELTS_Low("6.5");
		}else if(major.getTitle().equals("Communication and Media Studies")){
			major.setIELTS_Avg("7.0");
		}*/

		e=doc.getElementById("course-content-content");
		if(e!=null){
			major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").substring(0,
					html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").indexOf("Academic integrity module")));
		}
		
		Elements es=doc.getElementsByClass("faq");
		if(es!=null){
			String str="";
			for(Element tmp:es){
				str+=html2Str(tmp.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"")+"\n";
				
			}
			major.setAcademicRequirements(str);;
		}
		if(major.getAcademicRequirements().contains("IELTS")){
			if(major.getAcademicRequirements().contains("7.5")){
				major.setIELTS_Avg("7.5");
				if(major.getAcademicRequirements().contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(major.getAcademicRequirements().contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(major.getAcademicRequirements().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(major.getAcademicRequirements().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(major.getAcademicRequirements().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(major.getAcademicRequirements().contains("7.0")){
				major.setIELTS_Avg("7.0");
				if(major.getAcademicRequirements().contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(major.getAcademicRequirements().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(major.getAcademicRequirements().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(major.getAcademicRequirements().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(major.getAcademicRequirements().contains("6.5")){
				major.setIELTS_Avg("6.5");
				if(major.getAcademicRequirements().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(major.getAcademicRequirements().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(major.getAcademicRequirements().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(major.getAcademicRequirements().contains("6.0")){
				major.setIELTS_Avg("6.0");
				if(major.getAcademicRequirements().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(major.getAcademicRequirements().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(major.getAcademicRequirements().contains("5.5")){
				major.setIELTS_Avg("5.5");
				if(major.getAcademicRequirements().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}
		}
			
		major.setMonthOfEntry("9");
		
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


