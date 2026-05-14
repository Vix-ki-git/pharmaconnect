package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.DashboardPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC023_DashboardStatTilesEmptyTest extends BaseTest {

    @Test(description =
            "PETZ_TC023 - Validate 4 stat tiles on /dashboard for a new Pet Owner")
    public void verifyStatTilesEmpty() {
        ExtentReportManager.createTest(
                "PETZ_TC023_DashboardStatTilesEmpty",
                "Validate the 4 stat tiles (MY PETS, APPOINTMENTS, RESCUE REPORTS, ADOPTIONS) " +
                        "and their empty-state breakdown chips for a brand-new Pet Owner.");

        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.areStatTilesVisible(),
                "One or more of the 4 stat tiles (MY PETS, APPOINTMENTS, RESCUE REPORTS, " +
                        "ADOPTIONS) are not visible on /dashboard.");

        String src = driver.getPageSource();

        Assert.assertTrue(src.contains("MY PETS") || src.contains("My Pets"),
                "MY PETS tile is missing.");
        Assert.assertTrue(src.contains("APPOINTMENTS") || src.contains("Appointments"),
                "APPOINTMENTS tile is missing.");
        Assert.assertTrue(src.contains("RESCUE REPORTS") || src.contains("Rescue Reports"),
                "RESCUE REPORTS tile is missing.");
        Assert.assertTrue(src.contains("ADOPTIONS") || src.contains("Adoptions"),
                "ADOPTIONS tile is missing.");

        Assert.assertTrue(dashboard.areStatTileBreakdownsVisible(),
                "Stat tiles do not show their breakdown chips " +
                        "(e.g. Dogs/Cats, Upcoming/Done, Pending/Resolved, In Review/Approved, " +
                        "or the corresponding empty-state copy).");
    }
}
