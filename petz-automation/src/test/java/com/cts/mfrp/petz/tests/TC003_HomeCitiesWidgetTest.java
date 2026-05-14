package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.HomePage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC003_HomeCitiesWidgetTest extends BaseTest {

    private HomePage homePage;

    @BeforeMethod
    public void openHomePage() {
        driver.get(HOME_URL);
        homePage = new HomePage(driver);
    }

    @Test(description = "Step 1: All 4 cities visible — Chennai Live, others Coming Soon")
    public void step1_verifyCitiesWidget() {
        ExtentReportManager.createTest("TC003 - Step 1",
                "Verify Chennai=Live, Bangalore/Mumbai/Delhi=Coming Soon");

        Assert.assertTrue(homePage.areCitiesVisible(),
                "One or more cities not visible");

        Assert.assertTrue(homePage.isChennaiLive(),
                "Chennai should show Live or Active indicator");

        Assert.assertTrue(homePage.areComingSoonCitiesVisible(),
                "Bangalore/Mumbai/Delhi should show Coming Soon");

        ExtentReportManager.getTest().pass("Step 1 passed — cities widget verified");
    }
}