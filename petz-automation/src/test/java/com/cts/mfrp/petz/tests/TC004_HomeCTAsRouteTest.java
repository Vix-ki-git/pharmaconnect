package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.HomePage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC004_HomeCTAsRouteTest extends BaseTest {

    private HomePage homePage;
    private WebDriverWait wait;

    @BeforeMethod
    public void openHomePage() {
        driver.get(HOME_URL);
        homePage = new HomePage(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(description = "Step 1: Sign Up Free navigates to /auth/register")
    public void step1_signUpFreeNavigatesToRegister() {
        ExtentReportManager.createTest("TC004 - Step 1",
                "Sign Up Free -> /auth/register");

        homePage.clickNavSignUp();
        waitForNavigation();

        String url = homePage.getCurrentUrl();
        ExtentReportManager.getTest().info("Navigated to: " + url);

        Assert.assertTrue(
                url.contains("/auth/register") || url.contains("register"),
                "Expected /auth/register but got: " + url);

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: Log In navigates to /auth/login")
    public void step2_logInNavigatesToLogin() {
        ExtentReportManager.createTest("TC004 - Step 2",
                "Log In -> /auth/login");

        homePage.clickNavLogin();
        waitForNavigation();

        String url = homePage.getCurrentUrl();
        ExtentReportManager.getTest().info("Navigated to: " + url);

        Assert.assertTrue(
                url.contains("/auth/login") || url.contains("login"),
                "Expected /auth/login but got: " + url);

        ExtentReportManager.getTest().pass("Step 2 passed");
    }

    @Test(description = "Step 3: Footer Get Started navigates to /auth/register")
    public void step3_footerGetStartedNavigatesToRegister() {
        ExtentReportManager.createTest("TC004 - Step 3",
                "Footer Get Started -> /auth/register");

        homePage.clickFooterGetStarted();
        waitForNavigation();

        String url = homePage.getCurrentUrl();
        ExtentReportManager.getTest().info("Navigated to: " + url);

        Assert.assertTrue(
                url.contains("/auth/register") || url.contains("register"),
                "Expected /auth/register but got: " + url);

        ExtentReportManager.getTest().pass("Step 3 passed");
    }

    @Test(description = "Step 4: Footer Already have account navigates to /auth/login")
    public void step4_footerAlreadyHaveAccountNavigatesToLogin() {
        ExtentReportManager.createTest("TC004 - Step 4",
                "Footer Already have account -> /auth/login");

        homePage.clickFooterHaveAccount();
        waitForNavigation();

        String url = homePage.getCurrentUrl();
        ExtentReportManager.getTest().info("Navigated to: " + url);

        Assert.assertTrue(
                url.contains("/auth/login") || url.contains("login"),
                "Expected /auth/login but got: " + url);

        ExtentReportManager.getTest().pass("Step 4 passed");
    }

    private void waitForNavigation() {
        try {
            wait.until(ExpectedConditions.not(
                    ExpectedConditions.urlToBe(HOME_URL)));
        } catch (Exception e) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
    }
}