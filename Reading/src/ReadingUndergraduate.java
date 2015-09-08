import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;




import java.util.Set;

import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.test.FilterToHTML;
import com.jeiel.test.MajorForCollection;


public class ReadingUndergraduate {
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
	
	public static final String SCHOOL_NAME="Reading";
	public static final String FILE_PATH="C:\\Users\\Administrator\\Desktop\\wyl\\"+SCHOOL_NAME;
	public static final String STRUCTURE_PATH=FILE_PATH+"\\structure\\";
	public static final String STRUCTURE_FILE_PREFIX="structure_ug";
	public static final String STRUCTURE_FILE_SUFFIX=".txt";
	
	public static boolean finish=false;
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static int rowNum=1;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();;
	
	public static void main(String[] args) {
		try {
			
			initExcelWriter();
			initMajorList("http://www.reading.ac.uk/");
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
				exportExcel(FILE_PATH, "gen_data_"+SCHOOL_NAME+"_ug.xls");
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
			System.out.println("terminate in "+rowNum);
			e.printStackTrace();
		}
	}
	
	public static void initMajorList(String originalUrl){
		
		try {
			System.out.println("preparing majorList");
			Connection conn=Jsoup.connect(originalUrl);
			Document doc=conn.timeout(60000).get();
			Elements subjectList=doc.getElementsByTag("article").get(0).getElementsByClass("subject-list");//schools container
			Elements links=new Elements();
			for(Element link:subjectList){//get schools
				links.addAll(link.getElementsByTag("a"));
			}
			
			String baseUrl="http://www.reading.ac.uk";
			
			for(Element link:links){//get majors
				majorList.addAll(getMajorList(baseUrl+link.attr("href"),link.text()));
				//if(majorList.size()>10)return;
				//System.out.println("majorList size: "+majorList.size());
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
			
			writeToExcel(rowNum, SCHOOL, major.getSchool());			
			writeToExcel(rowNum, TITLE, major.getTitle());
			writeToExcel(rowNum, LEVEL, major.getLevel());
			writeToExcel(rowNum, TYPE, major.getType());
			writeToExcel(rowNum, LENGTH_MONTHS, ""+major.getLength());
			writeToExcel(rowNum, MONTH_OF_ENTRY, major.getMonthOfEntry());
			writeToExcel(rowNum, URL, major.getUrl());
			getDetails(rowNum,major);
			
			System.out.println(rowNum+"\t"+major.getUrl());
			rowNum++;
			/*if(rowNum>20)
				break;*/
		}
		finish=true;
	}
	
	public static List<MajorForCollection> getMajorList(String schoolUrl,String schoolName) throws Exception{

		Connection conn=Jsoup.connect(schoolUrl);
		Document doc=null;
		doc=conn.timeout(60000).get();
		Elements links=doc.getElementsByTag("section").get(0).getElementsByTag("li");
		List<MajorForCollection> list=new ArrayList<MajorForCollection>();
		String baseUrl="http://www.reading.ac.uk";
		for(Element e:links){
			MajorForCollection major=new MajorForCollection();
			major.setSchool(schoolName);
			major.setLevel("Undergraduate");
			major.setTitle(e.getElementsByTag("a").get(0).ownText().substring(
					e.getElementsByTag("a").get(0).ownText().indexOf(" ")+1));
			major.setType(e.getElementsByTag("a").get(0).ownText().substring(0, 
					e.getElementsByTag("a").get(0).ownText().indexOf(" ")));
			major.setLength(""+Integer.parseInt(e.getElementsByTag("p").get(1).text().substring(11,12))*12);
			major.setMonthOfEntry("9");
			major.setUrl(baseUrl+e.getElementsByTag("a").get(0).attr("href"));
			list.add(major);
		}
		return list;
	}

	public static void getDetails(int row,MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;
		e=doc.getElementById("Panel1");
		major.setAcademicRequirements(e.text());
		major.setIELTS_Avg(e.text().substring(e.text().indexOf("IELTS ")+6,e.text().indexOf("IELTS ")+9));
		major.setIELTS_Low(e.text().substring(e.text().indexOf("component below ")+16,e.text().indexOf("component below ")+19));
		e=doc.getElementById("Panel2").getElementsByClass("fade-panels").get(0);
		major.setStructure(html2Str(e.outerHtml()));
		e=doc.getElementById("Panel3").getElementsByTag("p").get(1);
		major.setTuitionFee(e.text().substring(e.text().indexOf("£")+1,e.text().indexOf(" per year")).replace(",",""));
		
		
		//writeToExcel(row, APPLICATION_FEE, );
		writeToExcel(row, TUITION_FEE, major.getTuitionFee());
		writeToExcel(row, ACADEMIC_ENTRY_REQUIREMENT, major.getAcademicRequirements());
		writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, major.getIELTS_Avg());
		writeToExcel(row, IELTS_LOWEST_REQUIREMENT, major.getIELTS_Low());
		writeToExcel(row, STRUCTURE, major.getStructure());
		//writeToExcel(row, MONTH_OF_ENTRY, ""+9);
		//writeToExcel(row, SCHOLARSHIP, );
		
	}

	public static void initExcelWriter()
			throws Exception {
		
		//鍒涘缓EXCEL鏂囨。绫诲瀷  
        book = new HSSFWorkbook(); 
        //鍒涘缓璇XCEL鐨勭涓�〉鍚嶄负鈥淪heet1鈥� 
         
		if(sheet==null){
			sheet = book.createSheet("Sheet1");
		}
		row = sheet.createRow((short) 0);
		//鍒涘缓绗竴琛岀殑绗竴涓崟鍏冩牸  
        row.createCell(0).setCellValue("School"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁 
        row.createCell(1).setCellValue("Level"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(2).setCellValue("Title"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(3).setCellValue("Type"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(4).setCellValue("Application Fee"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(5).setCellValue("Tuition Fee"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(6).setCellValue("Academic Entry Requirement"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(7).setCellValue("IELTS Average Requirement"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(8).setCellValue("IELTS Lowest Requirement"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(9).setCellValue("Structure"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(10).setCellValue("Length (months)"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(11).setCellValue("Month of Entry"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(12).setCellValue("Scholarship"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
        row.createCell(13).setCellValue("Url"); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
	}

	public static void writeToExcel(int rowNum, int col, String content)
			throws Exception {
		if(row==null||row.getRowNum()!=rowNum){
			row = sheet.createRow((short)rowNum);
		}
		
		row.createCell(col).setCellValue(content); // 琛ㄦ牸鐨勭涓�绗竴鍒楁樉绀虹殑鏁版嵁
	}

	public static void exportExcel(String filePath, String fileName) throws Exception {
		
		File fileDir = new File(filePath);
		File file = new File(filePath, fileName);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
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


