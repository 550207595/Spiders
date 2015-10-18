package com.jeiel.test;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
	
	public static final String SCHOOL_NAME="Sheffield";

	
	public static boolean finish=false;
	public static HSSFWorkbook book=null;
	public static HSSFSheet sheet =null; 
	public static HSSFRow row=null;
	public static int rowNum=1;
	public static List<MajorForCollection> majorList=new ArrayList<MajorForCollection>();
	
	
	public static void main(String[] args) {
		try {
			initExcelWriter();
			initMajorList("https://www.sheffield.ac.uk/prospectus/courseCategory.do;jsessionid=D1D1E82F3D594079672DFBB1E5F20601.tcs-live-node-01?category=azlist");
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
			Connection conn=Jsoup.connect(originalUrl);;
			Document doc=conn.timeout(60000).get();
			
			Elements es=doc.getElementsByAttributeValue("summary", "main layout table").get(0).getElementsByTag("td").get(0).getElementsByTag("ul");
			es.remove(0);
			String baseUrl="https://www.sheffield.ac.uk/prospectus/";
			for(Element e:es){//ul
				for(Element li:e.getElementsByTag("li")){//a
					MajorForCollection major = new MajorForCollection();
					major.setTitle(li.getElementsByTag("a").text());
					major.setLevel("Undergraduate");
					major.setType(li.text().substring(li.text().indexOf("-")+2, li.text().lastIndexOf("(")));
					major.setTuitionFee("9000");
					major.setMonthOfEntry("9");
					major.setUrl(baseUrl + li.getElementsByTag("a").get(0).attr("href"));
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
		
		if(doc.text().contains("Duration")){
			doc.text().subSequence(doc.text().indexOf("Duration"), doc.text().indexOf(" ", doc.text().indexOf("year", doc.text().indexOf("Duration"))));
			major.setLength("" + doc.text().subSequence(doc.text().indexOf("Duration"), doc.text().indexOf(" ", doc.text().indexOf("year", doc.text().indexOf("Duration")))));
			major.setLength(""+Integer.parseInt(major.getLength().substring(major.getLength().indexOf(" "), major.getLength().lastIndexOf(" ")).trim())*12);
		}
		e=doc.getElementById("department");
		major.setSchool(e.getElementsByTag("h3").get(0).text());
		//System.out.println(major.getSchool());
		

		
		e=doc.getElementById("qualifications");
		//System.out.println(e.text());
		for(Element tr:e.getElementsByTag("tr")){
			if(tr.text().contains("Access to HE"))break;
			major.setAcademicRequirements(major.getAcademicRequirements() + tr.text() + ";");
		}
		//System.out.println(major.getAcademicRequirements());
		major.setAcademicRequirements(major.getAcademicRequirements().substring(0, major.getAcademicRequirements().length()-2));
		e = e.getElementsByClass("pinkbg").get(0);
		
		if(e.text().contains("IELTS")){
			if(e.text().contains("8.5")){
				major.setIELTS_Avg("8.5");
				if(e.text().contains("8.0")){
					major.setIELTS_Low("8.0");
				}else if(e.text().contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(e.text().contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(e.text().contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(e.text().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(e.text().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("8.0")){
				major.setIELTS_Avg("8.0");
				if(e.text().contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(e.text().contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(e.text().contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(e.text().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(e.text().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("7.5")){
				major.setIELTS_Avg("7.5");
				if(e.text().contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(e.text().contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(e.text().contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(e.text().contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(e.text().contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(e.text().contains("7.0")){
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
		}else if(major.getAcademicRequirements().contains("IELTS")){
			if(major.getAcademicRequirements().contains("8.5")){
				major.setIELTS_Avg("8.5");
				if(major.getAcademicRequirements().contains("8.0")){
					major.setIELTS_Low("8.0");
				}else if(major.getAcademicRequirements().contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(major.getAcademicRequirements().contains("7.0")){
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
			}else if(major.getAcademicRequirements().contains("8.0")){
				major.setIELTS_Avg("8.0");
				if(major.getAcademicRequirements().contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(major.getAcademicRequirements().contains("7.0")){
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
			}else if(major.getAcademicRequirements().contains("7.5")){
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
			}else if(major.getAcademicRequirements().contains("5.0")){
				major.setIELTS_Avg("5.0");
				major.setIELTS_Low("5.0");
			}
		}else{
			major.setIELTS_Avg("6.5");
		}
		
		
		
		e=doc.getElementById("modules");
		if(e!=null){
			major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\""));
		}
			
		major.setScholarship("Alumni Fund scholarships$3000;"+
							"HSBC care leavers' scholarships$1000;"+
							"Asylum Seeker Scholarships$9840;"+
							"Sports scholarships$1000");
		if(major.getSchool().contains("Automatic Control and Systems Engineering")){
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
		}
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


