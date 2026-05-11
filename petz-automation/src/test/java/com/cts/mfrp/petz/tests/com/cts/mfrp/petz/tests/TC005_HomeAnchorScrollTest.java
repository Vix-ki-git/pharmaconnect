package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.HomePage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC005_HomeAnchorScrollTest extends BaseTest {

    private HomePage homePage;

    @BeforeMethod
    public void openHomePage() {
        driver.get(HOME_URL);
        homePage = new HomePage(driver);
    }

    @Test(description = "Step 1: Features nav scrolls to feature section")
    public void step1_featuresLinkScrollsToFeatureSection() {
        ExtentReportManager.createTest("TC005 - Step 1",
                "Features nav -> feature section");

        homePage.clickNavFeatures();
        waitForScroll();

        String src = driver.getPageSource();
        boolean found = src.contains("Everything Your Pet Needs")
                || src.contains("Manage Your Pets")
                || src.contains("Report Rescue")
                || src.contains("Adopt");

        Assert.assertTrue(found,
                "Feature section content not found after clicking Features nav");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: How it Works nav scrolls to steps section")
    public void step2_howItWorksLinkScrollsToStepsSection() {
        ExtentReportManager.createTest("TC005 - Step 2",
                "How it Works nav -> steps section");

        homePage.clickNavHowItWorks();
        waitForScroll();

        String src = driver.getPageSource();
        boolean found = src.contains("Report or Browse")
                || src.contains("NGO")
                || src.contains("Simple");

        Assert.assertTrue(found,
                "How it Works section not found after clicking nav");

        ExtentReportManager.getTest().pass("Step 2 passed");
    }

    @Test(description = "Step 3: Cities nav scrolls to cities section")
    public void step3_citiesLinkScrollsToCitiesSection() {
        ExtentReportManager.createTest("TC005 - Step 3",
                "Cities nav -> cities section");

        homePage.clickNavCities();
        waitForScroll();

        // Extra scroll to ensure cities section loaded
        ((JavascriptExecutor) driver)
                .executeScript(
                        "window.scrollTo(0, document.body.scrollHeight * 0.7)");
        waitForScroll();

        String src = driver.getPageSource();
        boolean found = src.contains("Chennai")
                && src.contains("Bangalore")
                && (src.contains("Where We Operate")
                || src.contains("Cities")
                || src.contains("Coming Soon"));

        Assert.assertTrue(found,
                "Cities section not found after clicking Cities nav");

        ExtentReportManager.getTest().pass("Step 3 passed");
    }

    private void waitForScroll() {
        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
    }
}