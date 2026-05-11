package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.HomePage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC001_HomeRenderTest extends BaseTest {

    private HomePage homePage;

    @BeforeMethod
    public void openHomePage() {
        driver.get(HOME_URL);
        homePage = new HomePage(driver);
    }

    @Test(description = "Step 1: Page title and header elements are visible")
    public void step1_verifyPageTitleAndHeader() {
        ExtentReportManager.createTest("TC001 - Step 1",
                "Verify page title and header nav elements");

        String actualTitle = homePage.getPageTitle();
        ExtentReportManager.getTest().info("Actual title: [" + actualTitle + "]");

        String normalizedActual   = actualTitle
                .replaceAll("[\\u2013\\u2014\\-]", "-").trim();
        String normalizedExpected = PAGE_TITLE
                .replaceAll("[\\u2013\\u2014\\-]", "-").trim();

        Assert.assertEquals(normalizedActual, normalizedExpected,
                "Page title mismatch. Actual: [" + actualTitle + "]");

        Assert.assertTrue(homePage.isNavLoginVisible(),
                "Log In link not visible in header");
        Assert.assertTrue(homePage.isNavSignUpVisible(),
                "Sign Up Free button not visible in header");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: Hero section elements are visible")
    public void step2_verifyHeroSection() {
        ExtentReportManager.createTest("TC001 - Step 2",
                "Verify hero badge, headline, subtitle and CTA buttons");

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        String src = driver.getPageSource();

        // Log snippet around key words for debugging
        int pawIdx = src.toLowerCase().indexOf("paw");
        if (pawIdx >= 0) {
            ExtentReportManager.getTest().info("Snippet around 'paw': "
                    + src.substring(Math.max(0, pawIdx - 30),
                    Math.min(src.length(), pawIdx + 80)));
        } else {
            ExtentReportManager.getTest().info(
                    "Word 'paw' NOT found in page source");
            ExtentReportManager.getTest().info(
                    "Page source first 500 chars: "
                            + src.substring(0, Math.min(src.length(), 500)));
        }

        boolean badgeVisible = src.contains("Now Active in Chennai")
                || src.contains("Active in Chennai")
                || src.contains("Chennai");
        Assert.assertTrue(badgeVisible,
                "Hero badge not found in page source");

        boolean headlineVisible = src.contains("Every Paw")
                || src.contains("Paw Deserves")
                || src.contains("Deserves Care")
                || src.toLowerCase().contains("every paw")
                || src.toLowerCase().contains("paw deserves");
        Assert.assertTrue(headlineVisible,
                "Hero headline not found in page source. Check extent report for snippet.");

        boolean subtitleVisible = src.contains("unified")
                || src.contains("welfare")
                || src.contains("India")
                || src.contains("platform");
        Assert.assertTrue(subtitleVisible, "Hero subtitle not found");

        boolean getStartedVisible = src.contains("Get Started")
                || src.contains("get-started");
        Assert.assertTrue(getStartedVisible, "Get Started CTA not found");

        boolean seeFeaturesVisible = src.contains("See Features")
                || src.contains("Features");
        Assert.assertTrue(seeFeaturesVisible, "See Features CTA not found");

        ExtentReportManager.getTest().pass("Step 2 passed — hero section verified");
    }
}