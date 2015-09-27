
import java.io.File;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

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
			initMajorList("http://www.york.ac.uk/study/undergraduate/courses/all?level=postgraduate&q=");
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
			Elements links=doc.getElementById("results").getElementsByTag("tbody").get(0).getElementsByTag("tr");
			
			MajorForCollection major;
			for(Element link:links){//get majors
				if(!link.outerHtml().contains("detail"))continue;
				major=new MajorForCollection();
				major.setTitle(link.getElementsByTag("a").get(0).text());
				major.setLevel("Postgraduate");
				major.setType(link.getElementsByClass("detail").get(0).getElementsByTag("li").get(0).text());
				major.setLength(Integer.parseInt(link.getElementsByClass("detail").get(0).getElementsByTag("li").get(1).text().substring(0,1))*12+"");
				major.setUrl(link.getElementsByTag("a").get(0).attr("href").replace("////", "//"));
				if(major.getUrl().contains("course-error"))continue;
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
		if(doc.getElementsByClass("o-grid__row").size()>0&&!major.getUrl().toLowerCase().contains("htm")){
			getDetails2(row, major);
			return;
		}
/*		if(major.getUrl().toLowerCase().contains("htm")){
			System.out.println("original url: " + major.getUrl());
			major.setUrl(doc.getElementsByTag("head").get(0).getElementsByTag("meta").get(0).attr("content").substring(7));
			throw new Exception("Throwed by self");
		}*/
		if(doc.getElementsByTag("body").get(0).text().equals("")){
			System.out.println("original url: " + major.getUrl());
			major.setUrl(doc.getElementsByTag("head").get(0).getElementsByTag("meta").get(0).attr("content").substring(7));
			throw new Exception("Throwed by self");
		}
		Element e;
		
		e=doc.getElementById("lhcolumn");
		if(e!=null){
			if(e.getElementsByTag("li").get(0).text().contains(" home")){
				major.setSchool(e.getElementsByTag("li").get(0).text().substring(0,e.getElementsByTag("li").get(0).text().indexOf(" home")));

			}else{
				major.setSchool(e.getElementsByTag("li").get(0).text());
			}
		}else{
			e=doc.getElementById("nav");
			if(e!=null){
				major.setSchool(e.getElementsByTag("li").text());
			}
		}
		
		if(major.getSchool().equals("Medicine")){
			major.setTuitionFee("25930");
		}else if(major.getSchool().equals("Biology")||major.getSchool().equals("Biochemistry")||major.getSchool().equals("Chemistry")
				||major.getSchool().equals("Computer Science")||major.getSchool().equals("Electronics")
				||major.getSchool().equals("Environment")||major.getSchool().equals("Natural Sciences")
				||major.getSchool().equals("Physics")||major.getSchool().equals("Psychology")){
			major.setTuitionFee("19500");
		}else if(major.getSchool().equals("Archaeology")||major.getSchool().equals("Economics and Related Studies")||major.getSchool().equals("Education")
				||major.getSchool().equals("English and Related Literature")||major.getSchool().equals("History")
				||major.getSchool().equals("History of Art")||major.getSchool().equals("Language and Linguistic Science")
				||major.getSchool().equals("Law")||major.getSchool().equals("Management")
				||major.getSchool().equals("Mathematics")||major.getSchool().equals("Music")
				||major.getSchool().equals("Philosophy")||major.getSchool().equals("Politics")
				||major.getSchool().equals("Politics, Economics and Philosophy")||major.getSchool().equals("Social Policy and Social Work")
				||major.getSchool().equals("Sociology")||major.getSchool().equals("Social and Political Science")
				||major.getSchool().equals("Theatre, Film and Television")){
			major.setTuitionFee("15150");
			
		}else if("BSc Environmental Geography (extended degree)".contains(major.getTitle())||
				"BSc Environmental Science (extended degree)".contains(major.getTitle())||
				"BSc Environment, Economics and Ecology (extended degree)".contains(major.getTitle())||
				"BA Human Geography and Environment (extended degree)".contains(major.getTitle())||
				"BA in Applied Social Science and Social Policy (extended degree)".contains(major.getTitle())){
			major.setTuitionFee("9000");
		}
		

		e=doc.getElementById("course-content-content");
		if(e!=null){
			if(e.text().contains("Academic integrity module")){
				major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").substring(0,
						html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").indexOf("Academic integrity module")));
			}else{
				major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\""));
			}
			
		}
		
		Elements es=doc.getElementsByClass("faq");
		if(es!=null&&es.size()>0){
			String str="";
			for(Element tmp:es){
				str+=html2Str(tmp.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"")+"\n";
				
			}
			major.setAcademicRequirements(str);;
		}else{
			e=doc.getElementById("course-applying-content");
			if(e!=null){
				if(e.text().contains("Entry requirements")){
					major.setAcademicRequirements(e.text().substring(e.text().indexOf("Entry requirements")));
				}
			}
		}

		major.setIELTS_Avg("6.5");
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
		
		major.setScholarship("New scholarships for refugees$8100;"
				+ "Scholarship for Overseas Students (SOS)$tuitionfee;"
				+ "Overseas Research Scholarship (ORS)$5000;"
				+ "Overseas Continuation Scholarship (OCS)$5000");
			
		major.setMonthOfEntry("9");
		
	}
	
	public static void getDetails2(int row,MajorForCollection major) throws Exception {
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;

		for(Element tmp:doc.getElementsByClass("o-grid__box--third")){
			if(tmp.text().contains("Learn more")){
				if(tmp.getElementsByTag("a").size()>0){
					major.setSchool(tmp.getElementsByTag("a").get(0).text());
				}
			}
		}
		
		if(major.getSchool().equals("Medicine")){
			major.setTuitionFee("25930");
		}else if(major.getSchool().equals("Biology")||major.getSchool().equals("Biochemistry")||major.getSchool().equals("Chemistry")
				||major.getSchool().equals("Computer Science")||major.getSchool().equals("Electronics")
				||major.getSchool().equals("Environment")||major.getSchool().equals("Natural Sciences")
				||major.getSchool().equals("Physics")||major.getSchool().equals("Psychology")){
			major.setTuitionFee("19500");
		}else if(major.getSchool().equals("Archaeology")||major.getSchool().equals("Economics and Related Studies")||major.getSchool().equals("Education")
				||major.getSchool().equals("English and Related Literature")||major.getSchool().equals("History")
				||major.getSchool().equals("History of Art")||major.getSchool().equals("Language and Linguistic Science")
				||major.getSchool().equals("Law")||major.getSchool().equals("Management")
				||major.getSchool().equals("Mathematics")||major.getSchool().equals("Music")
				||major.getSchool().equals("Philosophy")||major.getSchool().equals("Politics")
				||major.getSchool().equals("Politics, Economics and Philosophy")||major.getSchool().equals("Social Policy and Social Work")
				||major.getSchool().equals("Sociology")||major.getSchool().equals("Social and Political Science")
				||major.getSchool().equals("Theatre, Film and Television")){
			major.setTuitionFee("15150");
			
		}else if("BSc Environmental Geography (extended degree)".contains(major.getTitle())||
				"BSc Environmental Science (extended degree)".contains(major.getTitle())||
				"BSc Environment, Economics and Ecology (extended degree)".contains(major.getTitle())||
				"BA Human Geography and Environment (extended degree)".contains(major.getTitle())||
				"BA in Applied Social Science and Social Policy (extended degree)".contains(major.getTitle())){
			major.setTuitionFee("9000");
		}
		

		e=doc.getElementById("course-content");
		if(e!=null){
			e=e.getElementsByClass("c-tabs--vertical").get(0);
			major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\""));
			
		}
		
		e=doc.getElementById("entry");
		if(e!=null){
			major.setAcademicRequirements(e.text());;
		}
		major.setIELTS_Avg("6.5");
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
		
		major.setScholarship("New scholarships for refugees$8100;"
				+ "Scholarship for Overseas Students (SOS)$tuitionfee;"
				+ "Overseas Research Scholarship (ORS)$5000;"
				+ "Overseas Continuation Scholarship (OCS)$5000");
			
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


