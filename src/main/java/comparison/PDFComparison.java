package comparison;

//import de.redsix.pdfcompare.PdfComparator;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import com.juliusbaer.itasia.tes.pdfcompare.PdfComparator;

import java.io.IOException;

public class PDFComparison {

    public static void main(String[] arggs) throws IOException {
        new PdfComparator("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\MergedTwoFiles.pdf", "E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\C_MD72363244-68283838C.pdf").compare().writeTo("diffOutput");

        PDFMergerUtility ut = new PDFMergerUtility();
        ut.addSource("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\A_MD72363244-78283838X.pdf");
        ut.addSource("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\B_MD72363244-88283838Y.pdf");
        ut.setDestinationFileName("E:\\automation\\pdfcompare-master\\src\\main\\java\\comparison\\MergedTwoFiles.pdf");
        ut.mergeDocuments();
    }
}
