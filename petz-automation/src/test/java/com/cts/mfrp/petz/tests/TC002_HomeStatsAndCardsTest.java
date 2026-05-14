package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.HomePage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC002_HomeStatsAndCardsTest extends BaseTest {

    private HomePage homePage;

    @BeforeMethod
    public void openHomePage() {
        driver.get(HOME_URL);
        homePage = new HomePage(driver);
    }

    @Test(description = "Step 1: Stats strip shows all 4 stats")
    public void step1_verifyStatsStrip() {
        ExtentReportManager.createTest("TC002 - Step 1",
                "Verify stats: 2400+ rescued, 120+ clinics, 1 city, 98%");

        Assert.assertTrue(homePage.areStatsVisible(),
                "One or more stats not visible");

        ExtentReportManager.getTest().pass("Step 1 passed — stats verified");
    }

    @Test(description = "Step 2: Four feature cards visible")
    public void step2_verifyFeatureCards() {
        ExtentReportManager.createTest("TC002 - Step 2",
                "Verify 4 feature cards visible");

        Assert.assertTrue(homePage.areFeatureCardsVisible(),
                "One or more feature cards not visible");

        ExtentReportManager.getTest().pass("Step 2 passed — feature cards verified");
    }

    @Test(description = "Step 3: Three How it Works steps visible")
    public void step3_verifyHowItWorksSteps() {
        ExtentReportManager.createTest("TC002 - Step 3",
                "Verify 3 steps: Report or Browse / NGOs Respond / Animals Thrive");

        Assert.assertTrue(homePage.areHowItWorksStepsVisible(),
                "One or more How it Works steps not visible");

        ExtentReportManager.getTest().pass("Step 3 passed — steps verified");
    }
}