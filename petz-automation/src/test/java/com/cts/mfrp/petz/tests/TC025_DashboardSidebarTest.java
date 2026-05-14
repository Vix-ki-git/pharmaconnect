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

public class TC025_DashboardSidebarTest extends BaseTest {

    @Test(description =
            "PETZ_TC025 - Validate sidebar items for a Pet Owner on /dashboard")
    public void verifySidebar() {
        ExtentReportManager.createTest(
                "PETZ_TC025_DashboardSidebar",
                "Validate sidebar items for a Pet Owner: Dashboard (active), My Adoptions, " +
                        "Browse Animals, Appointments, Rescue; plus user widget with 'USER' label.");

        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.isSidebarVisible(),
                "Sidebar is not visible on /dashboard.");

        Assert.assertTrue(
                dashboard.sidebarContains(
                        "Dashboard", "My Adoptions", "Browse Animals", "Appointments", "Rescue"),
                "Sidebar is missing one or more expected items: " +
                        "Dashboard, My Adoptions, Browse Animals, Appointments, Rescue.");

        Assert.assertTrue(dashboard.sidebarUserLabelVisible(),
                "User widget at the bottom of the sidebar does not show the 'USER' label.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));

        String[][] navItems = {
                {"My Adoptions",   "/adoption/my"},
                {"Browse Animals", "/adoption/animals"},
                {"Appointments",   "/appointments"},
                {"Rescue",         "/rescue"},
                {"Dashboard",      "/dashboard"}
        };

        for (String[] item : navItems) {
            String label   = item[0];
            String urlPart = item[1];

            dashboard.clickSidebarItem(label);
            wait.until(ExpectedConditions.urlContains(urlPart));

            Assert.assertTrue(driver.getCurrentUrl().contains(urlPart),
                    "Sidebar item '" + label + "' did not route to " + urlPart +
                            ". Actual URL: " + driver.getCurrentUrl());
        }
    }
}
