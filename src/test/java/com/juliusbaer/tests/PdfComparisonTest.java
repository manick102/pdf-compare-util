package com.juliusbaer.tests;

import com.juliusbaer.entity.PdfComparisonEntity;
import com.juliusbaer.itasia.tes.pdfcompare.CompareResult;
import utility.PdfUtils;
import com.juliusbaer.testutils.TestDataProvider;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import utility.ExcelUtil;

public class PdfComparisonTest {

	@BeforeSuite
	public void suiteSetup() {
		System.out.println(System.getProperty("beforeAssetFolderPath"));
		ExcelUtil.getInstance().createExcelSheetWithHeaders();

	}

	@AfterSuite
	public void finishUpResults() {
		ExcelUtil.getInstance().postDataToExcelFile();
	}

	@Test(dataProviderClass = TestDataProvider.class, dataProvider = "comparableTapFiles")
	public void comparePdfsTest(PdfComparisonEntity pdfComparisonEntity) {

		PdfUtils.removeFirstPageAndSave(pdfComparisonEntity.getSmrFilePath(),
				pdfComparisonEntity.getRemovedFirstPageSmrFilePath());

		PdfUtils.mergeAstAndSecFiles(pdfComparisonEntity.getAssetFilePath(),
				pdfComparisonEntity.getRemovedFirstPageSmrFilePath(), pdfComparisonEntity.getMergedFilePath());

		CompareResult result = PdfUtils.getComparedResult(pdfComparisonEntity.getTapFilePath(),
				pdfComparisonEntity.getMergedFilePath(), pdfComparisonEntity.getResultFilePath());
		String differences = PdfUtils.getAllDifferences(pdfComparisonEntity.getTapFilePath(),
				pdfComparisonEntity.getMergedFilePath(), result);
		ExcelUtil.getInstance().updateExcelSheetWithResult(pdfComparisonEntity.getAssetFilePath(),
				pdfComparisonEntity.getSmrFilePath(), pdfComparisonEntity.getTapFilePath(),
				pdfComparisonEntity.getIdentifier(), result, pdfComparisonEntity.getIndex(), differences,
				pdfComparisonEntity.getResultFilePath());

	}

}
