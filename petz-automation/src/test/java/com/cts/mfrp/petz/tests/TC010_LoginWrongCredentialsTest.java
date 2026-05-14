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

import static com.cts.mfrp.petz.constants.AppConstants.LOGIN_URL;

public class TC010_LoginWrongCredentialsTest extends BaseTest {

    private static final String INVALID_EMAIL    = "nobody@example.com";
    private static final String INVALID_PASSWORD = "Whatever@123";

    private LoginPage   loginPage;
    private WebDriverWait wait;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
        wait      = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(description = "Step 1: Wrong credentials keep user on /auth/login with an error indicator")
    public void step1_wrongCredentialsShowError() {
        ExtentReportManager.createTest("TC010 - Step 1",
                "Submitting invalid credentials keeps user on /auth/login and shows error");

        loginPage.enterEmail(INVALID_EMAIL);
        loginPage.enterPassword(INVALID_PASSWORD);

        Assert.assertTrue(loginPage.isSignInEnabled(),
                "Sign In should be enabled before clicking (fields are populated with syntactically valid values)");

        loginPage.clickSignIn();

        // Wait for the network call to complete (stay on login or show error)
        try {
            // Wait up to 8s for either an error element or URL to remain /login
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        String currentUrl = loginPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after submit: " + currentUrl);

        Assert.assertTrue(
                currentUrl.contains("/auth/login") || currentUrl.contains("login"),
                "User should remain on /auth/login after wrong credentials. Actual URL: " + currentUrl);

        boolean errorVisible = loginPage.isErrorDisplayed();
        ExtentReportManager.getTest().info("Error indicator visible: " + errorVisible);

        Assert.assertTrue(errorVisible,
                "An error toast or inline message should be shown after submitting wrong credentials");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }
}
