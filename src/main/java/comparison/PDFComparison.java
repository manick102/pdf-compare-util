package comparison;

//import de.redsix.pdfcompare.PdfComparator;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

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

                new PdfComparator(file.getAbsolutePath(),
                        "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after/"+MERGED_FILE_PREFIX + identifier + ".pdf")
                        .compare()
                        .writeTo("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/results/"+ RESULT_FILE_PREFIX+ identifier + ".pdf");
            }
        }

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
