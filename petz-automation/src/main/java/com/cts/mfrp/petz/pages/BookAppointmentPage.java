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
import java.util.Locale;

import static com.cts.mfrp.petz.constants.AppConstants.APPOINTMENTS_BOOK_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * Page Object for /appointments/book — drives TC038–TC043.
 * Targets Angular Material primitives: mat-select panels, mat-datepicker grid.
 */
public class BookAppointmentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Page chrome ──
    private final By title = By.xpath(
            "//*[self::h1 or self::h2 or self::h3]" +
            "[normalize-space()='Book Appointment' " +
            "or contains(normalize-space(.),'Book Appointment')]");

    private final By subtitle = By.xpath(
            "//*[contains(normalize-space(.),'Schedule a vet visit for your pet')]");

    private final By myAppointmentsTopRight = By.xpath(
            "(//*[self::a or self::button or self::mat-button " +
            "or self::*[@role='button']][contains(normalize-space(.),'My Appointments')])[1]");

    private final By infoBanner = By.xpath(
            "//*[contains(normalize-space(.)," +
            "\"You'll receive a confirmation once the hospital reviews your request\") " +
            "or contains(normalize-space(.)," +
            "'You will receive a confirmation once the hospital reviews your request')]");

    // ── Section headings ──
    private final By step1Heading = By.xpath(
            "//*[contains(translate(normalize-space(.)," +
            "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
            "'CHOOSE HOSPITAL') and " +
            "contains(translate(normalize-space(.)," +
            "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
            "'DOCTOR')]");

    private final By step2Heading = By.xpath(
            "//*[contains(translate(normalize-space(.)," +
            "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
            "'DATE') and " +
            "contains(translate(normalize-space(.)," +
            "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
            "'TIME')]");

    private final By step3Heading = By.xpath(
            "//*[contains(translate(normalize-space(.)," +
            "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
            "'REASON FOR VISIT')]");

    // ── Form fields (mat-select triggers + inputs) ──
    // Identify each mat-select by its formControlName — these are unique and stable.
    private final By hospitalSelect = By.xpath("//mat-select[@formcontrolname='hospitalId']");
    private final By doctorSelect   = By.xpath("//mat-select[@formcontrolname='doctorId']");
    private final By timeSelect     = By.xpath("//mat-select[@formcontrolname='apptTime']");

    private final By dateInput = By.xpath("//input[@formcontrolname='apptDate']");

    private final By datepickerToggle = By.xpath(
            "(//mat-datepicker-toggle//button " +
            "| //button[contains(@class,'mat-datepicker-toggle')])[1]");

    private final By reasonTextarea = By.xpath("//textarea[@formcontrolname='reason']");

    private final By confirmButton = By.xpath(
            "(//button[contains(normalize-space(.),'Confirm Booking')] " +
            "| //*[self::button or self::mat-button][@type='submit'])[1]");

    private final By cancelButton = By.xpath(
            "(//button[normalize-space()='Cancel'] " +
            "| //button[contains(normalize-space(.),'Cancel')])[1]");

    // ── Overlay primitives (mat-select panel + datepicker calendar) ──
    private static final String MAT_OPTION_XPATH =
            "//div[contains(@class,'mat-select-panel') " +
            "or contains(@class,'mat-mdc-select-panel') " +
            "or contains(@class,'cdk-overlay-pane')]//mat-option";
    private static final By MAT_OPTION       = By.xpath(MAT_OPTION_XPATH);
    private static final By MAT_OPTION_FIRST = By.xpath("(" + MAT_OPTION_XPATH + ")[1]");

    public BookAppointmentPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() {
        driver.get(APPOINTMENTS_BOOK_URL);
        wait.until(ExpectedConditions.urlContains("/appointments/book"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ── Chrome / layout ──

    public boolean isTitleVisible() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(title)).isDisplayed(); }
        catch (Exception e) { return driver.getPageSource().contains("Book Appointment"); }
    }

    public boolean isSubtitleVisible() {
        try { return driver.findElement(subtitle).isDisplayed(); }
        catch (Exception e) {
            return driver.getPageSource().contains("Schedule a vet visit for your pet");
        }
    }

    public boolean isMyAppointmentsTopRightVisible() {
        try { return driver.findElement(myAppointmentsTopRight).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean isStep1Visible() { return present(step1Heading) || sourceHas("CHOOSE HOSPITAL"); }
    public boolean isStep2Visible() { return present(step2Heading) || sourceHas("DATE") && sourceHas("TIME"); }
    public boolean isStep3Visible() { return present(step3Heading) || sourceHas("REASON FOR VISIT"); }

    public boolean isHospitalDropdownVisible() { return present(hospitalSelect); }
    public boolean isDoctorDropdownVisible()   { return present(doctorSelect); }
    public boolean isDateFieldVisible()        { return present(dateInput); }
    public boolean isTimeDropdownVisible()     { return present(timeSelect); }
    public boolean isReasonTextareaVisible()   { return present(reasonTextarea); }
    public boolean isInfoBannerVisible() {
        try { return driver.findElement(infoBanner).isDisplayed(); }
        catch (Exception e) {
            return driver.getPageSource().toLowerCase(Locale.ENGLISH)
                    .contains("receive a confirmation");
        }
    }

    public boolean isCancelButtonVisible() { return present(cancelButton); }

    public boolean isConfirmButtonDisabled() {
        try {
            WebElement btn = driver.findElement(confirmButton);
            String disabledAttr = btn.getAttribute("disabled");
            String ariaDisabled = btn.getAttribute("aria-disabled");
            return !btn.isEnabled()
                    || (disabledAttr != null && !disabledAttr.equals("false"))
                    || "true".equalsIgnoreCase(ariaDisabled);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConfirmButtonEnabled() { return !isConfirmButtonDisabled(); }

    // ── Mat-select interactions ──

    public List<WebElement> openHospitalDropdown() {
        openMatSelect(hospitalSelect);
        return driver.findElements(MAT_OPTION);
    }

    public List<WebElement> openDoctorDropdown() {
        openMatSelect(doctorSelect);
        return driver.findElements(MAT_OPTION);
    }

    public List<WebElement> openTimeDropdown() {
        openMatSelect(timeSelect);
        return driver.findElements(MAT_OPTION);
    }

    public void selectFirstHospital() {
        openHospitalDropdown();
        clickFirstOption();
    }

    public void selectFirstDoctor() {
        openDoctorDropdown();
        clickFirstOption();
    }

    public void selectFirstTime() {
        openTimeDropdown();
        clickFirstOption();
    }

    private void openMatSelect(By selectLocator) {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(selectLocator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", sel);

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

        // Strategy 1: native click on the mat-select host.
        try { sel.click(); } catch (Exception ignored) {}
        if (panelHasOpened(shortWait)) return;

        // Strategy 2: keyboard activation — mat-select opens on ENTER / SPACE when focused.
        try { sel.sendKeys(Keys.ENTER); } catch (Exception ignored) {}
        if (panelHasOpened(shortWait)) return;

        // Strategy 3: JS click on the host (last resort, bypasses any pointer-interception).
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        } catch (Exception ignored) {}
        wait.until(ExpectedConditions.presenceOfElementLocated(MAT_OPTION));
    }

    private boolean panelHasOpened(WebDriverWait shortWait) {
        try {
            shortWait.until(ExpectedConditions.presenceOfElementLocated(MAT_OPTION));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void clickFirstOption() {
        WebElement first = wait.until(
                ExpectedConditions.elementToBeClickable(MAT_OPTION_FIRST));
        scrollAndClick(first);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MAT_OPTION));
    }

    public String getHospitalDisplayValue() { return matSelectText(hospitalSelect); }
    public String getDoctorDisplayValue()   { return matSelectText(doctorSelect); }
    public String getTimeDisplayValue()     { return matSelectText(timeSelect); }

    private String matSelectText(By selectLocator) {
        try {
            return driver.findElement(selectLocator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // ── Date picker ──

    public void openDatepicker() {
        try {
            WebElement toggle = driver.findElement(datepickerToggle);
            scrollAndClick(toggle);
        } catch (Exception e) {
            // Fall back to clicking the input itself.
            WebElement in = driver.findElement(dateInput);
            scrollAndClick(in);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//mat-calendar | //*[contains(@class,'mat-calendar')]")));
    }

    /** Returns the calendar cell for the given date, or null if it isn't currently rendered. */
    public WebElement calendarCellFor(LocalDate date) {
        // Angular Material aria-label format: 'May 16, 2026'
        String aria = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
        try {
            return driver.findElement(By.xpath(
                    "//mat-calendar//*[@aria-label=\"" + aria + "\"]"));
        } catch (Exception e) {
            // Some builds render aria-label without commas or with a different format.
            try {
                return driver.findElement(By.xpath(
                        "//mat-calendar//*[contains(@aria-label,\"" + aria + "\")]"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public boolean isCellDisabled(WebElement cell) {
        if (cell == null) return false;
        String aria   = cell.getAttribute("aria-disabled");
        String clazz  = cell.getAttribute("class");
        return "true".equalsIgnoreCase(aria)
                || (clazz != null && clazz.toLowerCase(Locale.ENGLISH).contains("disabled"));
    }

    public void tryClickCell(WebElement cell) {
        if (cell == null) return;
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", cell);
            cell.click();
        } catch (Exception ignored) {}
    }

    public String getDateInputValue() {
        try { return driver.findElement(dateInput).getAttribute("value"); }
        catch (Exception e) { return ""; }
    }

    /**
     * Picks a date by clicking its calendar cell. The cell must already be visible
     * in the calendar viewport (callers can use openDatepicker() + navigation helpers).
     */
    public void pickDate(LocalDate date) {
        openDatepicker();
        WebElement cell = calendarCellFor(date);
        if (cell == null) {
            // Date is on a future month — advance using the "next month" header button as needed.
            for (int i = 0; i < 24 && cell == null; i++) {
                try {
                    driver.findElement(By.xpath(
                            "//button[contains(@class,'mat-calendar-next-button') " +
                            "or @aria-label='Next month']")).click();
                    cell = calendarCellFor(date);
                } catch (Exception e) {
                    break;
                }
            }
        }
        if (cell != null) {
            cell.click();
        }
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//mat-calendar | //*[contains(@class,'mat-calendar')]")));
    }

    public void closeDatepicker() {
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); }
        catch (Exception ignored) {}
    }

    // ── Reason / submit / cancel ──

    public void fillReason(String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(reasonTextarea));
        el.clear();
        el.sendKeys(text);
        // Trigger Angular's blur-based validation.
        el.sendKeys(Keys.TAB);
    }

    public void clearReason() {
        try {
            WebElement el = driver.findElement(reasonTextarea);
            el.clear();
            el.sendKeys(Keys.TAB);
        } catch (Exception ignored) {}
    }

    public String getReasonValue() {
        try { return driver.findElement(reasonTextarea).getAttribute("value"); }
        catch (Exception e) { return ""; }
    }

    public void clickCancel() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        scrollAndClick(btn);
    }

    // ── Internals ──

    private boolean present(By locator) {
        try { return driver.findElement(locator).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private boolean sourceHas(String needle) {
        return driver.getPageSource().toUpperCase(Locale.ENGLISH).contains(needle.toUpperCase(Locale.ENGLISH));
    }

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
