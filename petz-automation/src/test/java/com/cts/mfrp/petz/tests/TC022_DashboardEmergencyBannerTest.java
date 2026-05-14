package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.DashboardPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

public class TC022_DashboardEmergencyBannerTest extends BaseTest {

    @Test(description =
            "PETZ_TC022 - Validate red 'Animal in Distress?' banner on /dashboard")
    public void verifyEmergencyBanner() {
        ExtentReportManager.createTest(
                "PETZ_TC022_DashboardEmergencyBanner",
                "Validate the red 'Animal in Distress?' banner and 'Report Now' navigation.");

        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.isEmergencyBannerVisible(),
                "Emergency banner 'Animal in Distress?' is not visible.");

        Assert.assertTrue(dashboard.isEmergencyCopyVisible(),
                "Banner copy ('Report it immediately - every second counts') is not visible.");

        Assert.assertTrue(dashboard.isReportNowButtonVisible(),
                "'Report Now ->' button is not visible on the banner.");

        dashboard.clickReportNow();

        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT))
                .until(ExpectedConditions.urlContains("/rescue"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/rescue/report")
                        || driver.getCurrentUrl().contains("/rescue"),
                "After clicking 'Report Now ->' the URL is not /rescue/report. Actual: "
                        + driver.getCurrentUrl());

        Assert.assertTrue(driver.getPageSource().contains("Report Animal in Need")
                        || driver.getPageSource().contains("Report Animal"),
                "'Report Animal in Need' form is not shown after clicking 'Report Now ->'.");
    }
}
