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


public class KeelePostgraduate {
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
	
	public static final String SCHOOL_NAME="Keele";
	public static final String FILE_PATH="C:\\Users\\Jeiel\\Desktop\\wyl\\"+SCHOOL_NAME;
	public static final String STRUCTURE_PATH=FILE_PATH+"\\structure\\";
	public static final String STRUCTURE_FILE_PREFIX="structure_pgt";
	public static final String STRUCTURE_FILE_SUFFIX=".txt";
	
	public static boolean finish=false;
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static int rowNum=1;
	
	
	
	public static void main(String[] args) {
		try {
			System.out.println("start");
			initExcelWriter();
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
				exportExcel(FILE_PATH, "gen_data_"+SCHOOL_NAME+"_pgt.xls");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void tryGet(){
		try{
			run("http://www.keele.ac.uk/pgtcourses/",rowNum);
		}catch(Exception e){
			System.out.println("terminate in "+rowNum);
			e.printStackTrace();
		}
	}
	public static void run(String originalUrl,int beginIndex) throws Exception{
		Connection conn=Jsoup.connect(originalUrl);
		Document doc=null;
		doc=conn.timeout(60000).get();
		Elements divs=doc.getElementsByClass("toggler-c");
		divs.remove(0);
		Elements links=new Elements();
		for(Element e:divs){
			links.addAll(e.getElementsByTag("a"));
		}
		
		String baseUrl="http://www.keele.ac.uk";
		rowNum=1;
		for(Element link:links){
			if(rowNum<beginIndex){
				rowNum++;
				continue;
			}

			
			writeToExcel(rowNum, TITLE, link.text());
			writeToExcel(rowNum, LEVEL, "Postgraduate");
			writeToExcel(rowNum, URL, baseUrl+link.attr("href"));
			getDetails(rowNum,link.attr("href").contains("http:")?link.attr("href"):baseUrl+link.attr("href"));
			
			System.out.println(rowNum+"\t"+(link.attr("href").contains("http:")?link.attr("href"):baseUrl+link.attr("href")));
			rowNum++;
			/*if(rowNum>20)
				break;*/
		}
		finish=true;
	}

	public static void getDetails(int row,String url) throws Exception {
		Connection conn=Jsoup.connect(url);
		Document doc=conn.timeout(60000).get();
		if(doc.getElementsByClass("keycontent").size()==0)return;
		Elements es=doc.getElementsByClass("keycontent").get(0).getElementsByTag("tr");
		for(Element e:es){
			if(e.text().toLowerCase().contains("faculty")){
				writeToExcel(row, SCHOOL, e.getElementsByTag("td").get(1).text());
			}else if(e.text().toLowerCase().contains("title")){
				writeToExcel(row, TITLE, e.getElementsByTag("td").get(1).text());
			}else if(e.text().toLowerCase().contains("type")){
				writeToExcel(row, TYPE, e.getElementsByTag("td").get(1).text());
			}else if(e.text().toLowerCase().contains("subject")){
				writeToExcel(row, 14, e.getElementsByTag("td").get(1).text());
			}
		}
		
		//writeToExcel(row, APPLICATION_FEE, );
		//writeToExcel(row, TUITION_FEE, );
		writeToExcel(row, ACADEMIC_ENTRY_REQUIREMENT, HTMLFilter(html2Str(doc.getElementById("tabs-3").outerHtml())));
		if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("IELTS")){
			if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("7.0")){
				writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "7.0");
				if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("6.5")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "6.5");
				}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("6.0")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "6.0");
				}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.5")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.5");
				}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.0")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.0");
				}
			}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("6.5")){
				writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "6.5");
				if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("6.0")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "6.0");
				}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.5")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.5");
				}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.0")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.0");
				}
			}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("6.0")){
				writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "6.0");
				if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.5")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.5");
				}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.0")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.0");
				}
			}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.5")){
				writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "5.5");
				if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.0")){
					writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.0");
				}
			}else if(KeelePostgraduate.row.getCell(ACADEMIC_ENTRY_REQUIREMENT).getStringCellValue().contains("5.0")){
				writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "5.0");
				writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.0");
			}
		}
		if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Physiotherapy")){
			writeToExcel(row, TUITION_FEE, "13300");
		}else if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Creative Writing")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("English Literatures")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("History")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Humanities")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Global Media and Culture")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Dialogue Studies")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Diplomatic Studies")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Environmental Politics")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("European Politics and Culture")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Global Security")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("International Relations")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Political Parties and Elections")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Climate Change Studies")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Child Care Law and Practice")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Safeguarding Adults")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Medical Ethics and Palliative Care")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Criminology and Criminal Justice")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Social Science Research Methods")){
			writeToExcel(row, TUITION_FEE, "13000");
		}else if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Education")){
			writeToExcel(row, TUITION_FEE, "10000");
		}else if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Human Rights")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Medical Ethics and Law")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("International Law")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Law and Society")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Internet and Web Technologies")){
			writeToExcel(row, TUITION_FEE, "13750");
		}else if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Project Management")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Information Technology and Management")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Molecular Parasitology and Vector Biology")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Biomedical Blood Science")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Scientific")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Medical Science")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Biomedical Engineering")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Cell and Tissue Engineering")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Child Development")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Cognitive Psychology")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Counselling Psychology")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Counselling Psychology Studies")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Psychology of Health and Wellbeing")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Social and Community Psychology")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Environmental Sustainability and Green Technology")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Geoscience Research")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Analytical Science")){
			writeToExcel(row, TUITION_FEE, "14300");
		}else if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Management")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Finance")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Marketing")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Human Resource Management")||
				KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("International Business")){
			writeToExcel(row, TUITION_FEE, "14000");
		}else if(KeelePostgraduate.row.getCell(TITLE).getStringCellValue().contains("Clinical Psychological Research")){
			writeToExcel(row, TUITION_FEE, "14800");
		}else{
			writeToExcel(row, TUITION_FEE, "?");
		}
		
		
		writeToExcel(row, STRUCTURE, HTMLFilter(html2Str(doc.getElementById("tabs-4").outerHtml())));
		writeToExcel(row, LENGTH_MONTHS, getLastYear(doc.getElementById("tabs-4")));
		writeToExcel(row, MONTH_OF_ENTRY, ""+9);
		writeToExcel(row, SCHOLARSHIP, "The Keele Graduate Bursary (Keele Graduates Only)$1000;"
				+ "The Keele Graduate Scholarship (Open To All)$500;"
				+ "The Keele Access Scholarship$850;"
				+ "Keele Postgraduate Support Scheme Scholarships$10000");
		
	}

	public static void initExcelWriter()
			throws Exception {
		
		//创建EXCEL文档类型  
        book = new HSSFWorkbook(); 
        //创建该EXCEL的第一页名为“Sheet1”  
         
		if(sheet==null){
			sheet = book.createSheet("Sheet1");
		}
		row = sheet.createRow((short) 0);
		//创建第一行的第一个单元格  
        row.createCell(0).setCellValue("School"); // 表格的第一行第一列显示的数据 
        row.createCell(1).setCellValue("Level"); // 表格的第一行第一列显示的数据
        row.createCell(2).setCellValue("Title"); // 表格的第一行第一列显示的数据
        row.createCell(3).setCellValue("Type"); // 表格的第一行第一列显示的数据
        row.createCell(4).setCellValue("Application Fee"); // 表格的第一行第一列显示的数据
        row.createCell(5).setCellValue("Tuition Fee"); // 表格的第一行第一列显示的数据
        row.createCell(6).setCellValue("Academic Entry Requirement"); // 表格的第一行第一列显示的数据
        row.createCell(7).setCellValue("IELTS Average Requirement"); // 表格的第一行第一列显示的数据
        row.createCell(8).setCellValue("IELTS Lowest Requirement"); // 表格的第一行第一列显示的数据
        row.createCell(9).setCellValue("Structure"); // 表格的第一行第一列显示的数据
        row.createCell(10).setCellValue("Length (months)"); // 表格的第一行第一列显示的数据
        row.createCell(11).setCellValue("Month of Entry"); // 表格的第一行第一列显示的数据
        row.createCell(12).setCellValue("Scholarship"); // 表格的第一行第一列显示的数据
        row.createCell(13).setCellValue("Url"); // 表格的第一行第一列显示的数据
        row.createCell(14).setCellValue("Subject Area"); 
	}

	public static void writeToExcel(int rowNum, int col, String content)
			throws Exception {
		if(row==null||row.getRowNum()!=rowNum){
			row = sheet.createRow((short)rowNum);
		}
		
		row.createCell(col).setCellValue(content); // 表格的第一行第一列显示的数据
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
	public static void exportStructureTxt(String structure,int index) throws Exception{
		File fileDir=new File(STRUCTURE_PATH);
		File file=new File(STRUCTURE_PATH, STRUCTURE_FILE_PREFIX+index+STRUCTURE_FILE_SUFFIX);
		if(!fileDir.exists()){
			fileDir.mkdirs();
		}
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream fos=new FileOutputStream(file);
		fos.write(structure.getBytes());
		fos.close();
	}
	
	public static boolean startWith(Elements es,String prefix){
		boolean flag=true;
		for(Element e:es){
			flag&=e.text().startsWith(prefix);
		}
		return flag;
	}
	public static String getLastYear(Element e){
		if(e.outerHtml().toLowerCase().contains("semester 6")||e.outerHtml().toLowerCase().contains("year 3")||e.outerHtml().toLowerCase().contains("year three")||e.outerHtml().toLowerCase().contains("3 years")||e.outerHtml().toLowerCase().contains("three-year")||e.outerHtml().toLowerCase().contains("three years")){
			return "36";
		}else if(e.outerHtml().toLowerCase().contains("semester 4")||e.outerHtml().toLowerCase().contains("year 2")||e.outerHtml().toLowerCase().contains("year two")||e.outerHtml().toLowerCase().contains("two-year")||e.outerHtml().toLowerCase().contains("two years")){
			return "24";
		}else if(e.outerHtml().toLowerCase().contains("semester 2")||e.outerHtml().toLowerCase().contains("second semester")||e.outerHtml().toLowerCase().contains("year 1")||e.outerHtml().toLowerCase().contains("year one")||e.outerHtml().toLowerCase().contains("one-year")||e.outerHtml().toLowerCase().contains("one years")){
			return "12";
		}
		return "";
	}
	
	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}
	
	public static String HTMLFilter(String input) {
	    // 浣垮け鍘荤敤澶勭殑鏍囩浠庢柊鏈変綔鐢?
	    if (input == null) {
	        input = "";
	        return input;
	    }
	    input = input.trim().replaceAll("&amp;", "&");
	    input = input.trim().replaceAll("&lt;", "<");
	    input = input.trim().replaceAll("&gt;", ">");
	    input = input.trim().replaceAll("    ", " ");
	    input = input.trim().replaceAll("\n", "\r\n");
	    input = input.trim().replaceAll("<br>", "\n");
	    input = input.trim().replaceAll("&nbsp;", "  ");
	    input = input.trim().replaceAll("&quot;", "\"");
	    input = input.trim().replaceAll("&#39;", "'");
	    input = input.trim().replaceAll("&#92;", "\\\\");

	    return input;
	}
}
