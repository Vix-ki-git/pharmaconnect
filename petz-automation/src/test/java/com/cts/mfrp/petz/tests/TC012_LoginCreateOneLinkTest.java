package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC012_LoginCreateOneLinkTest extends BaseTest {

    private LoginPage     loginPage;
    private WebDriverWait wait;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
        wait      = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(description = "Step 1: 'Create one' link routes to /auth/register with 'Create account' title")
    public void step1_createOneLinkRoutesToRegister() {
        ExtentReportManager.createTest("TC012 - Step 1",
                "'Create one' -> /auth/register with right card title 'Create account'");

        loginPage.clickCreateOne();
        wait.until(ExpectedConditions.urlContains("register"));

        String url = loginPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after 'Create one' click: " + url);

        Assert.assertTrue(url.contains("/auth/register") || url.contains("register"),
                "Expected /auth/register but got: " + url);

        String src = driver.getPageSource();
        Assert.assertTrue(src.contains("Create account") || src.contains("Create Account"),
                "Right card title 'Create account' not found on register page");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: '← Back to home' on login page routes back to /")
    public void step2_backToHomeRoutesToRoot() {
        ExtentReportManager.createTest("TC012 - Step 2",
                "'← Back to home' -> / (landing page)");

        loginPage.clickBackToHome();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login")));

        String url = loginPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after 'Back to home' click: " + url);

        Assert.assertTrue(
                url.equals(HOME_URL) || url.equals(BASE_URL) || url.endsWith("/"),
                "Expected home URL (/) but got: " + url);

        ExtentReportManager.getTest().pass("Step 2 passed");
    }
}
