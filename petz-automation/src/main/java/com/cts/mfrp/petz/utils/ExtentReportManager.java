package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

import static com.cts.mfrp.petz.constants.AppConstants.REPORT_PATH;

public class ExtentReportManager {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static void initReport() {
        new File("test-output/reports").mkdirs();
        new File("test-output/screenshots").mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("PETZ Automation Report");
        spark.config().setReportName("TC001 - TC005 Landing Page Tests");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Project",     "PETZ Animal Welfare Platform");
        extent.setSystemInfo("Environment", "https://stellular-taffy-e3ee7a.netlify.app");
        extent.setSystemInfo("Tester",      "Automation QA");
        extent.setSystemInfo("Browser",     "Chrome");
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = extent.createTest(testName, description);
        test.set(extentTest);
        return extentTest;
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}