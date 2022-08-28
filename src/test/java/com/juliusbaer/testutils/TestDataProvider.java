package com.juliusbaer.testutils;

import constants.FilePrefix;
import constants.FolderProperty;
import com.juliusbaer.entity.PdfComparisonEntity;
import org.testng.annotations.DataProvider;
import utility.FileUtil;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestDataProvider {




    @DataProvider(name = "comparableTapFiles", parallel = true)
    public static Object[][] getData() {
        AtomicInteger index = new AtomicInteger(1);
        String modifiedSmrFileFolder = FileUtil.createFolder(System.getProperty(FolderProperty.BEFORE_SMR_FILE),"modified");
        String resultFileFolder = FileUtil.createFolder(System.getProperty(FolderProperty.RESULT_FILE));
        File[] tapFiles = readFiles(System.getProperty(FolderProperty.TAP_FILE));
        File[] assetFiles = readFiles(System.getProperty(FolderProperty.BEFORE_ASSET_FILE));
        File[] smrFiles = readFiles(System.getProperty(FolderProperty.BEFORE_SMR_FILE));


        return Arrays.stream(tapFiles).filter(val -> val.getName().endsWith(".pdf")).map(file -> {
            String identifier = file.getName().substring(0, 10);
            String removedFirstPageSmrFilePath = FileUtil
                            .getFilePath(modifiedSmrFileFolder
                            , FilePrefix.T24_SECB_REM_PAGE_1_PREFIX + "_" + identifier+".pdf");
            String mergedFilePath = FileUtil
                            .getFilePath(modifiedSmrFileFolder
                            , FilePrefix.MERGED_FILE_PREFIX + "_" + identifier+".pdf");
            File assetFile = Arrays.stream(assetFiles).filter(fs -> fs.getName().startsWith(identifier)).findFirst().get();
            File smrFile = Arrays.stream(smrFiles).filter(fs -> fs.getName().startsWith(identifier)).findFirst().get();
            String resultFilePath = FileUtil.getFilePath(resultFileFolder,FilePrefix.RESULT_FILE_PREFIX + identifier);

            PdfComparisonEntity entity = PdfComparisonEntity.builder()
                    .tapFilePath(file.getAbsolutePath()).assetFilePath(assetFile.getAbsolutePath())
                    .smrFilePath(smrFile != null ? smrFile.getAbsolutePath() : null).index(index.getAndIncrement())
                    .identifier(identifier).removedFirstPageSmrFilePath(removedFirstPageSmrFilePath)
                    .mergedFilePath(mergedFilePath).resultFilePath(resultFilePath).build();

            return new Object[]{entity};
        }).collect(Collectors.toList()).toArray(new Object[0][0]);
    }

    private static File[] readFiles(String path) {
        File folder = new File(path);
        return folder.listFiles();
    }
}
