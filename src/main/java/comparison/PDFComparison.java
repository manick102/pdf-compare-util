package comparison;

//import de.redsix.pdfcompare.PdfComparator;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import com.juliusbaer.itasia.tes.pdfcompare.CompareResult;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class PDFComparison {

    public static String T24_AST_B_PREFIX  = "AST_B_Stmt";
    public static String T24_SEC_B_PREFIX  = "SEC_B_Stmt";
    public static String T24_SECB_REM_PAGE_1_PREFIX  = "SEC_B_RemPage1";
    public static String TAP_AST_B_PREFIX  = "TAP_AST_B_Stmt";

    public static String MERGED_FILE_PREFIX  = "MERGED_";

    public static String RESULT_FILE_PREFIX = "Result_";

    public static String TAP_FILE_PATH = "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/tap";

    public static String T24_BEFORE_FILE_PATH = "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/before";

    public static String T24_AFTER_FILE_PATH = "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after";



    public static void main(String[] arggs) throws IOException {
//        new PdfComparator("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\MergedTwoFiles.pdf", "E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\C_MD72363244-68283838C.pdf").compare().writeTo("diffOutput");
//
//        PDFMergerUtility ut = new PDFMergerUtility();
//        ut.addSource("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\A_MD72363244-78283838X.pdf");
//        ut.addSource("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\B_MD72363244-88283838Y.pdf");
//        ut.setDestinationFileName("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\MergedTwoFiles.pdf");
//        ut.mergeDocuments();
        
        /* Creating excel workbook and column header */
    	XSSFWorkbook wb = new XSSFWorkbook(); 
    	XSSFSheet sheet = wb.createSheet("Sheet1"); 
    	int rownum = 0;
    	XSSFRow rowhead = sheet.createRow(rownum);  
    	rowhead.createCell(0).setCellValue("S.No.");  
    	rowhead.createCell(1).setCellValue("T24 File1");  
    	rowhead.createCell(2).setCellValue("T24 File2");  
    	rowhead.createCell(3).setCellValue("T24 Merged File");  
    	rowhead.createCell(4).setCellValue("TAP File");  
    	rowhead.createCell(5).setCellValue("Status");  
    	rowhead.createCell(6).setCellValue("Result");  
    	rownum++;
    	/* Creating excel workbook and column header */

        File[] beforeFiles = readBeforeFiles();
        for (File file : readTapFiles()) {
            if (file.getName().endsWith(".pdf")) {
                String fileName = file.getName().replace(TAP_AST_B_PREFIX,"");
                String identifier = fileName.substring(0,10);
                System.out.println("identifier"+identifier);
                File astA = Arrays.stream(beforeFiles)
                        .filter(fs -> fs.getName().startsWith(T24_AST_B_PREFIX+ identifier))
                        .findFirst().get();


                File secB = Arrays.stream(beforeFiles)
                        .filter(fs -> fs.getName().startsWith(T24_SEC_B_PREFIX+ identifier))
                        .findFirst().get();

//                PDDocument document = new PDDocument();
                PDDocument document = PDDocument.load(secB);
                document.removePage(0);
                document.save("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/before/"+ T24_SECB_REM_PAGE_1_PREFIX+ identifier + ".pdf");
                document.close();

                PDFMergerUtility ut = new PDFMergerUtility();
                ut.addSource(astA.getAbsolutePath());
                ut.addSource("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/before/"+T24_SECB_REM_PAGE_1_PREFIX + identifier + ".pdf");
                ut.setDestinationFileName("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after/"+MERGED_FILE_PREFIX + identifier + ".pdf");
                ut.mergeDocuments();

                // new PdfComparator(file.getAbsolutePath(),
                   //     "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after/"+MERGED_FILE_PREFIX + identifier + ".pdf")
                     //   .compare()
                       // .writeTo("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/results/"+ RESULT_FILE_PREFIX+ identifier + ".pdf");
                
                /* excel related changes */
                CompareResult result = new PdfComparator(file.getAbsolutePath(),
                        "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after/"+MERGED_FILE_PREFIX + identifier + ".pdf")
                        .compare();
                result.writeTo("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/results/"+ RESULT_FILE_PREFIX+ identifier );
                
                
                CreationHelper createHelper = wb.getCreationHelper();
                XSSFRow row = sheet.createRow(rownum);  
                row.createCell(0).setCellValue(""+rownum);  
                row.createCell(1).setCellValue(astA.getName());  
                row.createCell(2).setCellValue(secB.getName());  
                row.createCell(3).setCellValue(T24_SECB_REM_PAGE_1_PREFIX+ identifier);  
            	row.createCell(4).setCellValue(file.getName());  
            	row.createCell(5).setCellValue(result.isEqual() ? "PASS": "FAIL"); 
            	
            	XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(HyperlinkType.FILE);
            	 XSSFCellStyle hlinkstyle = wb.createCellStyle();
                 XSSFFont hlinkfont = wb.createFont();
                 hlinkfont.setUnderline(XSSFFont.U_SINGLE);
                 hlinkfont.setColor(IndexedColors.BLUE.index);
                 hlinkstyle.setFont(hlinkfont);
                 
            	XSSFCell cell= row.createCell(6);
            	cell.setCellValue("Result File Link");
            	link.setAddress(RESULT_FILE_PATH + RESULT_FILE_PREFIX+ identifier + ".pdf");
            	cell.setHyperlink(link);
                cell.setCellStyle(hlinkstyle);
            	
            	/* excel related changes */
            }
        }
        String fileName = "Results" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx";
        FileOutputStream out = new FileOutputStream(new File(fileName));
        wb.write(out);
        out.close();
    }

    public static File[] readTapFiles() {
        File folder = new File(TAP_FILE_PATH);
        return folder.listFiles();

    }

    public static File[] readAfterFiles() {
        File folder = new File(T24_AFTER_FILE_PATH);
        return folder.listFiles();
    }

    public static File[] readBeforeFiles() {
        File folder = new File(T24_BEFORE_FILE_PATH);
        return folder.listFiles();
    }
}
