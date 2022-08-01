package comparison;

import com.juliusbaer.itasia.tes.pdfcompare.CompareResult;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;
import org.apache.commons.lang3.StringUtils;
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
import utility.PDFUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    private static File[] beforeAssetFiles = readBeforeAssetFiles();
    private static File[] beforeSMRFiles = readBeforeSMRFiles();

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

        File tapFile = Arrays.stream(tapFiles).filter(fs -> fs.getName().startsWith(identifier)).findFirst().get();

        File assetFile = Arrays.stream(beforeAssetFiles).filter(fs -> fs.getName().startsWith(identifier)).findFirst().get();

        File smrFile = Arrays.stream(beforeSMRFiles).filter(fs -> fs.getName().startsWith(identifier)).findFirst().get();


        String mergingFilePathOne = assetFile.getAbsolutePath();
        String mergingFilePathTwo = getRequiredBeforeSMRFilePath(T24_SECB_REM_PAGE_1_PREFIX + identifier + ".pdf");
        String destinationFilePath = getRequiredAfterFilePath(MERGED_FILE_PREFIX + identifier + ".pdf");
        String resultFilePath = RESULT_FILE_PATH + RESULT_FILE_PREFIX + identifier;

        removeFirstPageAndSave(smrFile,mergingFilePathTwo, identifier);
        mergeAstAndSecFiles(mergingFilePathOne,mergingFilePathTwo, destinationFilePath);
        CompareResult result = getComparedResult(tapFile.getAbsolutePath(), destinationFilePath, resultFilePath);
        String differences = getAllDifferences(tapFile.getAbsolutePath(), destinationFilePath, result);
        updateExcelSheetWithResult(assetFile, smrFile, tapFile, identifier, result, rownum, differences);

    }

    @DataProvider(name = "comparableTapFiles", parallel = true)
    public Object[][] getComparableTapFilesIdentifiers() {
        AtomicInteger index = new AtomicInteger(1);
        return Arrays.stream(readTapFiles()).filter(val -> val.getName().endsWith(".pdf")).map(file -> {
            Map<String, String> map = new HashMap<>();
            map.put("identifier", file.getName().substring(0, 10));
            map.put("index", "" + index.getAndIncrement());
            return new Object[]{map};
        }).collect(Collectors.toList()).toArray(new Object[0][0]);
    }


    private static void updateExcelSheetWithResult(File assetFile, File smrFile, File tapFile, String identifier, CompareResult result, int rownum, String differences) {
        CreationHelper createHelper = wb.getCreationHelper();
        XSSFRow row = sheet.createRow(rownum);
        row.createCell(0).setCellValue("" + rownum);
        row.createCell(1).setCellValue(assetFile.getName());
        row.createCell(2).setCellValue(smrFile.getName());
        row.createCell(3).setCellValue(tapFile.getName());
        row.createCell(4).setCellValue(result.isEqual() ? "PASS" : "FAIL");

        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.FILE);
        XSSFCellStyle hlinkstyle = wb.createCellStyle();
        XSSFFont hlinkfont = wb.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(IndexedColors.BLUE.index);
        hlinkstyle.setFont(hlinkfont);

        XSSFCell cell = row.createCell(5);
        cell.setCellValue("Result File Link");
        String val = RESULT_FILE_PATH + RESULT_FILE_PREFIX + identifier + ".pdf";

        link.setAddress(String.join("/",val.split("\\\\")));
        cell.setHyperlink(link);
        cell.setCellStyle(hlinkstyle);

        row.createCell(6).setCellValue(result.isEqual() ? "" : result.getPagesWithDifferences().toString());
        row.createCell(7).setCellValue(differences);
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
        rowhead.createCell(7).setCellValue("Differences");
    }

    private static void removeFirstPageAndSave(File secB,String path, String identifier) {
        PDDocument document = null;
        try {
            document = PDDocument.load(secB);
            document.removePage(0);
            document.save(path);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mergeAstAndSecFiles(String assetFilePath, String smrFilePath, String destinationPath) {
        PDFMergerUtility ut = new PDFMergerUtility();
        try {
            ut.addSource(assetFilePath);
            ut.addSource(smrFilePath);
            ut.setDestinationFileName(destinationPath);
            ut.mergeDocuments();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CompareResult getComparedResult(String tapFilePath, String mergedFilePath, String resultFilePath ) {
        CompareResult result = null;
        try {
            result = new PdfComparator(tapFilePath,mergedFilePath ).withIgnore(EXCLUSIONS_CONF_FILE_PATH).compare();
            result.writeTo(resultFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static String getAllDifferences(String tapFilePath, String mergedFilePath, CompareResult result) {
        AtomicReference<ArrayList<String>> list = new AtomicReference<ArrayList<String>>();
        PDFUtil pdfUtil = new PDFUtil();

        result.getPagesWithDifferences().forEach(pageNum -> {
            try {
                String[] str1 = pdfUtil.getText(mergedFilePath, pageNum, pageNum).split(" ");
                String[] str2 = pdfUtil.getText(tapFilePath, pageNum, pageNum).split(" ");

                updateStringDiff(str1, str2).forEach(strVal -> {
                    list.get().add(strVal);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        return list.toString();
    }

    private static List<String> updateStringDiff(String[] str1, String[] str2) {
        int maxLength = str1.length > str2.length ? str1.length : str2.length;
        List<String> list = new ArrayList<>();
        int startIndex = 0;
        int startSecondIndex = 0;
        for (int i = 1; i < maxLength; i++) {
            String val1 = getStringVal(str1, startIndex, i);
            String val2 = getStringVal(str2, startSecondIndex, i);

            if (!val1.equals(val2)) {
                String diff = StringUtils.difference(val1, val2);
                if (!diff.isEmpty()) {
                    list.add(diff);
                }
                startIndex = i + 1;
                startSecondIndex = i + 1;
            }

        }
        return list;
    }

    private static String getStringVal(String[] strArr, int startIndex, int endIndex) {
        String value = "";
        for (int i = startIndex; i <= endIndex; i++) {
            String concat = strArr.length <= i ? "" : strArr[i];
            value = value + concat;
        }
        return value;
    }


    private static File[] readTapFiles() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "tap");
        return folder.listFiles();

    }

    private static File[] readBeforeAssetFiles() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "beforeAsset");
        return folder.listFiles();
    }
    private static File[] readBeforeSMRFiles() {
        File folder = new File(System.getProperty("user.dir") + File.separator + "beforeSMR");
        return folder.listFiles();
    }

    private static String getRequiredBeforeSMRFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "beforeSMR" + File.separator + fileName;
    }

    private static String getRequiredAfterFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "after" + File.separator + fileName;
    }
}
