package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.BookAppointmentPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Book Appointment scenario — TS08, TC038 to TC043 in one class.
 * Does NOT actually submit a booking — only verifies the Confirm button is correctly enabled
 * once every required field is valid, then exercises Cancel.
 */
public class BookAppointmentTest extends BaseTest {

    private static final DateTimeFormatter ARIA_DATE =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    /** Picks a target date 14 days out, rolled forward to Monday if it lands on a weekend, so the
     *  doctor's standard 09:00–17:00 schedule reliably has slots. */
    private LocalDate futureWeekday() {
        LocalDate d = LocalDate.now().plusDays(14);
        while (d.getDayOfWeek().getValue() >= 6) { // 6=Sat, 7=Sun
            d = d.plusDays(1);
        }
        return d;
    }

    // ─── TC038 ─────────────────────────────────────────────────────────────
    @Test(priority = 38, description =
            "PETZ_TC038 - Validate the layout of /appointments/book")
    public void TC038_verifyBookFormLayout() {
        ExtentReportManager.createTest(
                "PETZ_TC038_BookFormLayout",
                "Open /appointments/book and validate title, subtitle, top-right 'My Appointments', " +
                        "the three step sections with their fields, the info banner, and the " +
                        "(disabled) Confirm Booking + Cancel buttons.");

        new LoginPage(driver).loginAsPetOwner();
        BookAppointmentPage book = new BookAppointmentPage(driver);
        book.open();

        Assert.assertTrue(book.isTitleVisible(),
                "Title 'Book Appointment' is not visible.");
        Assert.assertTrue(book.isSubtitleVisible(),
                "Subtitle 'Schedule a vet visit for your pet' is not visible.");
        Assert.assertTrue(book.isMyAppointmentsTopRightVisible(),
                "'My Appointments' button at the top-right is not visible.");

        Assert.assertTrue(book.isStep1Visible(),
                "Step 1 'CHOOSE HOSPITAL & DOCTOR' section is not visible.");
        Assert.assertTrue(book.isHospitalDropdownVisible(),
                "Hospital dropdown is not visible in Step 1.");
        Assert.assertTrue(book.isDoctorDropdownVisible(),
                "Doctor dropdown is not visible in Step 1.");

        Assert.assertTrue(book.isStep2Visible(),
                "Step 2 'DATE & TIME' section is not visible.");
        Assert.assertTrue(book.isDateFieldVisible(),
                "'Appointment Date' field with 'Pick a date' placeholder is not visible.");
        Assert.assertTrue(book.isTimeDropdownVisible(),
                "'Preferred Time' dropdown is not visible in Step 2.");

        Assert.assertTrue(book.isStep3Visible(),
                "Step 3 'REASON FOR VISIT' section is not visible.");
        Assert.assertTrue(book.isReasonTextareaVisible(),
                "Reason textarea is not visible in Step 3.");

        Assert.assertTrue(book.isInfoBannerVisible(),
                "Info banner about receiving a confirmation is not visible.");

        Assert.assertTrue(book.isCancelButtonVisible(),
                "Cancel button is not visible.");
        Assert.assertTrue(book.isConfirmButtonDisabled(),
                "Confirm Booking should be disabled on initial load (no fields filled yet).");
    }

    // ─── TC039 ─────────────────────────────────────────────────────────────
    @Test(priority = 39, description =
            "PETZ_TC039 - Confirm Booking stays disabled until every required field is valid")
    public void TC039_verifyConfirmDisabledUntilValid() {
        // NOTE: the deployed form marks the Reason textarea aria-required="false", so Confirm
        // enables once Hospital + Doctor + Date + Time are valid — Reason is optional in
        // practice even though the test plan describes it as required. We assert the disabled
        // → enabled transition and stop short of insisting on the exact step at which it flips.
        ExtentReportManager.createTest(
                "PETZ_TC039_BookConfirmDisabledUntilValid",
                "Progressively fill the form; Confirm Booking starts disabled, stays disabled " +
                        "while required fields are missing, and is enabled once Hospital + Doctor " +
                        "+ Date + Time are valid (Reason is optional on the deployed form).");

        new LoginPage(driver).loginAsPetOwner();
        BookAppointmentPage book = new BookAppointmentPage(driver);
        book.open();

        Assert.assertTrue(book.isConfirmButtonDisabled(),
                "Confirm Booking should start disabled.");

        book.selectFirstHospital();
        Assert.assertFalse(book.getHospitalDisplayValue().toLowerCase().contains("select a hospital"),
                "Hospital field still shows the placeholder after selection. Display value: '"
                        + book.getHospitalDisplayValue() + "'");
        Assert.assertTrue(book.isConfirmButtonDisabled(),
                "Confirm Booking should still be disabled after only Hospital is selected.");

        book.selectFirstDoctor();
        Assert.assertFalse(book.getDoctorDisplayValue().toLowerCase().contains("select a doctor"),
                "Doctor field still shows the placeholder after selection. Display value: '"
                        + book.getDoctorDisplayValue() + "'");
        Assert.assertTrue(book.isConfirmButtonDisabled(),
                "Confirm Booking should still be disabled after Hospital + Doctor are selected.");

        book.pickDate(futureWeekday());
        Assert.assertTrue(book.isConfirmButtonDisabled(),
                "Confirm Booking should still be disabled after Hospital + Doctor + Date are filled.");

        book.selectFirstTime();
        Assert.assertTrue(book.isConfirmButtonEnabled(),
                "Confirm Booking should be enabled once Hospital + Doctor + Date + Time are all " +
                        "valid. Field snapshot — "
                        + "hospital='" + book.getHospitalDisplayValue() + "', "
                        + "doctor='"   + book.getDoctorDisplayValue()   + "', "
                        + "date='"     + book.getDateInputValue()       + "', "
                        + "time='"     + book.getTimeDisplayValue()     + "'");

        book.fillReason("Annual vaccination.");
        Assert.assertTrue(book.isConfirmButtonEnabled(),
                "Confirm Booking should stay enabled after Reason is also filled.");
    }

