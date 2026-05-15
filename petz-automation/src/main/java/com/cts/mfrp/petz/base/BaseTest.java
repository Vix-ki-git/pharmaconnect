package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.utils.DriverFactory;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import com.cts.mfrp.petz.utils.ScreenshotUtil;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    protected WebDriver driver;

    @BeforeSuite(alwaysRun = true)
    public void initReport() {
        ExtentReportManager.initReport();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        var extentTest = ExtentReportManager.getTest();
        if (result.getStatus() == ITestResult.FAILURE) {
            String screenshotPath = ScreenshotUtil
                    .takeScreenshot(driver, result.getName());
            if (extentTest != null) {
                try {
                    extentTest.addScreenCaptureFromPath(screenshotPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                extentTest.fail(result.getThrowable());
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            if (extentTest != null) extentTest.pass("Test passed successfully");
        } else {
            if (extentTest != null) extentTest.skip("Test skipped");
        }
        DriverFactory.quitDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void flushReport() {
        ExtentReportManager.flushReport();
    }
}