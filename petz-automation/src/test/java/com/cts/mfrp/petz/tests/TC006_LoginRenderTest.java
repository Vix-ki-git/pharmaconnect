package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.LOGIN_URL;

public class TC006_LoginRenderTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
    }

    @Test(description = "Step 1: Two-column layout, left panel and right card elements are visible")
    public void step1_verifyLoginPageLayout() {
        ExtentReportManager.createTest("TC006 - Step 1",
                "Verify /auth/login two-column layout with left dark panel and right white card");

        Assert.assertTrue(loginPage.isLeftPanelVisible(),
                "Left panel with 'Animal Welfare Platform' headline not found");

        Assert.assertTrue(loginPage.isLeftPanelFeatureBulletsVisible(),
                "Feature bullets (Manage/Report/Adopt/Book) not all visible in left panel");

        Assert.assertTrue(loginPage.isEmailInputVisible(),
                "Email address input not visible");

        Assert.assertTrue(loginPage.isPasswordInputVisible(),
                "Password input not visible");

        Assert.assertTrue(loginPage.isSignInButtonPresent(),
                "Sign In button not present on page");

        Assert.assertFalse(loginPage.isSignInEnabled(),
                "Sign In button should be disabled on initial page load (fields empty)");

        Assert.assertTrue(loginPage.isCreateOneLinkVisible(),
                "'Create one' link not visible");

        String src = driver.getPageSource();
        Assert.assertTrue(src.contains("Welcome back") || src.contains("welcome back"),
                "Right card title 'Welcome back' not found");

        ExtentReportManager.getTest().pass("Step 1 passed — login page layout verified");
    }
}