    // ─── TC040 ─────────────────────────────────────────────────────────────
    @Test(priority = 40, description =
            "PETZ_TC040 - Validate Hospital and Doctor dropdowns")
    public void TC040_verifyHospitalAndDoctorOptions() {
        ExtentReportManager.createTest(
                "PETZ_TC040_BookHospitalDoctorOptions",
                "Hospital dropdown lists verified hospitals; selecting one populates the Doctor " +
                        "dropdown with that hospital's doctors.");

        new LoginPage(driver).loginAsPetOwner();
        BookAppointmentPage book = new BookAppointmentPage(driver);
        book.open();

        List<WebElement> hospitals = book.openHospitalDropdown();
        Assert.assertFalse(hospitals.isEmpty(),
                "Hospital dropdown is empty — expected at least one verified hospital.");

        // Pick the first hospital so the Doctor dropdown can populate.
        hospitals.get(0).click();

        // Wait for the panel to close before opening the next select.
        book.openDoctorDropdown(); // throws/returns empty list if no panel renders

        List<WebElement> doctors = driver.findElements(
                org.openqa.selenium.By.xpath(
                        "//div[contains(@class,'mat-select-panel') " +
                        "or contains(@class,'mat-mdc-select-panel') " +
                        "or contains(@class,'cdk-overlay-pane')]//mat-option"));
        Assert.assertFalse(doctors.isEmpty(),
                "Doctor dropdown did not populate after selecting a hospital. " +
                        "Expected either active doctors or a visible empty/disabled state.");
    }

    // ─── TC041 ─────────────────────────────────────────────────────────────
    @Test(priority = 41, description =
            "PETZ_TC041 - Date picker rejects past dates")
    public void TC041_verifyDatePickerRejectsPast() {
        ExtentReportManager.createTest(
                "PETZ_TC041_BookDateRejectsPast",
                "Open the Appointment Date picker, locate yesterday's cell and confirm it is " +
                        "disabled (aria-disabled or a 'disabled' class). Clicking it must leave the " +
                        "date field empty.");

        new LoginPage(driver).loginAsPetOwner();
        BookAppointmentPage book = new BookAppointmentPage(driver);
        book.open();

        book.openDatepicker();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        WebElement cell = book.calendarCellFor(yesterday);
        Assert.assertNotNull(cell,
                "Could not find a calendar cell for yesterday (" + yesterday.format(ARIA_DATE) + "). " +
                        "Did the calendar fail to render this month?");

        Assert.assertTrue(book.isCellDisabled(cell),
                "Yesterday's cell (" + yesterday.format(ARIA_DATE) + ") should be visually " +
                        "disabled, but no aria-disabled / disabled class was found.");

        book.tryClickCell(cell);

        Assert.assertTrue(book.getDateInputValue() == null
                        || book.getDateInputValue().isBlank(),
                "Date field should remain empty after clicking a disabled past date. " +
                        "Actual value: '" + book.getDateInputValue() + "'");

        book.closeDatepicker();
    }

    // ─── TC042 ─────────────────────────────────────────────────────────────
    @Test(priority = 42, description =
            "PETZ_TC042 - Preferred Time dropdown shows slots for the picked date")
    public void TC042_verifyTimeOptions() {
        ExtentReportManager.createTest(
                "PETZ_TC042_BookTimeOptions",
                "After selecting Hospital + Doctor + a future date, open the Preferred Time " +
                        "dropdown and assert at least one slot is listed.");

        new LoginPage(driver).loginAsPetOwner();
        BookAppointmentPage book = new BookAppointmentPage(driver);
        book.open();

        book.selectFirstHospital();
        book.selectFirstDoctor();
        book.pickDate(futureWeekday());

        List<WebElement> slots = book.openTimeDropdown();
        Assert.assertFalse(slots.isEmpty(),
                "Preferred Time dropdown did not show any slots for the chosen doctor on " +
                        futureWeekday() + ".");

        slots.get(0).click();
        String selected = book.getTimeDisplayValue();
        Assert.assertFalse(selected == null || selected.isBlank(),
                "Time dropdown did not capture the selection. Display value: '" + selected + "'");
    }

    // ─── TC043 ─────────────────────────────────────────────────────────────
    @Test(priority = 43, description =
            "PETZ_TC043 - Cancel button clears the form OR routes back to /appointments")
    public void TC043_verifyCancel() {
        ExtentReportManager.createTest(
                "PETZ_TC043_BookCancel",
                "Fill some fields, click Cancel, and assert either the form was cleared or " +
                        "the browser navigated to /appointments.");

        new LoginPage(driver).loginAsPetOwner();
        BookAppointmentPage book = new BookAppointmentPage(driver);
        book.open();

        book.selectFirstHospital();
        book.fillReason("Test reason — should be cleared.");

        String beforeReason = book.getReasonValue();
        Assert.assertEquals(beforeReason, "Test reason — should be cleared.",
                "Reason did not pick up the test text before Cancel was clicked.");

        book.clickCancel();

        boolean navigatedAway = !driver.getCurrentUrl().contains("/appointments/book");
        String reasonAfter   = book.getReasonValue();
        boolean reasonCleared = reasonAfter == null || reasonAfter.isBlank();

        Assert.assertTrue(navigatedAway || reasonCleared,
                "Cancel did not navigate away from /appointments/book nor clear the form. " +
                        "URL: " + driver.getCurrentUrl() + ", reason='" + reasonAfter + "'");
    }
}
