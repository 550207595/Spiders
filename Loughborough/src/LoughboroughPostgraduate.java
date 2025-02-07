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


public class LoughboroughPostgraduate {
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
	
	public static final String SCHOOL_NAME="Loughborough";

	
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
			initMajorList("http://www.lboro.ac.uk/study/postgraduate/programmes/");
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
				exportExcel("gen_data_"+SCHOOL_NAME+"_pgt.xls");
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
			Elements links=doc.getElementsByClass("a-to-z-clearing").get(0).getElementsByClass("ug-course-info");
			
			String baseUrl="http://www.lboro.ac.uk";
			MajorForCollection major;
			for(Element link:links){//get majors
				major=new MajorForCollection();
				major.setType(link.getElementsByTag("h3").get(0).text());
				major.setTitle(link.getElementsByTag("a").get(0).text());
				major.setLevel("Postgraduate");
				if(link.getElementsByTag("a").size()>1){
					major.setSchool(link.getElementsByTag("a").get(1).text());
				}
				
				major.setUrl(baseUrl+link.getElementsByTag("a").get(0).attr("href"));
				majorList.add(major);
			}
			
			
			System.out.println("majorList prepared");
			System.out.println("majorList size: "+majorList.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public static void getFee(MajorForCollection major){
		HttpURLConnection connection=null;
		try {
			URL url = new URL("http://regweb.lboro.ac.uk/fees/service/search.php?"
					+"title="+major.getTitle()
					+"&level=P&year=2016");
			connection = (HttpURLConnection) url.openConnection();
		    connection.setDoInput(true);
		    connection.setRequestMethod("GET");
    
		    connection.connect();

		    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    String lines;
		    StringBuffer sb = new StringBuffer("");
		    while ((lines = reader.readLine()) != null) {
		    	lines = new String(lines.getBytes("utf-8"));
		    	sb.append(lines);
		    }
		    Matcher matcher=Pattern.compile("\"class\":\"international\",\"value\":\"[0-9]+\"").matcher(sb);
		    if(matcher.find()){//get the first match
		    	String str=matcher.group();
		    	major.setTuitionFee(str.substring(str.indexOf("value\":")+8,str.lastIndexOf("\"")));
		    	System.out.println(major.getTuitionFee());
		    }
		    //System.out.println(sb.toString().replace("searchResults(", "").replace(");", ""));
		    //JSONObject obj=JSONObject.fromString(sb.substring(14, sb.length()-2));
		    
		    
		    System.out.println(major.getTitle());
		    
		    //System.out.println(obj);
		    
		    //System.out.println(obj.getJSONArray("FTprogrammes").getJSONObject(0));
		    /*if(sb.toString().contains("status: \"success\""))
		    major.setTuitionFee(sb.substring(sb.indexOf("\"value\"", sb.indexOf("\"class\":\"international\""))+"\"value\":\"".length(),
					sb.indexOf("\",\"status\"", sb.indexOf("\"class\":\"international\""))));*/
			//System.out.println(major.getTuitionFee());
			getFeeSuccessed=true;
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			e.printStackTrace();
			System.out.print("getFee failed! Retrying: ");
			System.out.println("http://regweb.lboro.ac.uk/fees/service/search.php?"
					+"title="+major.getTitle()
					+"&level=P&year=2016");
		}finally{
			if(connection!=null){
				connection.disconnect();
			}
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
		if(major.getUrl().equals("http://www.lboro.ac.uk"))return;
		if(row==12||row==14||row==51||row==52)return;
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;
		e=doc.getElementById("main-header-block").getElementsByTag("h4").get(1);
		if(e!=null){
			if(e.text().contains(" year full-time")){
				major.setLength(Integer.parseInt(e.text().substring(e.text().indexOf(" year full-time")-1,e.text().indexOf(" year full-time")))*12+"");
			}else if(e.text().contains("years")&&!e.text().startsWith("P")){
				major.setLength(Integer.parseInt(e.text().substring(0,1))*12+"");
			}else{
				major.setLength(e.text());
			}
			
			//major.setLength(""+Integer.parseInt(e.text().substring(e.text().indexOf(" y")-1, e.text().indexOf(" y")))*12);
		}
		
		if(doc.getElementById("accordion")!=null){
			int i=1;
			System.out.println(doc.getElementById("accordion"));
			for(Element tmp:doc.getElementById("accordion").getElementsByTag("h3")){
				if(tmp.text().contains("Entry")){
					System.out.println("i=" + i);
					e=doc.getElementById("accordion").getElementsByTag("div").get(i);
					major.setAcademicRequirements(e.text());
					System.out.println(major.getAcademicRequirements());
					break;
				}
				i++;
			}
			
		}
		
		major.setIELTS_Avg("6.5");
		major.setIELTS_Low("6.0");
			
		if(major.getSchool().equals("School of Business and Economics")){
			major.setIELTS_Avg("7.0");
			major.setIELTS_Low("6.5");
		}else if(major.getTitle().equals("Communication and Media Studies")){
			major.setIELTS_Avg("7.0");
		}
		getFeeSuccessed=false;
		while(!getFeeSuccessed){
			getFee(major);
		}
		
		
		e=doc.getElementById("structure");
		if(e!=null){
			//System.out.println(new HtmlToPlainText().getPlainText(e));
			major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\""));
			//System.out.println(major.getStructure());
		}
			
		//e=doc.getElementById("Panel3").getElementsByTag("p").get(1);
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


