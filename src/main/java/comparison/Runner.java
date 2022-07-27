package comparison;

import com.juliusbaer.itasia.tes.pdfcompare.CompareResult;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The type Runner.
 */
public class Runner {

    private static String EXCLUSIONS_CONF_FILE_PATH = System.getProperty("user.dir") + File.separator + "exclusions.conf";
    private static String T24_SECB_REM_PAGE_1_PREFIX = "SEC_B_RemPage1";

    private static String MERGED_FILE_PREFIX = "MERGED_";

    private static String RESULT_FILE_PREFIX = "Result_";

    private static String RESULT_FILE_PATH = System.getProperty("user.dir") + File.separator + "results" + File.separator;

    private static XSSFWorkbook wb;
    private static XSSFSheet sheet;
    private static File[] beforeFiles = readBeforeFiles();

    private static File[] tapFiles = readTapFiles();

    @BeforeSuite
    public void setupExcelFile() {
        createExcelSheetWithHeaders();
    }

    @AfterSuite
    public void finishUpResults() {
        postDataToExcelFile();
    }

    private static void postDataToExcelFile() {
        String fileName = "Results" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(fileName));
            wb.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(dataProvider = "comparableTapFiles", threadPoolSize = 2)
    public void run(Map<String, String> map) {

        String identifier = map.get("identifier");
        int rownum = Integer.parseInt(map.get("index"));

        System.out.println(identifier);
        System.out.println(rownum);

        File file = Arrays.stream(tapFiles).filter(fs -> fs.getName().startsWith(identifier)).findFirst().get();

        File astA = Arrays.stream(beforeFiles)
                .filter(fs -> fs.getName().startsWith(identifier) && fs.getName().endsWith("Y.pdf"))
                .findFirst().get();

        File secB = Arrays.stream(beforeFiles)
                .filter(fs -> fs.getName().startsWith(identifier) && !fs.getName().endsWith("Y.pdf"))
                .findFirst().get();

        removeFirstPageAndSave(secB, identifier);
        mergeAstAndSecFiles(astA,identifier);
        CompareResult result = getComparedResult(file,identifier);
        updateExcelSheetWithResult(astA,secB,file,identifier,result,rownum);

    }

    @DataProvider(name = "comparableTapFiles" , parallel = true)
    public Object[][] getComparableTapFilesIdentifiers() {
        AtomicInteger index = new AtomicInteger(1);
        return Arrays.stream(readTapFiles()).filter(val -> val.getName().endsWith(".pdf")).map(file -> {
            Map<String, String> map = new HashMap<>();
            map.put("identifier", file.getName().substring(0, 10));
            map.put("index", "" + index.getAndIncrement());
            return new Object[]{map};
        }).collect(Collectors.toList()).toArray(new Object[0][0]);
    }


    private static void updateExcelSheetWithResult(File astA, File secB, File file, String identifier, CompareResult result, int rownum) {
        CreationHelper createHelper = wb.getCreationHelper();
        XSSFRow row = sheet.createRow(rownum);
        row.createCell(0).setCellValue("" + rownum);
        row.createCell(1).setCellValue(astA.getName());
        row.createCell(2).setCellValue(secB.getName());
        row.createCell(3).setCellValue(T24_SECB_REM_PAGE_1_PREFIX + identifier);
        row.createCell(4).setCellValue(file.getName());
        row.createCell(6).setCellValue(result.isEqual() ? "" : result.getPagesWithDifferences().toString());

        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.FILE);
        row.createCell(5).setCellValue(result.isEqual() ? "PASS" : "FAIL");
        XSSFCellStyle hlinkstyle = wb.createCellStyle();
        XSSFFont hlinkfont = wb.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(IndexedColors.BLUE.index);
        hlinkstyle.setFont(hlinkfont);

        XSSFCell cell = row.createCell(6);
        cell.setCellValue("Result File Link");
        link.setAddress(RESULT_FILE_PATH + RESULT_FILE_PREFIX + identifier + ".pdf");
        cell.setHyperlink(link);
        cell.setCellStyle(hlinkstyle);
    }

    private static void createExcelSheetWithHeaders() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet("Sheet1");
        XSSFRow rowhead = sheet.createRow(0);
        rowhead.createCell(0).setCellValue("S.No.");
        rowhead.createCell(1).setCellValue("T24 File1");
        rowhead.createCell(2).setCellValue("T24 File2");
        rowhead.createCell(3).setCellValue("TAP File");
        rowhead.createCell(4).setCellValue("Status");
        rowhead.createCell(5).setCellValue("Result");
        rowhead.createCell(6).setCellValue("ErrorPages");
    }

    private static void removeFirstPageAndSave(File secB, String identifier) {
        PDDocument document = null;
        try {
            document = PDDocument.load(secB);
            document.removePage(0);
            document.save(getRequiredBeforeFilePath(T24_SECB_REM_PAGE_1_PREFIX + identifier + ".pdf"));
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mergeAstAndSecFiles(File astA, String identifier) {
        PDFMergerUtility ut = new PDFMergerUtility();
        try {
            ut.addSource(astA.getAbsolutePath());
            ut.addSource(getRequiredBeforeFilePath(T24_SECB_REM_PAGE_1_PREFIX + identifier + ".pdf"));
            ut.setDestinationFileName(getRequiredAfterFilePath(MERGED_FILE_PREFIX + identifier + ".pdf"));
            ut.mergeDocuments();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CompareResult getComparedResult(File file, String identifier) {
        CompareResult result = null;
        try {
            result = new PdfComparator(file.getAbsolutePath(),
                    getRequiredAfterFilePath(MERGED_FILE_PREFIX + identifier + ".pdf")).withIgnore(EXCLUSIONS_CONF_FILE_PATH)
                    .compare();
            result.writeTo(RESULT_FILE_PATH + RESULT_FILE_PREFIX + identifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    private static File[] readTapFiles() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "tap");
        return folder.listFiles();

    }

    private static File[] readBeforeFiles() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "before");
        return folder.listFiles();
    }

    private static String getRequiredBeforeFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "before" + File.separator + fileName;
    }

    private static String getRequiredAfterFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "after" + File.separator + fileName;
    }
}
