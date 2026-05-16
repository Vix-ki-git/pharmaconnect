package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.APPOINTMENTS_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * /appointments. Covers PETZ_TC045 – PETZ_TC046.
 */
public class MyAppointmentsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'My Appointments')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Track all your scheduled vet visits')]");

    private final By bookAppointmentBtnTop = By.xpath(
            "(//a[contains(normalize-space(),'Book Appointment')] | //button[contains(normalize-space(),'Book Appointment')])[1]");
    private final By bookNowBtnEmpty = By.xpath(
            "//a[contains(normalize-space(),'Book Now')] | //button[contains(normalize-space(),'Book Now')]");

    private final By emptyStateTitle = By.xpath("//*[normalize-space()='No appointments']");
    private final By emptyStateBody  = By.xpath("//*[contains(normalize-space(),'You haven') and contains(normalize-space(),'booked any vet visits')]");

    private final By appointmentRows = By.cssSelector(
            ".appointment-card, .appointment-row, app-appointment-card, [class*='appointment-card'], [class*='appt-card']");

    private final By statusBadges = By.xpath(
            "//*[contains(@class,'status') or contains(@class,'badge')]" +
            "[contains(translate(.,'PENDINGCONFIRMEDCOMPLETEDCANCELLED','pendingconfirmedcompletedcancelled'),'pending') " +
            "or contains(translate(.,'PENDINGCONFIRMEDCOMPLETEDCANCELLED','pendingconfirmedcompletedcancelled'),'confirmed') " +
            "or contains(translate(.,'PENDINGCONFIRMEDCOMPLETEDCANCELLED','pendingconfirmedcompletedcancelled'),'completed') " +
            "or contains(translate(.,'PENDINGCONFIRMEDCOMPLETEDCANCELLED','pendingconfirmedcompletedcancelled'),'cancelled')]");

    public MyAppointmentsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(APPOINTMENTS_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }

    public boolean isEmptyStateVisible() {
        return isVisible(emptyStateTitle) && isVisible(emptyStateBody);
    }

    public void clickBookAppointmentTop() { safeClick(bookAppointmentBtnTop); }
    public void clickBookNowEmpty()       { safeClick(bookNowBtnEmpty); }

    public int getAppointmentRowCount() { return driver.findElements(appointmentRows).size(); }
    public boolean hasStatusBadge()     { return !driver.findElements(statusBadges).isEmpty(); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private void safeClick(By by) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
