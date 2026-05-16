package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.APPOINTMENTS_BOOK_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * /appointments/book. Covers PETZ_TC038 – PETZ_TC044.
 */
public class BookAppointmentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Book Appointment')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Schedule a vet visit for your pet')]");

    private final By myAppointmentsBtn = By.xpath(
            "//a[normalize-space()='My Appointments'] | //button[normalize-space()='My Appointments']");

    // STEP 1
    private final By hospitalSelect = By.xpath(
            "//mat-select[@formcontrolname='hospital' or @name='hospital' or @formcontrolname='hospitalId']" +
            " | //label[contains(normalize-space(),'Hospital')]/following::mat-select[1]");
    private final By doctorSelect = By.xpath(
            "//mat-select[@formcontrolname='doctor' or @name='doctor' or @formcontrolname='doctorId']" +
            " | //label[contains(normalize-space(),'Doctor')]/following::mat-select[1]");

    // STEP 2
    private final By dateInput = By.xpath(
            "//input[contains(@class,'mat-datepicker-input') or @formcontrolname='date' or @name='date']" +
            " | //*[contains(normalize-space(),'Appointment Date')]/following::input[1]");
    private final By datepickerToggle = By.xpath(
            "//mat-datepicker-toggle/button | //*[contains(@class,'mat-datepicker-toggle')]/button");
    private final By timeSelect = By.xpath(
            "//mat-select[@formcontrolname='time' or @name='time']" +
            " | //label[contains(normalize-space(),'Preferred Time')]/following::mat-select[1]");

    // STEP 3
    private final By reasonTextarea = By.xpath(
            "//textarea[@formcontrolname='reason' or @name='reason' or contains(@placeholder,'Annual vaccination')]");

    // Footer
    private final By confirmBtn = By.xpath(
            "//button[normalize-space()='Confirm Booking' or normalize-space()='Confirm' or @type='submit']");
    private final By cancelBtn  = By.xpath("//button[normalize-space()='Cancel']");

    // Option overlays (mat-select, mat-calendar)
    private final By matOptions = By.xpath("//mat-option | //*[contains(@class,'mat-mdc-option')]");
    private final By matCalendarCells = By.xpath("//mat-calendar//*[contains(@class,'mat-calendar-body-cell')]");

    public BookAppointmentPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(APPOINTMENTS_BOOK_URL); }

    // Layout
    public boolean isTitleVisible()             { return isVisible(title); }
    public boolean isSubtitleVisible()          { return isVisible(subtitle); }
    public boolean isMyAppointmentsBtnVisible() { return isVisible(myAppointmentsBtn); }
    public boolean isHospitalSelectVisible()    { return isVisible(hospitalSelect); }
    public boolean isDoctorSelectVisible()      { return isVisible(doctorSelect); }
    public boolean isDateInputVisible()         { return isVisible(dateInput); }
    public boolean isTimeSelectVisible()        { return isVisible(timeSelect); }
    public boolean isReasonTextareaVisible()    { return isVisible(reasonTextarea); }
    public boolean isConfirmDisabled() {
        try {
            WebElement btn = driver.findElement(confirmBtn);
            String d = btn.getAttribute("disabled");
            String aria = btn.getAttribute("aria-disabled");
            return (d != null && !d.equals("false")) || "true".equalsIgnoreCase(aria);
        } catch (Exception e) { return true; }
    }
    public boolean isConfirmEnabled() { return !isConfirmDisabled(); }

    public boolean infoBannerVisible() {
        return driver.getPageSource().contains("hospital reviews your request");
    }

    // STEP 1
    public List<String> openHospitalOptions() {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(hospitalSelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
        return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
    }

    public void selectFirstHospital() {
        openHospitalOptions();
        WebElement first = driver.findElements(matOptions).get(0);
        try { first.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", first);
        }
    }

    public List<String> openDoctorOptions() {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(doctorSelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
            return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean isDoctorSelectDisabled() {
        try {
            WebElement el = driver.findElement(doctorSelect);
            String aria = el.getAttribute("aria-disabled");
            return "true".equalsIgnoreCase(aria);
        } catch (Exception e) { return false; }
    }

    public void selectFirstDoctor() {
        openDoctorOptions();
        List<WebElement> opts = driver.findElements(matOptions);
        if (opts.isEmpty()) return;
        try { opts.get(0).click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opts.get(0));
        }
    }

    // STEP 2 — date
    public void openDatepicker() {
        try { safeClick(datepickerToggle); }
        catch (Exception e) { safeClick(dateInput); }
    }

    public void selectDate(LocalDate date) {
        // Easiest reliable path: type ISO/locale date directly into the input.
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(dateInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        input.sendKeys(date.format(DateTimeFormatter.ofPattern("M/d/yyyy")));
        input.sendKeys(Keys.ENTER);
    }

    public boolean isDateCellDisabled(LocalDate date) {
        openDatepicker();
        try {
            String aria = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.ENGLISH));
            WebElement cell = driver.findElement(By.xpath(
                    "//*[contains(@class,'mat-calendar-body-cell') and @aria-label='" + aria + "']"));
            String disabled = cell.getAttribute("aria-disabled");
            return "true".equalsIgnoreCase(disabled);
        } catch (Exception e) {
            return false;
        }
    }

    // STEP 2 — time
    public List<String> openTimeOptions() {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(timeSelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
            return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public void selectFirstTime() {
        openTimeOptions();
        List<WebElement> opts = driver.findElements(matOptions);
        if (opts.isEmpty()) return;
        try { opts.get(0).click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opts.get(0));
        }
    }

    // STEP 3 — reason
    public void typeReason(String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(reasonTextarea));
        el.clear(); el.sendKeys(text);
    }

    // Footer
    public void clickConfirm() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(confirmBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickCancel() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(cancelBtn));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickMyAppointments() { safeClick(myAppointmentsBtn); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
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
