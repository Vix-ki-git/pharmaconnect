package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.HOSPITAL_APPOINTMENTS_URL;

/**
 * /hospital/appointments. Covers PETZ_TC071 – PETZ_TC073.
 */
public class HospitalAppointmentsPage {

    public static final String TAB_ALL       = "ALL";
    public static final String TAB_PENDING   = "PENDING";
    public static final String TAB_CONFIRMED = "CONFIRMED";
    public static final String TAB_COMPLETED = "COMPLETED";
    public static final String TAB_CANCELLED = "CANCELLED";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Appointments')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Manage incoming and scheduled vet appointments')]");
    private final By totalChip = By.xpath("//*[contains(normalize-space(),'total') and (contains(@class,'chip') or contains(@class,'badge'))]");

    private final By weekRangeHeader = By.xpath(
            "//*[contains(normalize-space(),'–') or contains(normalize-space(),'-')]" +
            "[contains(translate(normalize-space(),'JANFEBMARAPRMAYJUNJULAUGSEPOCTNOVDEC','janfebmaraprmayjunjulaugsepoctnovdec')," +
            "'jan') or contains(translate(normalize-space(),'JANFEBMARAPRMAYJUNJULAUGSEPOCTNOVDEC','janfebmaraprmayjunjulaugsepoctnovdec'),'feb')]");
    private final By allWeekButton = By.xpath("//button[normalize-space()='All Week']");
    private final By nextWeekChevron = By.xpath(
            "//button[.//mat-icon[normalize-space()='chevron_right' or normalize-space()='keyboard_arrow_right']]");
    private final By prevWeekChevron = By.xpath(
            "//button[.//mat-icon[normalize-space()='chevron_left' or normalize-space()='keyboard_arrow_left']]");

    private final By searchInput = By.xpath(
            "//input[contains(@placeholder,'Search') and (contains(@placeholder,'pet') or contains(@placeholder,'owner') or contains(@placeholder,'doctor'))]");
    private final By statusFilter = By.xpath(
            "//mat-select[@formcontrolname='status' or @name='statusFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Status')][1]]");
    private final By sortFilter = By.xpath(
            "//mat-select[@formcontrolname='sort' or @name='sortFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Sort')][1]]");

    private final By emptyStateTitle = By.xpath("//*[normalize-space()='No appointments']");

    public HospitalAppointmentsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(HOSPITAL_APPOINTMENTS_URL); }

    // Layout
    public boolean isTitleVisible()        { return isVisible(title); }
    public boolean isSubtitleVisible()     { return isVisible(subtitle); }
    public boolean isTotalChipVisible()    { return isVisible(totalChip); }
    public boolean isWeekRangeVisible()    { return isVisible(weekRangeHeader); }
    public boolean isAllWeekButtonVisible(){ return isVisible(allWeekButton); }
    public boolean isSearchInputVisible()  { return isVisible(searchInput); }
    public boolean isStatusFilterVisible() { return isVisible(statusFilter); }
    public boolean isSortFilterVisible()   { return isVisible(sortFilter); }
    public boolean isEmptyStateVisible()   { return isVisible(emptyStateTitle); }

    public boolean hasAllFiveStatusTabs() {
        String src = driver.getPageSource().toUpperCase();
        return src.contains("ALL") && src.contains("PENDING") && src.contains("CONFIRMED")
                && src.contains("COMPLETED") && src.contains("CANCELLED");
    }

    public boolean isTabSelected(String tab) {
        try {
            String xpath = "//*[contains(@class,'mat-mdc-tab') or @role='tab']" +
                    "[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" +
                    tab.toLowerCase() + "')]";
            WebElement el = driver.findElement(By.xpath(xpath));
            String aria = el.getAttribute("aria-selected");
            String cls  = el.getAttribute("class");
            return "true".equalsIgnoreCase(aria) || (cls != null && cls.contains("active"));
        } catch (Exception e) { return false; }
    }

    public void clickTab(String tab) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[(contains(@class,'mat-mdc-tab') or @role='tab')" +
                        " and contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + tab.toLowerCase() + "')]")));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    public void clickNextWeek() { safeClick(nextWeekChevron); }
    public void clickPrevWeek() { safeClick(prevWeekChevron); }
    public void clickAllWeek()  { safeClick(allWeekButton); }

    public String getWeekRangeText() {
        try { return driver.findElement(weekRangeHeader).getText(); }
        catch (Exception e) { return ""; }
    }

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
