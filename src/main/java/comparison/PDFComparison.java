package comparison;

//import de.redsix.pdfcompare.PdfComparator;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class PDFComparison {

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
                String fileName = file.getName().replace("TAP_AST_B_Stmt","");
                String identifier = fileName.substring(0,10);
                System.out.println("identifier"+identifier);
                File astA = Arrays.stream(beforeFiles)
                        .filter(fs -> fs.getName().startsWith("AST_B_Stmt"+ identifier))
                        .findFirst().get();
                File secB = Arrays.stream(beforeFiles)
                        .filter(fs -> fs.getName().startsWith("SEC_B_Stmt"+ identifier))
                        .findFirst().get();

                        PDFMergerUtility ut = new PDFMergerUtility();
                ut.addSource(astA.getAbsolutePath());
                ut.addSource(secB.getAbsolutePath());
                ut.setDestinationFileName("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after/Merged_" + identifier + ".pdf");
                ut.mergeDocuments();

                new PdfComparator(file.getAbsolutePath(),
                        "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after/Merged_" + identifier + ".pdf")
                        .compare()
                        .writeTo("/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/results/Result_" + identifier + ".pdf");
            }
        }

    }

    public static File[] readTapFiles() {
        String path = "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/tap";
        File folder = new File(path);
        return folder.listFiles();

    }

    public static File[] readAfterFiles() {
        String path = "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/after";
        File folder = new File(path);
        return folder.listFiles();
    }

    public static File[] readBeforeFiles() {
        String path = "/Users/arunpandian/IdeaProjects/pdf-compare-util/src/main/resources/before";
        File folder = new File(path);
        return folder.listFiles();
    }
}
