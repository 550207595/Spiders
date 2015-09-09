import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;



import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


public class StrathPostgraduate {
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
	
	
	public static final String FILE_PATH="C:\\Users\\Administrator\\Desktop\\wyl";
	public static final String STRUCTURE_PATH=FILE_PATH+"\\structure_pgt\\";
	public static final String STRUCTURE_FILE_PREFIX="structure_";
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
				exportExcel(FILE_PATH, "gen_data_strath_pgt.xls");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void tryGet(){
		try{
			run("http://www.strath.ac.uk/courses/",rowNum);
		}catch(Exception e){
			System.out.println("terminate in "+rowNum);
			e.printStackTrace();
		}
	}
	public static void run(String originalUrl,int beginIndex) throws Exception{
		Connection conn=Jsoup.connect(originalUrl);
		Document doc=null;
		doc=conn.timeout(10000).get();

		Elements links=doc.getElementById("container").getElementsByClass("pgt-item");
		String baseUrl="http://www.strath.ac.uk";
		rowNum=1;
		for(Element link:links){
			if(rowNum<beginIndex){
				rowNum++;
				continue;
			}
			writeToExcel(rowNum, SCHOOL, link.attr("data-department"));
			writeToExcel(rowNum, LEVEL, "Postgraduate");
			writeToExcel(rowNum, TITLE, link.getElementsByClass("uos-course-title").get(0).text());
			writeToExcel(rowNum, TYPE, link.getElementsByClass("uos-course-award").get(0).text());
			writeToExcel(rowNum, APPLICATION_FEE, "11");

			writeToExcel(rowNum, IELTS_AVERAGE_REQUIREMENT, "6.5");
			writeToExcel(rowNum, URL, baseUrl+link.getElementsByTag("a").get(0).attr("href"));
			writeToExcel(rowNum, SCHOLARSHIP, "McNAUGHTAN BURSARY￥1250;HASS UG Scholarship Award￥12,000");
			getDetails(rowNum,baseUrl+link.getElementsByTag("a").get(0).attr("href"));
			
			System.out.println(rowNum+"\t"+baseUrl+link.getElementsByTag("a").get(0).attr("href"));
			rowNum++;
			/*if(rowNum>10){
				break;
			}*/
		}

		finish=true;
	}

	public static void getDetails(int row,String url) throws Exception {
		Connection conn=Jsoup.connect(url);
		Document doc=conn.timeout(10000).get();
		Element e=doc.getElementById("uniquename-tab-2");
		writeToExcel(row, STRUCTURE, STRUCTURE_FILE_PREFIX+row+STRUCTURE_FILE_SUFFIX);
		String structure=HTMLFilter(html2Str(e.outerHtml()));
		exportStructureTxt(structure.contains("Assessment")?structure.substring(0, structure.indexOf("Assessment")):structure, row);
		Elements years=e.getElementsByTag("h4");
		if(years==null||years.size()==0||!startWith(years,"Year")){
			years=e.getElementsByTag("h3");
			if(years==null||years.size()==0||!startWith(years,"Year")){
				years=e.getElementsByTag("h3");
			}
		}
		writeToExcel(row, LENGTH_MONTHS, getLastYear(years));
		e=doc.getElementById("uniquename-tab-3");
		writeToExcel(row, ACADEMIC_ENTRY_REQUIREMENT, e.text());
		if(e.text().contains("no individual test score below ")){
			writeToExcel(row, IELTS_LOWEST_REQUIREMENT, e.text().substring(e.text().indexOf("no individual test score below ")+"no individual test score below ".length(), e.text().indexOf("no individual test score below ")+"no individual test score below ".length()+3));
		}
		
		e=doc.getElementById("uniquename-tab-4");
		
		if(e.text().indexOf("£", e.text().indexOf("£", e.text().indexOf("£")+1)+1)>0){
			writeToExcel(row, TUITION_FEE, e.text().substring(e.text().indexOf("£", e.text().indexOf("£", e.text().indexOf("£")+1)+1)+1,e.text().indexOf(" ",e.text().indexOf("£", e.text().indexOf("£", e.text().indexOf("£")+1)+1)+1)));
		}
		else{
			writeToExcel(row, TUITION_FEE, "");
		}
		writeToExcel(row, MONTH_OF_ENTRY, "9");
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
		/*sheet.addCell(new Label(0, 0, "School"));
		sheet.addCell(new Label(1, 0, "Level"));
		sheet.addCell(new Label(2, 0, "Title"));
		sheet.addCell(new Label(3, 0, "Type"));
		sheet.addCell(new Label(4, 0, "Application Fee"));
		sheet.addCell(new Label(5, 0, "Tuition Fee"));
		sheet.addCell(new Label(6, 0, "Academic Entry Requirement"));
		sheet.addCell(new Label(7, 0, "IELTS Average Requirement"));
		sheet.addCell(new Label(8, 0, "IELTS Lowest Requirement"));
		sheet.addCell(new Label(9, 0, "Structure"));
		sheet.addCell(new Label(10, 0, "Length (months)"));
		sheet.addCell(new Label(11, 0, "Month of Entry"));
		sheet.addCell(new Label(12, 0, "Scholarship"));
		sheet.addCell(new Label(13, 0, "Url"));*/
	}

	public static void writeToExcel(int rowNum, int col, String content)
			throws Exception {
		if(row==null||row.getRowNum()!=rowNum){
			row = sheet.createRow((short)rowNum);
		}
		if(col==TUITION_FEE)System.out.println(content);
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
	public static String getLastYear(Elements es){
		for(int i=es.size()-1;i>=0;i--){
			if(es.get(i).text().startsWith("Years")){
				return Integer.parseInt(es.get(i).text().substring(6,7))*12+" & "+Integer.parseInt(es.get(i).text().substring(10))*12;
			}else if(es.get(i).text().contains("/")){
				return Integer.parseInt(es.get(i).text().substring(5,6))*12+"/"+Integer.parseInt(es.get(i).text().substring(7))*12;
			}else if(es.get(i).text().startsWith("Year")){
				return Integer.parseInt(es.get(i).text().substring(5,6))*12+"";
			}
		}
		return "";
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


	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}

}
