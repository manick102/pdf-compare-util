package utility;

import com.juliusbaer.itasia.tes.pdfcompare.CompareResult;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;
import constants.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PdfUtils {

    public static void removeFirstPageAndSave(String filePath, String savePath) {
        PDDocument document = null;
        try {
            document = PDDocument.load(new File(filePath));
            document.removePage(0);
            document.save(savePath);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mergeAstAndSecFiles(String assetFilePath, String smrFilePath, String destinationPath) {
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

    public static CompareResult getComparedResult(String tapFilePath, String mergedFilePath, String resultFilePath) {
        CompareResult result = null;
        try {
            result = new PdfComparator(tapFilePath, mergedFilePath).withIgnore(Config.EXCLUSIONS_CONF_FILE_PATH).compare();
            result.writeTo(resultFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static String getAllDifferences(String tapFilePath, String mergedFilePath, CompareResult result) {
        ArrayList<String> list = new ArrayList<String>();
        PDFUtil pdfUtil = new PDFUtil();
        List<Integer> pages = (List<Integer>) result.getPagesWithDifferences();
        if (pages.size() > 0) {
            Iterator itr = pages.iterator();
            while (itr.hasNext()) {
                int pageNum = (int) itr.next();
                try {
                    String[] str1 = pdfUtil.getText(mergedFilePath, pageNum, pageNum).split(" ");
                    String[] str2 = pdfUtil.getText(tapFilePath, pageNum, pageNum).split(" ");
                    List<String> diff = updateStringDiff(str1, str2);
                    if (diff.size() > 0) {
                        list.addAll(diff);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return list != null && list.size() > 0 ?  list.toString(): "";
    }

    public static List<String> updateStringDiff(String[] str1, String[] str2) {
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

    public static String getStringVal(String[] strArr, int startIndex, int endIndex) {
        String value = "";
        for (int i = startIndex; i <= endIndex; i++) {
            String concat = strArr.length <= i ? "" : strArr[i];
            value = value + concat;
        }
        return value;
    }
}
