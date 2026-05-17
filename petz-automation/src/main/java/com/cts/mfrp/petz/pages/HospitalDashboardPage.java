package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.HOSPITAL_URL;

/**
 * /hospital. Covers PETZ_TC070.
 */
public class HospitalDashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Hospital Dashboard')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'operation overview') or contains(normalize-space(),'Welcome back')]");
    private final By datePill = By.xpath(
            "//*[contains(@class,'date') or contains(@class,'pill') or contains(@class,'chip')]" +
            "[contains(normalize-space(),'Mon,') or contains(normalize-space(),'Tue,') or contains(normalize-space(),'Wed,') " +
            "or contains(normalize-space(),'Thu,') or contains(normalize-space(),'Fri,') or contains(normalize-space(),'Sat,') " +
            "or contains(normalize-space(),'Sun,')]");

    private final By appointmentsNavCard = By.xpath(
            "//*[contains(normalize-space(),'View and manage all scheduled visits')]/ancestor::a[1]" +
            " | //*[contains(normalize-space(),'View and manage all scheduled visits')]/ancestor::*[contains(@class,'card')][1]");
    private final By manageDoctorsNavCard = By.xpath(
            "//*[contains(normalize-space(),'Add, view and manage medical staff')]/ancestor::a[1]" +
            " | //*[contains(normalize-space(),'Add, view and manage medical staff')]/ancestor::*[contains(@class,'card')][1]");

    public HospitalDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(HOSPITAL_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }
    public boolean isDatePillVisible() { return isVisible(datePill); }

    public boolean areSixStatTilesVisible() {
        String src = driver.getPageSource().toUpperCase();
        return src.contains("TODAY") && src.contains("DOCTORS") && src.contains("PENDING")
                && src.contains("COMPLETED") && src.contains("CANCELLED") && src.contains("TOTAL");
    }

    public boolean areInfoCardsVisible() {
        String src = driver.getPageSource();
        return (src.contains("APPOINTMENT STATUS") || src.contains("Appointment Status"))
                && (src.contains("THIS WEEK") || src.contains("This Week"))
                && (src.contains("DOCTOR CAPACITY") || src.contains("Doctor Capacity"));
    }

    public boolean areNavigationCardsVisible() {
        String src = driver.getPageSource();
        return src.contains("View and manage all scheduled visits")
                && src.contains("Add, view and manage medical staff");
    }

    public void clickAppointmentsNavCard()   { safeClick(appointmentsNavCard); }
    public void clickManageDoctorsNavCard()  { safeClick(manageDoctorsNavCard); }

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
