package utility;

import java.io.File;

public class RequiredPaths {

    public  static String getRequiredBeforeSMRFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "beforeSMR" + File.separator + fileName;
    }

    public static String getRequiredAfterFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "after" + File.separator + fileName;
    }
}
