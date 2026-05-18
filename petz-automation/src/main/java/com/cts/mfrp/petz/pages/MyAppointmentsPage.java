package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import static com.cts.mfrp.petz.constants.AppConstants.APPOINTMENTS_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * Page Object for /appointments — the pet owner's 'My Appointments' page.
 * Covers both the empty state and the populated list state (TC045 + TC046).
 */
public class MyAppointmentsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Page chrome ──
    private final By heading = By.xpath(
            "//*[self::h1 or self::h2 or self::h3]" +
            "[normalize-space()='My Appointments' " +
            "or contains(normalize-space(.),'My Appointments')]");

    private final By subtitle = By.xpath(
            "//*[contains(normalize-space(.),'Track all your scheduled vet visits')]");

    // First 'Book Appointment'-style control on the page = the top-right header button.
    private final By bookAppointmentTopRight = By.xpath(
            "(//*[self::a or self::button or self::mat-button or " +
            "(self::*[@role='button'])][contains(normalize-space(.),'Book Appointment')])[1]");

    // ── Empty state ──
    private final By emptyStateContainer = By.xpath(
            "//*[contains(normalize-space(.),'No appointments')]" +
            "/ancestor::*[self::section or self::div][1]");

    private final By noAppointmentsText = By.xpath(
            "//*[normalize-space()='No appointments' " +
            "or contains(normalize-space(.),'No appointments')]");

    private final By emptyStateSubtitle = By.xpath(
            "//*[contains(normalize-space(.)," +
            "\"You haven't booked any vet visits yet\") " +
            "or contains(normalize-space(.)," +
            "'You have not booked any vet visits yet')]");

    private final By emptyStateIcon = By.xpath(
            "//*[contains(normalize-space(.),'No appointments')]" +
            "/ancestor::*[self::section or self::div][1]" +
            "//*[self::mat-icon or self::svg or self::i " +
            "or (self::span and (contains(@class,'icon') or contains(@class,'material')))]");

    // 'Book Now' CTA inside the empty-state card.
    private final By bookNowCta = By.xpath(
            "(//*[contains(normalize-space(.),'No appointments')]" +
            "/ancestor::*[self::section or self::div][1]" +
            "//*[self::a or self::button or self::mat-button or " +
            "(self::*[@role='button'])][contains(normalize-space(.),'Book Now') " +
            "or contains(normalize-space(.),'Book Appointment')])[1]");

    // ── Populated state ──
    private final By appointmentRows = By.xpath(
            "//mat-card[.//*[contains(@class,'status') or contains(@class,'badge') " +
            "or contains(@class,'chip')]] " +
            "| //*[contains(@class,'appointment') or contains(@class,'booking') " +
            "or contains(@class,'card')]" +
            "[.//*[contains(@class,'status') or contains(@class,'badge') or contains(@class,'chip')]]");

    private static final String[] STATUS_TOKENS = {
            "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"
    };

    public MyAppointmentsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() {
        driver.get(APPOINTMENTS_URL);
        wait.until(ExpectedConditions.urlContains("/appointments"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ── Chrome assertions ──

    public boolean isHeadingVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(heading))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("My Appointments");
        }
    }

    public boolean isSubtitleVisible() {
        try {
            return driver.findElement(subtitle).isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Track all your scheduled vet visits");
        }
    }

    public boolean isBookAppointmentTopRightVisible() {
        try { return driver.findElement(bookAppointmentTopRight).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    // ── Empty state ──

    public boolean isEmptyStateVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(emptyStateContainer))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("No appointments");
        }
    }

    public boolean isNoAppointmentsTextVisible() {
        try { return driver.findElement(noAppointmentsText).isDisplayed(); }
        catch (Exception e) { return driver.getPageSource().contains("No appointments"); }
    }

    public boolean isEmptyStateSubtitleVisible() {
        try { return driver.findElement(emptyStateSubtitle).isDisplayed(); }
        catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("You haven't booked any vet visits yet")
                    || src.contains("You have not booked any vet visits yet");
        }
    }

    public boolean isEmptyStateIconVisible() {
        try { return driver.findElement(emptyStateIcon).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean isBookNowCtaVisible() {
        try { return driver.findElement(bookNowCta).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public void clickBookNowCta() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(bookNowCta));
        scrollAndClick(btn);
    }

    // ── Populated state ──

    public boolean hasAppointmentRows() {
        try { return !driver.findElements(appointmentRows).isEmpty(); }
        catch (Exception e) { return false; }
    }

    public int appointmentRowCount() {
        try { return driver.findElements(appointmentRows).size(); }
        catch (Exception e) { return 0; }
    }

    /** True if the first appointment row text contains a status badge keyword. */
    public boolean firstRowHasStatusBadge() {
        List<WebElement> rows = driver.findElements(appointmentRows);
        if (rows.isEmpty()) return false;
        String text = rows.get(0).getText().toUpperCase(Locale.ENGLISH);
        for (String token : STATUS_TOKENS) {
            if (text.contains(token)) return true;
        }
        return false;
    }

    public boolean firstRowMentions(String needle) {
        List<WebElement> rows = driver.findElements(appointmentRows);
        if (rows.isEmpty() || needle == null || needle.isBlank()) return false;
        return rows.get(0).getText().toLowerCase(Locale.ENGLISH)
                .contains(needle.toLowerCase(Locale.ENGLISH));
    }

    /** Returns the visible text of the first appointment row — useful for diagnostic asserts. */
    public String firstRowText() {
        List<WebElement> rows = driver.findElements(appointmentRows);
        return rows.isEmpty() ? "" : rows.get(0).getText();
    }

    /** True if the row text references a typical date pattern (year digits) or a clock time. */
    public boolean firstRowHasDateOrTime() {
        String text = firstRowText();
        if (text.isBlank()) return false;
        return text.matches("(?s).*\\b\\d{4}\\b.*")           // a year somewhere in the row
                || text.matches("(?s).*\\b\\d{1,2}:\\d{2}\\b.*"); // hh:mm time
    }

    /** True if the row exposes a Reschedule or Cancel action button (the plan says 'where applicable'). */
    public boolean firstRowHasActionButton() {
        List<WebElement> rows = driver.findElements(appointmentRows);
        if (rows.isEmpty()) return false;
        WebElement row = rows.get(0);
        try {
            return !row.findElements(By.xpath(
                    ".//*[self::a or self::button or self::mat-button]" +
                    "[contains(normalize-space(.),'Reschedule') " +
                    "or contains(normalize-space(.),'Cancel')]")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Internals ──

    private void scrollAndClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", el);
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
