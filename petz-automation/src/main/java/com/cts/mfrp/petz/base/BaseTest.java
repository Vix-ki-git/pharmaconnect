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

    @BeforeSuite
    public void initReport() {
        ExtentReportManager.initReport();
    }

    @BeforeMethod
    public void setUp() {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            String screenshotPath = ScreenshotUtil
                    .takeScreenshot(driver, result.getName());
            try {
                ExtentReportManager.getTest()
                        .addScreenCaptureFromPath(screenshotPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ExtentReportManager.getTest().fail(result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            ExtentReportManager.getTest().pass("Test passed successfully");
        } else {
            ExtentReportManager.getTest().skip("Test skipped");
        }
        DriverFactory.quitDriver();
    }

    @AfterSuite
    public void flushReport() {
        ExtentReportManager.flushReport();
    }
}