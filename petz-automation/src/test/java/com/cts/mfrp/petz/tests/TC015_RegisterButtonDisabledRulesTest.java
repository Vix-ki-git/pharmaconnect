package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.REGISTER_URL;

public class TC015_RegisterButtonDisabledRulesTest extends BaseTest {

    private RegisterPage registerPage;

    @BeforeMethod
    public void openRegisterPage() {
        driver.get(REGISTER_URL);
        registerPage = new RegisterPage(driver);
    }

    @Test(description = "Step 1: 'Create Account' button is disabled on initial load")
    public void step1_buttonDisabledOnLoad() {
        ExtentReportManager.createTest("TC015 - Step 1",
                "'Create Account' button is grey/disabled on page load");

        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "'Create Account' should be disabled with no fields filled");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: Button stays disabled after filling Name, Phone, and valid Email only")
    public void step2_buttonDisabledWithNamePhoneEmail() {
        ExtentReportManager.createTest("TC015 - Step 2",
                "Button stays disabled after Name + Phone + Email (no password yet)");

        registerPage.enterFullName("Test User");
        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "Button should still be disabled after Full Name only");

        registerPage.enterPhone("+919876543210");
        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "Button should still be disabled after Full Name + Phone");

        registerPage.enterEmail("valid@example.com");
        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "Button should still be disabled after Full Name + Phone + Email (no password)");

        ExtentReportManager.getTest().pass("Step 2 passed");
    }

    @Test(description = "Step 3: Button stays disabled when password is less than 6 characters")
    public void step3_buttonDisabledShortPassword() {
        ExtentReportManager.createTest("TC015 - Step 3",
                "Button stays disabled when password has only 3 characters");

        registerPage.enterFullName("Test User");
        registerPage.enterPhone("+919876543210");
        registerPage.enterEmail("valid@example.com");
        registerPage.enterPassword("abc");

        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "Button should remain disabled with a 3-character password (minimum is 6)");

        ExtentReportManager.getTest().pass("Step 3 passed");
    }

    @Test(description = "Step 4: Button becomes enabled when password reaches exactly 6 characters")
    public void step4_buttonEnabledWithSixCharPassword() {
        ExtentReportManager.createTest("TC015 - Step 4",
                "Button turns orange/enabled when password is 'abc123' (6 characters)");

        registerPage.enterFullName("Test User");
        registerPage.enterPhone("+919876543210");
        registerPage.enterEmail("valid@example.com");
        registerPage.enterPassword("abc123");

        Assert.assertTrue(registerPage.isCreateAccountEnabled(),
                "Button should be enabled with a 6-character password");

        ExtentReportManager.getTest().pass("Step 4 passed");
    }

    @Test(description = "Step 5: Button disables again when email is replaced with an invalid value")
    public void step5_buttonDisablesOnInvalidEmail() {
        ExtentReportManager.createTest("TC015 - Step 5",
                "Button disables again when email is changed to 'not-an-email'");

        registerPage.enterFullName("Test User");
        registerPage.enterPhone("+919876543210");
        registerPage.enterEmail("valid@example.com");
        registerPage.enterPassword("abc123");

        Assert.assertTrue(registerPage.isCreateAccountEnabled(),
                "Pre-condition: button should be enabled before changing email");

        registerPage.clearEmail();
        registerPage.enterEmail("not-an-email");

        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "Button should disable again when email is replaced with invalid value 'not-an-email'");

        ExtentReportManager.getTest().pass("Step 5 passed");
    }
}
