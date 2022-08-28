package utility;

import com.juliusbaer.itasia.tes.pdfcompare.CompareResult;
import constants.FilePrefix;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelUtil {


    private static ExcelUtil excelUtil;

    private ExcelUtil(){};

    public static ExcelUtil getInstance() {
        if (excelUtil == null) {
            excelUtil = new ExcelUtil();
        }
        return excelUtil;
    }
    private static XSSFWorkbook wb;
    private static XSSFSheet sheet;

    public void createExcelSheetWithHeaders() {
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

    public void postDataToExcelFile() {
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

    public void updateExcelSheetWithResult(String assetFile, String  smrFile, String tapFile, String identifier, CompareResult result, int rownum, String differences, String resultFilePath) {
        CreationHelper createHelper = wb.getCreationHelper();
        XSSFRow row = sheet.createRow(rownum);
        row.createCell(0).setCellValue("" + rownum);
        row.createCell(1).setCellValue(new File(assetFile).getName());
        row.createCell(2).setCellValue(new File(smrFile).getName());
        row.createCell(3).setCellValue(new File(tapFile).getName());
        row.createCell(4).setCellValue(result.isEqual() ? "PASS" : "FAIL");

        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.FILE);
        XSSFCellStyle hlinkstyle = wb.createCellStyle();
        XSSFFont hlinkfont = wb.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(IndexedColors.BLUE.index);
        hlinkstyle.setFont(hlinkfont);

        XSSFCell cell = row.createCell(5);
        cell.setCellValue("Result File Link");
        String val = resultFilePath+File.separator + FilePrefix.RESULT_FILE_PREFIX + identifier + ".pdf";

        link.setAddress(String.join("/", val.split("\\\\")));
        cell.setHyperlink(link);
        cell.setCellStyle(hlinkstyle);

        row.createCell(6).setCellValue(result.isEqual() ? "" : result.getPagesWithDifferences().toString());
        System.out.println(differences);
        row.createCell(7).setCellValue(StringUtils.substring(differences, 0, 32767));
    }

}
