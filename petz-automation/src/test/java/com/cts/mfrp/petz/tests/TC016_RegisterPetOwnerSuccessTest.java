package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC016_RegisterPetOwnerSuccessTest extends BaseTest {

    private RegisterPage  registerPage;
    private WebDriverWait wait;

    // Unique email generated per run to avoid duplicate-account errors
    private String uniqueEmail;

    @BeforeMethod
    public void openRegisterPage() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        uniqueEmail  = "petowner_" + timestamp + "@test.com";

        driver.get(REGISTER_URL);
        registerPage = new RegisterPage(driver);
        wait         = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test(description = "Step 1: Fill all fields as Pet Owner and verify 'Create Account' becomes enabled")
    public void step1_fillAllFieldsEnablesButton() {
        ExtentReportManager.createTest("TC016 - Step 1",
                "Fill Full Name, Phone, unique Email, Password, Account Type=Pet Owner → button enabled");

        registerPage.enterFullName(REG_FULL_NAME);
        registerPage.enterPhone(REG_PHONE);
        registerPage.enterEmail(uniqueEmail);
        registerPage.enterPassword(REG_PASSWORD);
        registerPage.selectAccountType("Pet Owner");

        ExtentReportManager.getTest().info("Test email used: " + uniqueEmail);

        Assert.assertTrue(registerPage.isCreateAccountEnabled(),
                "'Create Account' should be enabled (orange) after all valid fields are filled");

        ExtentReportManager.getTest().pass("Step 1 passed — all inputs accepted and button is enabled");
    }

    @Test(description = "Full flow: Submit Pet Owner registration and verify post-registration state")
    public void step1_submitRegistrationAndVerifyResult() {
        ExtentReportManager.createTest("TC016 - Full flow",
                "Submit valid Pet Owner registration and verify redirect or success message");

        registerPage.enterFullName(REG_FULL_NAME);
        registerPage.enterPhone(REG_PHONE);
        registerPage.enterEmail(uniqueEmail);
        registerPage.enterPassword(REG_PASSWORD);
        registerPage.selectAccountType("Pet Owner");

        ExtentReportManager.getTest().info("Submitting registration with email: " + uniqueEmail);

        registerPage.clickCreateAccount();

        // Wait for navigation away from /auth/register or a success indicator
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.not(ExpectedConditions.urlContains("/auth/register")),
                    ExpectedConditions.presenceOfElementLocated(
                            org.openqa.selenium.By.xpath(
                                    "//*[contains(text(),'success') or contains(text(),'Success')"
                                    + " or contains(text(),'registered') or contains(text(),'Registered')"
                                    + " or contains(text(),'created') or contains(text(),'Created')]"))
            ));
        } catch (Exception ignored) {
            // Capture current state even if wait timed out
        }

        String currentUrl = registerPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after registration submit: " + currentUrl);

        String src = driver.getPageSource();

        boolean navigatedAway = !currentUrl.contains("/auth/register");
        boolean successMessageShown = src.toLowerCase().contains("success")
                || src.toLowerCase().contains("registered")
                || src.toLowerCase().contains("account created")
                || src.toLowerCase().contains("verify");

        Assert.assertTrue(navigatedAway || successMessageShown,
                "After successful registration, expected either navigation away from /auth/register "
                + "or a success/verify message. URL: " + currentUrl);

        ExtentReportManager.getTest().pass("Full flow passed — registration submitted successfully");
    }
}
