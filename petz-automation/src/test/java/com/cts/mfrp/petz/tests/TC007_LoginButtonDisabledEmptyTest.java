package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.LOGIN_URL;

public class TC007_LoginButtonDisabledEmptyTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
    }

    @Test(description = "Step 1: Sign In is disabled with empty Email and Password")
    public void step1_bothFieldsEmpty() {
        ExtentReportManager.createTest("TC007 - Step 1",
                "Sign In stays disabled when both Email and Password are empty");

        Assert.assertFalse(loginPage.isSignInEnabled(),
                "Sign In should be disabled when both fields are empty");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: Sign In stays disabled when only Email has an invalid single character")
    public void step2_onlyEmailTyped() {
        ExtentReportManager.createTest("TC007 - Step 2",
                "Sign In stays disabled with invalid single-char email and empty password");

        loginPage.enterEmail("a");

        Assert.assertFalse(loginPage.isSignInEnabled(),
                "Sign In should remain disabled with invalid email and empty password");

        ExtentReportManager.getTest().pass("Step 2 passed");
    }

    @Test(description = "Step 3: Sign In stays disabled when only Password is typed")
    public void step3_onlyPasswordTyped() {
        ExtentReportManager.createTest("TC007 - Step 3",
                "Sign In stays disabled with empty email and a password");

        loginPage.enterPassword("anyPassword");

        Assert.assertFalse(loginPage.isSignInEnabled(),
                "Sign In should remain disabled with empty email");

        ExtentReportManager.getTest().pass("Step 3 passed");
    }

    @Test(description = "Step 4: Sign In turns enabled with valid email AND any password")
    public void step4_validEmailAndPassword() {
        ExtentReportManager.createTest("TC007 - Step 4",
                "Sign In turns enabled with a syntactically valid email and a password");

        loginPage.enterEmail("valid@example.com");
        loginPage.enterPassword("anyPassword");

        Assert.assertTrue(loginPage.isSignInEnabled(),
                "Sign In should be enabled with a valid email and a non-empty password");

        ExtentReportManager.getTest().pass("Step 4 passed");
    }
}
