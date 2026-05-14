package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.REGISTER_URL;

public class TC013_RegisterRenderTest extends BaseTest {

    private RegisterPage registerPage;

    @BeforeMethod
    public void openRegisterPage() {
        driver.get(REGISTER_URL);
        registerPage = new RegisterPage(driver);
    }

    @Test(description = "Step 1: Two-column layout with left panel, right card and all 5 inputs visible")
    public void step1_verifyRegisterPageLayout() {
        ExtentReportManager.createTest("TC013 - Step 1",
                "Verify /auth/register layout: left panel, right card, inputs, dropdown, button");

        Assert.assertTrue(registerPage.isLeftPanelVisible(),
                "Left panel 'Join the PETZ Community' not visible");

        Assert.assertTrue(registerPage.areRoleBulletsVisible(),
                "Role bullets (Pet Owner / NGO / Veterinary Hospital) not all visible in left panel");

        Assert.assertTrue(registerPage.isFullNameInputVisible(),
                "Full Name input not visible");

        Assert.assertTrue(registerPage.isPhoneInputVisible(),
                "Phone input not visible");

        Assert.assertTrue(registerPage.isEmailInputVisible(),
                "Email address input not visible");

        Assert.assertTrue(registerPage.isPasswordInputVisible(),
                "Password input not visible");

        Assert.assertTrue(registerPage.isAccountTypeDropdownVisible(),
                "Account Type dropdown not visible");

        Assert.assertTrue(registerPage.isCreateAccountButtonPresent(),
                "'Create Account' button not present");

        Assert.assertFalse(registerPage.isCreateAccountEnabled(),
                "'Create Account' button should be disabled on initial load (fields empty)");

        Assert.assertTrue(registerPage.isSignInLinkVisible(),
                "'Already have an account? Sign in' link not visible");

        String src = driver.getPageSource();
        Assert.assertTrue(src.contains("Create account") || src.contains("Create Account"),
                "Right card title 'Create account' not found");

        ExtentReportManager.getTest().pass("Step 1 passed — register page layout verified");
    }

    @Test(description = "Step 2: Placeholder texts are correct for all inputs")
    public void step2_verifyPlaceholderTexts() {
        ExtentReportManager.createTest("TC013 - Step 2",
                "Verify placeholder texts: John Doe, +91 phone, you@example.com, min. 6 characters");

        Assert.assertTrue(registerPage.areInputPlaceholdersCorrect(),
                "One or more placeholder texts are missing or incorrect. "
                + "Expected: 'John Doe', '+91 00000 00000', 'you@example.com', 'min. 6 characters'");

        ExtentReportManager.getTest().pass("Step 2 passed");
    }
}
