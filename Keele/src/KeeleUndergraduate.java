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


public class KeeleUndergraduate {
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
	public static final String STRUCTURE_FILE_PREFIX="structure_ug";
	public static final String STRUCTURE_FILE_SUFFIX=".txt";
	
	public static boolean finish=false;
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static int rowNum=1;
	
	
	public static Set<String> usedLinkSet=new HashSet<String>();
	public static String tempUrl;
	
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
				exportExcel(FILE_PATH, "gen_data_"+SCHOOL_NAME+"_ug.xls");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void tryGet(){
		try{
			run("http://www.keele.ac.uk/ugcourses/",rowNum);
		}catch(Exception e){
			System.out.println("terminate in "+rowNum);
			usedLinkSet.remove(tempUrl);
			e.printStackTrace();
		}
	}
	public static void run(String originalUrl,int beginIndex) throws Exception{
		Connection conn=Jsoup.connect(originalUrl);
		Document doc=null;
		doc=conn.timeout(60000).get();

		Elements links=doc.getElementsByClass("leftswatch").get(0).getElementsByAttributeValueContaining("href", "/");
		links.addAll(doc.getElementsByClass("rightswatch").get(0).getElementsByAttributeValueContaining("href", "/"));
		String baseUrl="http://www.keele.ac.uk";
		rowNum=1;
		for(Element link:links){
			tempUrl=link.attr("href");
			if(rowNum<beginIndex){
				rowNum++;
				usedLinkSet.add(link.attr("href"));
				continue;
			}
			if(usedLinkSet.contains(link.attr("href"))){
				rowNum++;
				continue;
			}
			usedLinkSet.add(link.attr("href"));
			
			writeToExcel(rowNum, TITLE, link.text());
			writeToExcel(rowNum, LEVEL, "Undergraduate");
			writeToExcel(rowNum, URL, baseUrl+link.attr("href"));
			getDetails(rowNum,baseUrl+link.attr("href"));
			
			System.out.println(rowNum+"\t"+baseUrl+link.attr("href"));
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
		if(KeeleUndergraduate.row.getCell(14).getStringCellValue().contains("Mathematics")){
			writeToExcel(row, TUITION_FEE, "12500");
		}else if(KeeleUndergraduate.row.getCell(14).getStringCellValue().contains("Music")){
			writeToExcel(row, TUITION_FEE, "13800");
		}else if(KeeleUndergraduate.row.getCell(14).getStringCellValue().contains("Pharmacy")){
			writeToExcel(row, TUITION_FEE, "15100"); 
		}else if(KeeleUndergraduate.row.getCell(14).getStringCellValue().contains("Nursing")){
			writeToExcel(row, TUITION_FEE, "15100"); 
		}else if(KeeleUndergraduate.row.getCell(14).getStringCellValue().contains("Medicine")){
			writeToExcel(row, TUITION_FEE, "25100"); 
		}else{
			writeToExcel(row, TUITION_FEE, "9600"); 
		}
		if(KeeleUndergraduate.row.getCell(TITLE).getStringCellValue().toLowerCase().contains("law")||
				KeeleUndergraduate.row.getCell(14).getStringCellValue().toLowerCase().contains("law")){
			writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "6.5");
			writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.5");
		}else if(KeeleUndergraduate.row.getCell(TITLE).getStringCellValue().toLowerCase().contains("pharmacy ")||
				KeeleUndergraduate.row.getCell(14).getStringCellValue().toLowerCase().contains("pharmacy ")){
			writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "7.0");
			writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "6.5");
		}else if(KeeleUndergraduate.row.getCell(TITLE).getStringCellValue().toLowerCase().contains("physiotherapy ")||
				KeeleUndergraduate.row.getCell(14).getStringCellValue().toLowerCase().contains("physiotherapy ")){
			writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "7.0");
			writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "6.5");
		}else{
			writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, "6.0");
			writeToExcel(row, IELTS_LOWEST_REQUIREMENT, "5.5");
		}
		
		//writeToExcel(row, APPLICATION_FEE, );
		//writeToExcel(row, TUITION_FEE, );
		//writeToExcel(row, ACADEMIC_ENTRY_REQUIREMENT, );
		//writeToExcel(row, IELTS_AVERAGE_REQUIREMENT, );
		//writeToExcel(row, IELTS_LOWEST_REQUIREMENT, );
		writeToExcel(row, STRUCTURE, html2Str(doc.getElementById("tabs-2").outerHtml()));
		writeToExcel(row, LENGTH_MONTHS, getLastYear(doc.getElementById("tabs-2")));
		//writeToExcel(row, MONTH_OF_ENTRY, ""+9);
		//writeToExcel(row, SCHOLARSHIP, );
		
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
