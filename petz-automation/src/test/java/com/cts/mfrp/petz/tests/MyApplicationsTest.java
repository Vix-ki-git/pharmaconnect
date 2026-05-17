package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.api.clients.AuthClient;
import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.models.auth.RegisterRequest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.MyApplicationsPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.response.Response;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * My Applications scenario — TS07, TC036 + TC037 in one class.
 * BaseTest provides a fresh WebDriver per @Test method.
 */
public class MyApplicationsTest extends BaseTest {

    private final AuthClient auth = new AuthClient();

    @BeforeClass(alwaysRun = true)
    public void initApi() {
        // AuthClient serialises POJOs via Jackson; drop nulls so optional fields don't leak.
        ApiSpecs.configureJacksonOnce();
    }

    private String[] registerFreshPetOwner() {
        long ts = System.currentTimeMillis();
        String email    = "auto_" + ts + "@example.com";
        String password = "Strong@123";
        String name     = "Auto User " + ts;
        String phone    = "98765" + String.format("%05d", (int) (Math.random() * 100_000));

        RegisterRequest req = new RegisterRequest(name, email, password, phone, "USER");
        Response r = auth.register(req);
        if (r.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to register fresh pet owner for empty-state test. HTTP "
                            + r.statusCode() + " — " + r.asString());
        }
        return new String[] { email, password };
    }

    // ─── TC036 ─────────────────────────────────────────────────────────────
    @Test(priority = 36, description =
            "PETZ_TC036 - Validate the empty state on /adoption/my for a brand-new account")
    public void TC036_verifyMyApplicationsEmptyState() {
        ExtentReportManager.createTest(
                "PETZ_TC036_MyAppsEmpty",
                "Register a fresh USER, log in, open /adoption/my and validate the empty state: " +
                        "heading, top-right Browse Animals, clipboard icon, 'No applications yet', " +
                        "subtitle, and the orange Browse Animals CTA. Then click CTA and assert " +
                        "navigation to /adoption/animals.");

        String[] creds = registerFreshPetOwner();
        new LoginPage(driver).login(creds[0], creds[1]);

        MyApplicationsPage myApps = new MyApplicationsPage(driver);
        myApps.open();

        Assert.assertTrue(myApps.isHeadingVisible(),
                "Heading 'My Applications' is not visible on /adoption/my.");

        Assert.assertTrue(myApps.isSubtitleVisible(),
                "Subtitle 'Track your adoption application statuses' is not visible.");

        Assert.assertTrue(myApps.isBrowseAnimalsTopRightVisible(),
                "'Browse Animals' button in the top-right of the page is not visible.");

        Assert.assertTrue(myApps.isEmptyStateVisible(),
                "Empty-state container is not visible for a brand-new account.");

        Assert.assertTrue(myApps.isClipboardIconVisible(),
                "Orange clipboard icon is not visible inside the empty state.");

        Assert.assertTrue(myApps.isNoApplicationsTextVisible(),
                "'No applications yet' text is not visible inside the empty state.");

        Assert.assertTrue(myApps.isEmptyStateSubtitleVisible(),
                "Empty-state subtitle 'Find your perfect companion and submit an adoption " +
                        "application.' is not visible.");

        Assert.assertTrue(myApps.isBrowseAnimalsCtaVisible(),
                "Orange 'Browse Animals' CTA below the empty-state copy is not visible.");

        Assert.assertFalse(myApps.hasApplicationCards(),
                "Fresh account should not see any application cards on /adoption/my, " +
                        "but at least one card is visible.");

        // Step 2 — click 'Browse Animals' navigates to /adoption/animals.
        myApps.clickBrowseAnimalsCta();
        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT))
                .until(ExpectedConditions.urlContains("/adoption/animals"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/adoption/animals")
                        && !driver.getCurrentUrl().contains("/adoption/my"),
                "Clicking 'Browse Animals' did not navigate to /adoption/animals. Actual URL: "
                        + driver.getCurrentUrl());
    }

    // ─── TC037 ─────────────────────────────────────────────────────────────
    @Test(priority = 37, description =
            "PETZ_TC037 - Validate the populated list on /adoption/my for a user with applications")
    public void TC037_verifyMyApplicationsListWithStatus() {
        ExtentReportManager.createTest(
                "PETZ_TC037_MyAppsListWithStatus",
                "Log in as the seed pet owner (who already has at least one submitted application), " +
                        "open /adoption/my, and assert at least one card surfaces a status badge " +
                        "(Pending / Under Review / Approved / Rejected).");

        new LoginPage(driver).loginAsPetOwner();

        MyApplicationsPage myApps = new MyApplicationsPage(driver);
        myApps.open();

        Assert.assertTrue(myApps.isHeadingVisible(),
                "Heading 'My Applications' is not visible on /adoption/my.");

        Assert.assertTrue(myApps.isSubtitleVisible(),
                "Subtitle 'Track your adoption application statuses' is not visible.");

        Assert.assertTrue(myApps.hasApplicationCards(),
                "Expected at least one application card on /adoption/my for the seed pet owner, " +
                        "but none were found. (Seed user is expected to have an existing application.)");

        Assert.assertTrue(myApps.firstCardHasStatusBadge(),
                "First application card does not surface a status badge " +
                        "(Pending / Under Review / Approved / Rejected). " +
                        "Card text: " + driver.findElements(
                                org.openqa.selenium.By.xpath(
                                        "//mat-card | //*[contains(@class,'card')]"))
                                .stream().findFirst().map(e -> e.getText()).orElse("(no card)"));
    }
}
