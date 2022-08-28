package utility;

import java.io.File;

public class FileUtil {

    public static String createFolder(String path) {
        File file = new File(path+ File.separator);
        if (!file.exists()){
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static String createFolder(String path, String folderName) {
        File file = new File(path+ File.separator + folderName);
        if (!file.exists()){
            file.mkdir();
        }
        return file.getAbsolutePath();
    }



    public static String getFilePath(String folderPath,String fileName) {
         return new File(folderPath +File.separator+fileName).getAbsolutePath();
    }

}
