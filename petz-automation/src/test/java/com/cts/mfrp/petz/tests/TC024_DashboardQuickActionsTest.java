package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.DashboardPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class TC024_DashboardQuickActionsTest extends BaseTest {

    @Test(description =
            "PETZ_TC024 - Validate Quick Actions cards route to the right pages")
    public void verifyQuickActions() {
        ExtentReportManager.createTest(
                "PETZ_TC024_DashboardQuickActions",
                "Validate the 5 Quick Action cards on /dashboard route to the correct destinations.");

        LoginPage login = new LoginPage(driver);
        login.loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.areQuickActionsVisible(),
                "One or more Quick Action cards (Report Rescue, Browse Adoptions, " +
                        "Book Appointment, My Adoptions, My Appointments) are not visible.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));

        String[][] actions = {
                {"Report Rescue",    "/rescue"},
                {"Browse Adoptions", "/adoption/animals"},
                {"Book Appointment", "/appointments/book"},
                {"My Adoptions",     "/adoption/my"},
                {"My Appointments",  "/appointments"}
        };

        for (String[] action : actions) {
            String label   = action[0];
            String urlPart = action[1];

            driver.get(DASHBOARD_URL);
            wait.until(ExpectedConditions.urlContains("/dashboard"));

            dashboard.clickQuickAction(label);
            wait.until(ExpectedConditions.urlContains(urlPart));

            Assert.assertTrue(driver.getCurrentUrl().contains(urlPart),
                    "Quick Action '" + label + "' did not route to " + urlPart +
                            ". Actual URL: " + driver.getCurrentUrl());
        }
    }
}
