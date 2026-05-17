package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.api.swagger.RecordingFilter;
import com.cts.mfrp.petz.api.swagger.SwaggerReport;
import com.cts.mfrp.petz.api.swagger.TestContext;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static com.cts.mfrp.petz.constants.AppConstants.API_BASE_URL;

public class BaseApiTest {

    @BeforeSuite(alwaysRun = true)
    public void initApiSuite() {
        RestAssured.baseURI = API_BASE_URL;
        com.cts.mfrp.petz.api.specs.ApiSpecs.configureJacksonOnce();
        RestAssured.filters(new RecordingFilter());
        ExtentReportManager.initReport();
    }

    @BeforeMethod(alwaysRun = true)
    public void tagTestName(Method method) {
        TestContext.set(getClass().getSimpleName() + "." + method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void recordApiResult(ITestResult result) {
        if (ExtentReportManager.getTest() != null) {
            switch (result.getStatus()) {
                case ITestResult.SUCCESS -> ExtentReportManager.getTest().pass("API test passed");
                case ITestResult.FAILURE -> ExtentReportManager.getTest().fail(result.getThrowable());
                case ITestResult.SKIP    -> ExtentReportManager.getTest().skip("API test skipped");
                default                  -> { /* no-op */ }
            }
        }
        TestContext.clear();
    }

    @AfterSuite(alwaysRun = true)
    public void flushApiReport() {
        ExtentReportManager.flushReport();
        SwaggerReport.write(
                RecordingFilter.captures(),
                API_BASE_URL,
                Path.of("test-output", "swagger"),
                /* openInBrowser = */ true
        );
    }
}
