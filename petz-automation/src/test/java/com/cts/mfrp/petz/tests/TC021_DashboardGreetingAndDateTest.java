package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.DashboardPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.DASHBOARD_URL;
import static com.cts.mfrp.petz.constants.AppConstants.PET_OWNER_NAME;

public class TC021_DashboardGreetingAndDateTest extends BaseTest {

    @Test(description =
            "PETZ_TC021 - Validate greeting block and date on /dashboard")
    public void verifyDashboardGreetingAndDate() {
        ExtentReportManager.createTest(
                "PETZ_TC021_DashboardGreetingAndDate",
                "Validate the greeting block and date on /dashboard for a Pet Owner.");

        new LoginPage(driver).loginAsPetOwner();
        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Expected to land on /dashboard after login, but was: "
                        + driver.getCurrentUrl());

        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.isGreetingVisible(),
                "Greeting heading 'Good <Morning|Afternoon|Evening>, <name>!' is not visible.");

        String firstName = PET_OWNER_NAME.split("\\s+")[0];
        Assert.assertTrue(dashboard.greetingContainsFirstName(firstName),
                "Greeting does not contain the user's first name: " + firstName);

        Assert.assertTrue(dashboard.isSubheadingVisible(),
                "Subheading 'Here's today's overview of the platform.' is not visible.");

        Assert.assertTrue(dashboard.isTodayDateVisible(),
                "Today's date (weekday, month, day, year) is not visible on the dashboard.");

        driver.get(DASHBOARD_URL);
    }

}
