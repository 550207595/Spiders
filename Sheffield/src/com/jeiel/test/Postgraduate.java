package com.jeiel.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;



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
			initMajorList("http://sheffield.ac.uk/postgraduate/taught/courses/all");
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
			Connection conn=Jsoup.connect(originalUrl);;
			Document doc=conn.timeout(60000).get();
			
			Elements es=doc.getElementsByAttributeValue("class", "main content").get(0).getElementsByTag("p");
			es.remove(0);
			String baseUrl="http://sheffield.ac.uk";
			for(Element e:es){//p
				MajorForCollection major = new MajorForCollection();
				if(e.text().contains("-")){
					major.setTitle(e.text().substring(0, e.text().indexOf("-")-1));
					major.setType(e.text().substring(e.text().indexOf("-")+2));
				}else if(e.text().contains("–")){
					major.setTitle(e.text().substring(0, e.text().indexOf("–")-1));
					major.setType(e.text().substring(e.text().indexOf("–")+2));
				}else{
					major.setTitle(e.text());
				}
				
				major.setLevel("Postgraduate");
				major.setMonthOfEntry("9");
				major.setUrl(e.getElementsByTag("a").get(0).attr("href").contains("http")?e.getElementsByTag("a").get(0).attr("href") : baseUrl + e.getElementsByTag("a").get(0).attr("href"));
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
		if(major.getUrl().contains("broken-link"))return;
		Connection conn=Jsoup.connect(major.getUrl());
		Document doc=conn.timeout(60000).get();
		Element e;
		e = doc.getElementsByAttributeValue("class", "main content").get(0).getElementsByTag("p").get(0);
		major.setSchool(e.text());
		
		if(doc.text().contains("duration")){
			if(doc.text().indexOf("year", doc.text().indexOf("duration"))>0){
				//System.out.println(doc.text().substring(doc.text().indexOf("year", doc.text().indexOf("duration"))-2, doc.text().indexOf("year", doc.text().indexOf("duration"))+4));
				major.setLength(doc.text().substring(doc.text().indexOf("year", doc.text().indexOf("duration"))-2, doc.text().indexOf("year", doc.text().indexOf("duration"))+4).trim());
				if(major.getLength().contains("1")){
					major.setLength("12");
				}else if(major.getLength().contains("2")){
					major.setLength("24");
				}if(major.getLength().contains("3")){
					major.setLength("36");
				}if(major.getLength().contains("4")){
					major.setLength("48");
				}if(major.getLength().contains("5")){
					major.setLength("60");
				}
			}
		}
		
		if(doc.getElementsByClass("layout")!=null&&doc.getElementsByClass("layout").size()>0){
			for(Element table :doc.getElementsByClass("layout")){
				if(table.text().contains("Key facts")){
					//System.out.println(table.getElementsByTag("td").get(0).childNodeSize());
					for(Node tmp:table.getElementsByTag("td").get(0).childNodes()){
						if(!tmp.toString().trim().equals("")){
							String str =html2Str(tmp.toString());
							//System.out.println("Node:" + str);
							if(str.equals("Entry requirements")){
								str=table.getElementsByTag("td").get(0).childNodes().get(table.getElementsByTag("td").get(0).childNodes().indexOf(tmp)+2).toString();
								str=html2Str(str);
								major.setAcademicRequirements(str);
							}else if(str.equals("English language requirements")){
								str=table.getElementsByTag("td").get(0).childNodes().get(table.getElementsByTag("td").get(0).childNodes().indexOf(tmp)+2).toString();
								str=html2Str(str);
								ExtractIELTS(str, major);
							}
						}
					}
				}else if(table.text().contains("About the course")){
					if(table.getElementsByTag("td")!=null&&table.getElementsByTag("td").size()>0){
						major.setStructure(html2Str(table.getElementsByTag("td").get(0).outerHtml()).replace("Apply now", "").trim());
					}
				}
			}
		}
		
		/*e=doc.getElementById("modules");
		if(e!=null){
			major.setStructure(html2Str(e.outerHtml()).replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\""));
		}*/
		if(doc.getElementsByAttributeValueContaining("src", "course")!=null&&doc.getElementsByAttributeValueContaining("src", "course").size()>0){
			e=doc.getElementsByAttributeValueContaining("src", "course").get(0);
			major.setTuitionFee(getFee(e.attr("src").substring(e.attr("src").indexOf("course"))));
		}
		
		
		major.setScholarship("Sheffield Benefactors' Scholarships$10000;"+
							"Postgraduate Support Scheme scholarship$10000");
	}
	
	public static String getFee(String courseId){
		if(courseId==null||courseId.equals(""))return "";
		String url = "http://ssd.dept.shef.ac.uk/fees/pgt/api/lookup.php?year=2015&status=Overseas&";
		String fee="";
		Connection conn=Jsoup.connect(url+courseId);
		try {
			Document doc=conn.timeout(60000).get();
			if(doc.text().contains("£")){
				fee = doc.text().substring(doc.text().indexOf("£")+1, 
						doc.text().indexOf(".",doc.text().indexOf("£")+1));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fee;
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
	
	public static void ExtractIELTS(String str, MajorForCollection major){
		if(str.contains("IELTS")){
			if(str.contains("8.5")){
				major.setIELTS_Avg("8.5");
				if(str.contains("8.0")){
					major.setIELTS_Low("8.0");
				}else if(str.contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(str.contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(str.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(str.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(str.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("8.0")){
				major.setIELTS_Avg("8.0");
				if(str.contains("7.5")){
					major.setIELTS_Low("7.5");
				}else if(str.contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(str.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(str.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(str.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("7.5")){
				major.setIELTS_Avg("7.5");
				if(str.contains("7.0")){
					major.setIELTS_Low("7.0");
				}else if(str.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(str.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(str.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("7.0")){
				major.setIELTS_Avg("7.0");
				if(str.contains("6.5")){
					major.setIELTS_Low("6.5");
				}else if(str.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(str.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("6.5")){
				major.setIELTS_Avg("6.5");
				if(str.contains("6.0")){
					major.setIELTS_Low("6.0");
				}else if(str.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("6.0")){
				major.setIELTS_Avg("6.0");
				if(str.contains("5.5")){
					major.setIELTS_Low("5.5");
				}else if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("5.5")){
				major.setIELTS_Avg("5.5");
				if(str.contains("5.0")){
					major.setIELTS_Low("5.0");
				}
			}else if(str.contains("5.0")){
				major.setIELTS_Avg("5.0");
				major.setIELTS_Low("5.0");
			}
		}
	}
	
	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}
}


