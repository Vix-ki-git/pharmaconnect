package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.api.auth.Role;
import com.cts.mfrp.petz.api.clients.AuthClient;
import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.models.auth.RegisterRequest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.MyAppointmentsPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * My Appointments scenario — TS09, TC045 + TC046 in one class.
 * BaseTest provides a fresh WebDriver per @Test method.
 */
public class MyAppointmentsTest extends BaseTest {

    private final AuthClient auth = new AuthClient();

    @BeforeClass(alwaysRun = true)
    public void initApi() {
        ApiSpecs.configureJacksonOnce();
    }

    private String[] registerFreshPetOwner() {
        long ts = System.currentTimeMillis();
        String email    = "auto_appt_" + ts + "@example.com";
        String password = "Strong@123";
        String name     = "Auto Appt User " + ts;
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

    /** Books an appointment via the API for the seed pet owner so TC046 always has data. */
    private void ensureSeedUserHasAppointment() {
        // Reuse the cached seed-user token via TokenManager + Role.USER.
        var spec = ApiSpecs.asRole(Role.USER);

        Response existing = RestAssured.given(spec).when().get("/appointments/my");
        if (existing.statusCode() == 200) {
            List<?> appts = existing.jsonPath().getList("data");
            if (appts != null && !appts.isEmpty()) {
                return;
            }
        }

        // No bookings yet — create one. Pick the seed user's first pet.
        Response pets = RestAssured.given(spec).when().get("/pets/my");
        Integer petId = null;
        if (pets.statusCode() == 200) {
            try { petId = pets.jsonPath().getInt("data[0].id"); } catch (Exception ignored) {}
        }
        if (petId == null) {
            throw new IllegalStateException(
                    "Seed user has no pets — cannot create test appointment. " +
                            "/pets/my responded: " + pets.statusCode() + " " + pets.asString());
        }

        // Random far-future weekday + minute-precise time to avoid slot collisions across reruns.
        LocalDate future = LocalDate.now().plusDays(120 + (int) (Math.random() * 600));
        while (future.getDayOfWeek().getValue() >= 6) {
            future = future.plusDays(1);
        }
        String apptTime = String.format("%02d:%02d",
                10 + (int) (Math.random() * 6),  // 10:xx — 15:xx
                (int) (Math.random() * 2) == 0 ? 0 : 30);

        Map<String, Object> body = Map.of(
                "hospitalId", 1,
                "doctorId",   1,
                "petId",      petId,
                "apptDate",   future.toString(),
                "apptTime",   apptTime,
                "reason",     "QA setup for TC046 — please ignore.");

        Response book = RestAssured.given(spec).body(body).when().post("/appointments");
        if (book.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to seed appointment for TC046. HTTP " + book.statusCode()
                            + " — " + book.asString());
        }
    }

    // ─── TC045 ─────────────────────────────────────────────────────────────
    @Test(priority = 45, description =
            "PETZ_TC045 - Validate the empty state on /appointments for a brand-new account")
    public void TC045_verifyMyAppointmentsEmptyState() {
        ExtentReportManager.createTest(
                "PETZ_TC045_MyApptsEmpty",
                "Register a fresh USER, log in, open /appointments and validate: title, " +
                        "subtitle, top-right '+ Book Appointment', X-calendar icon, " +
                        "'No appointments', empty-state subtitle, and '+ Book Now' CTA. " +
                        "Click CTA → /appointments/book.");

        String[] creds = registerFreshPetOwner();
        new LoginPage(driver).login(creds[0], creds[1]);

        MyAppointmentsPage myAppts = new MyAppointmentsPage(driver);
        myAppts.open();

        Assert.assertTrue(myAppts.isHeadingVisible(),
                "Heading 'My Appointments' is not visible on /appointments.");

        Assert.assertTrue(myAppts.isSubtitleVisible(),
                "Subtitle 'Track all your scheduled vet visits' is not visible.");

        Assert.assertTrue(myAppts.isBookAppointmentTopRightVisible(),
                "'+ Book Appointment' button at the top-right is not visible.");

        Assert.assertTrue(myAppts.isEmptyStateVisible(),
                "Empty-state container is not visible for a brand-new account.");

        Assert.assertTrue(myAppts.isEmptyStateIconVisible(),
                "X-calendar (empty state) icon is not visible.");

        Assert.assertTrue(myAppts.isNoAppointmentsTextVisible(),
                "'No appointments' text is not visible.");

        Assert.assertTrue(myAppts.isEmptyStateSubtitleVisible(),
                "Empty-state subtitle \"You haven't booked any vet visits yet. " +
                        "Schedule one for your pet today.\" is not visible.");

        Assert.assertTrue(myAppts.isBookNowCtaVisible(),
                "'+ Book Now' CTA inside the empty-state card is not visible.");

        Assert.assertFalse(myAppts.hasAppointmentRows(),
                "Fresh account should not show any appointment rows on /appointments.");

        // Step 2 — click '+ Book Now' navigates to /appointments/book.
        myAppts.clickBookNowCta();
        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT))
                .until(ExpectedConditions.urlContains("/appointments/book"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/appointments/book"),
                "Clicking '+ Book Now' did not navigate to /appointments/book. Actual URL: "
                        + driver.getCurrentUrl());
    }

    // ─── TC046 ─────────────────────────────────────────────────────────────
    @Test(priority = 46, description =
            "PETZ_TC046 - Validate the list state on /appointments for a user with bookings")
    public void TC046_verifyMyAppointmentsList() {
        ExtentReportManager.createTest(
                "PETZ_TC046_MyApptsList",
                "Ensure the seed pet owner has at least one booking (creating one via the API " +
                        "if needed), then log in via the UI, open /appointments, and verify the " +
                        "row shows a status badge and date/time info.");

        ensureSeedUserHasAppointment();

        new LoginPage(driver).loginAsPetOwner();
        MyAppointmentsPage myAppts = new MyAppointmentsPage(driver);
        myAppts.open();

        Assert.assertTrue(myAppts.isHeadingVisible(),
                "Heading 'My Appointments' is not visible on /appointments.");

        Assert.assertTrue(myAppts.isSubtitleVisible(),
                "Subtitle 'Track all your scheduled vet visits' is not visible.");

        Assert.assertTrue(myAppts.hasAppointmentRows(),
                "Expected at least one appointment row on /appointments for the seed pet owner " +
                        "(an appointment was just seeded via the API).");

        Assert.assertTrue(myAppts.firstRowHasStatusBadge(),
                "First appointment row does not surface a status badge " +
                        "(PENDING / CONFIRMED / COMPLETED / CANCELLED). " +
                        "Row text was: " + myAppts.firstRowText());

        Assert.assertTrue(myAppts.firstRowHasDateOrTime(),
                "First appointment row does not display a date or time. " +
                        "Row text was: " + myAppts.firstRowText());

        // 'Where applicable' — Reschedule/Cancel may not be present for COMPLETED/CANCELLED rows,
        // so this is a soft check that simply logs the observation. It is not a hard assert.
        boolean hasActionButton = myAppts.firstRowHasActionButton();
        ExtentReportManager.getTest().info(
                "First row has Reschedule/Cancel action button: " + hasActionButton);
    }
}
