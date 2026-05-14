package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.LOGIN_URL;

public class TC008_LoginInvalidEmailTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
    }

    @Test(description = "Step 1: Sign In stays disabled with invalid email 'bad' and password 'x'")
    public void step1_invalidEmailKeepsButtonDisabled() {
        ExtentReportManager.createTest("TC008 - Step 1",
                "Sign In disabled when email='bad' and password='x'");

        loginPage.enterEmail("bad");
        loginPage.enterPassword("x");

        Assert.assertFalse(loginPage.isSignInEnabled(),
                "Sign In should stay disabled for invalid email syntax 'bad'");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: No inline error text shown after blurring invalid email field (UX gap)")
    public void step2_noInlineErrorOnBlur() {
        ExtentReportManager.createTest("TC008 - Step 2",
                "No inline error shown under email field after blur (UX gap — should be flagged)");

        loginPage.enterEmail("bad");
        loginPage.enterPassword("x");

        // Blur the email field by pressing Tab
        try {
            WebElement emailField = driver.findElement(
                    By.xpath("//input[@type='email'] | //input[@placeholder='you@example.com']"));
            emailField.sendKeys(Keys.TAB);
            Thread.sleep(800);
        } catch (Exception ignored) {}

        // Verify no inline validation error appears (this is the documented UX gap)
        boolean inlineErrorPresent = false;
        try {
            String src = driver.getPageSource().toLowerCase();
            // Look for common inline-error patterns near the email field
            inlineErrorPresent = src.contains("invalid email")
                    || src.contains("enter a valid email")
                    || src.contains("please enter")
                    || src.contains("email is not valid");
        } catch (Exception ignored) {}

        // TC008 documents that NO inline error appears — assert the documented state
        Assert.assertFalse(inlineErrorPresent,
                "Inline error appeared — UX gap may have been fixed; update test to assert error IS shown");

        // Sign In must still be disabled
        Assert.assertFalse(loginPage.isSignInEnabled(),
                "Sign In should still be disabled after blur with invalid email");

        ExtentReportManager.getTest().info(
                "UX gap confirmed: no inline email validation error on blur (as documented in TC008)");
        ExtentReportManager.getTest().pass("Step 2 passed");
    }
}
