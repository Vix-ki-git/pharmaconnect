package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.LOGIN_URL;

public class TC009_LoginPasswordEyeToggleTest extends BaseTest {

    private static final String TEST_PASSWORD = "VisibleSecret1!";

    private LoginPage loginPage;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
    }

    @Test(description = "Step 1: Password characters are masked after typing")
    public void step1_passwordStartsMasked() {
        ExtentReportManager.createTest("TC009 - Step 1",
                "Password field type is 'password' (masked) after typing");

        loginPage.enterPassword(TEST_PASSWORD);

        String type = loginPage.getPasswordFieldType();
        ExtentReportManager.getTest().info("Password field type after typing: " + type);

        Assert.assertEquals(type, "password",
                "Password should be masked (type='password') before toggling");

        Assert.assertTrue(loginPage.isEyeTogglePresent(),
                "Eye icon / show-password toggle not found on the page");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: Clicking eye icon reveals password in plain text")
    public void step2_clickEyeRevealsPassword() {
        ExtentReportManager.createTest("TC009 - Step 2",
                "After clicking eye icon, password field type changes to 'text'");

        loginPage.enterPassword(TEST_PASSWORD);
        loginPage.clickEyeToggle();

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        String type = loginPage.getPasswordFieldType();
        ExtentReportManager.getTest().info("Password field type after first eye click: " + type);

        Assert.assertEquals(type, "text",
                "Password should be revealed (type='text') after clicking the eye icon");

        ExtentReportManager.getTest().pass("Step 2 passed");
    }

    @Test(description = "Step 3: Clicking eye icon again masks the password")
    public void step3_clickEyeAgainMasksPassword() {
        ExtentReportManager.createTest("TC009 - Step 3",
                "After clicking eye icon twice, password field type reverts to 'password'");

        loginPage.enterPassword(TEST_PASSWORD);
        loginPage.clickEyeToggle();
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        loginPage.clickEyeToggle();
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}

        String type = loginPage.getPasswordFieldType();
        ExtentReportManager.getTest().info("Password field type after second eye click: " + type);

        Assert.assertEquals(type, "password",
                "Password should be masked again (type='password') after second eye click");

        ExtentReportManager.getTest().pass("Step 3 passed");
    }
}
