package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

/**
 * Pre-requisite: update PET_OWNER_EMAIL/PASSWORD, NGO_EMAIL/PASSWORD,
 * HOSPITAL_EMAIL/PASSWORD in AppConstants to real registered accounts.
 */
public class TC011_LoginSuccessRoutesTest extends BaseTest {

    private LoginPage     loginPage;
    private WebDriverWait wait;

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
        wait      = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test(description = "Step 1: Pet Owner login routes to /dashboard")
    public void step1_petOwnerRoutesToDashboard() {
        ExtentReportManager.createTest("TC011 - Step 1",
                "Pet Owner login -> /dashboard with 'Dashboard' sidebar active");

        loginPage.enterEmail(PET_OWNER_EMAIL);
        loginPage.enterPassword(PET_OWNER_PASSWORD);
        loginPage.clickSignIn();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));

        String url = loginPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after Pet Owner login: " + url);

        Assert.assertTrue(url.contains("/dashboard"),
                "Expected /dashboard for Pet Owner but got: " + url);

        String src = driver.getPageSource();
        Assert.assertTrue(src.contains("Dashboard"),
                "Dashboard menu item not found in sidebar after Pet Owner login");

        ExtentReportManager.getTest().pass("Step 1 passed");
    }

    @Test(description = "Step 2: NGO login routes to /ngo")
    public void step2_ngoRoutesToNgoDashboard() {
        ExtentReportManager.createTest("TC011 - Step 2",
                "NGO login -> /ngo with 'NGO Dashboard' sidebar active");

        loginPage.enterEmail(NGO_EMAIL);
        loginPage.enterPassword(NGO_PASSWORD);
        loginPage.clickSignIn();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));

        String url = loginPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after NGO login: " + url);

        Assert.assertTrue(url.contains("/ngo"),
                "Expected /ngo for NGO account but got: " + url);

        String src = driver.getPageSource();
        Assert.assertTrue(src.contains("NGO Dashboard") || src.contains("NGO"),
                "NGO Dashboard menu item not found after NGO login");

        ExtentReportManager.getTest().pass("Step 2 passed");
    }

    @Test(description = "Step 3: Veterinary Hospital login routes to /hospital")
    public void step3_hospitalRoutesToHospitalDashboard() {
        ExtentReportManager.createTest("TC011 - Step 3",
                "Veterinary Hospital login -> /hospital with 'Hospital Dashboard' sidebar active");

        loginPage.enterEmail(HOSPITAL_EMAIL);
        loginPage.enterPassword(HOSPITAL_PASSWORD);
        loginPage.clickSignIn();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));

        String url = loginPage.getCurrentUrl();
        ExtentReportManager.getTest().info("URL after Hospital login: " + url);

        Assert.assertTrue(url.contains("/hospital"),
                "Expected /hospital for Veterinary Hospital account but got: " + url);

        String src = driver.getPageSource();
        Assert.assertTrue(src.contains("Hospital Dashboard") || src.contains("Hospital"),
                "Hospital Dashboard menu item not found after Hospital login");

        ExtentReportManager.getTest().pass("Step 3 passed");
    }

    private void signOut() {
        try {
            // Try common sign-out patterns
            driver.findElement(By.xpath(
                    "//button[contains(text(),'Sign Out') or contains(text(),'Logout') or contains(text(),'Log out')]"))
                    .click();
            Thread.sleep(1000);
        } catch (Exception e) {
            // Navigate back to login as fallback
            driver.get(LOGIN_URL);
        }
    }
}
