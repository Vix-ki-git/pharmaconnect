package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.REGISTER_URL;

/**
 * /auth/register. Covers PETZ_TC013 – PETZ_TC020.
 *
 * <p>Selectors verified against the live deployment. The form has 5 inputs:
 * name, phone, email, password, confirmPassword — the test descriptions
 * mention four; the build also requires Confirm Password before the Create
 * Account button enables, so {@link #fillConfirmPassword} is exposed for the
 * happy-path tests (TC016 / TC017 / TC018).
 */
public class RegisterPage {

    public static final String ROLE_PET_OWNER = "Pet Owner";
    public static final String ROLE_NGO       = "NGO";
    public static final String ROLE_VET       = "Veterinary";

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Inputs — verified formcontrolname values
    private final By fullNameInput        = By.xpath("//input[@formcontrolname='name']");
    private final By phoneInput           = By.xpath("//input[@formcontrolname='phone']");
    private final By emailInput           = By.xpath("//input[@formcontrolname='email']");
    private final By passwordInput        = By.xpath("//input[@formcontrolname='password']");
    private final By confirmPasswordInput = By.xpath("//input[@formcontrolname='confirmPassword']");

    // Account Type — verified mat-select formcontrolname='role'
    private final By accountTypeTrigger = By.xpath("//mat-select[@formcontrolname='role']");
    private final By matOptions         = By.xpath("//mat-option | //*[contains(@class,'mat-mdc-option')]");

    // Submit + footer link
    private final By createAccountBtn = By.xpath("//button[@type='submit' and contains(normalize-space(),'Create')]");
    private final By signInLink       = By.xpath("//a[@routerlink='/auth/login' and contains(normalize-space(),'Sign in')]");

    // Layout
    private final By cardTitle      = By.xpath("//h2[normalize-space()='Create account']");
    private final By leftPanelTitle = By.xpath("//h1[contains(.,'Join') and contains(.,'PETZ')]");

    // Error surface
    private final By errorMessage = By.xpath(
            "//*[contains(@class,'mat-mdc-snack-bar-label') or contains(@class,'snack-bar') " +
            "or contains(@class,'error-message') or (contains(@class,'mat-mdc-form-field-error'))]");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(REGISTER_URL); }

    // ── TC013: layout queries ──
    public boolean isCardTitleVisible()       { return isVisible(cardTitle); }
    public boolean isLeftPanelTitleVisible()  { return isVisible(leftPanelTitle); }
    public boolean leftPanelRoleBulletsVisible() {
        String src = driver.getPageSource();
        return (src.contains("pet owner") || src.contains("Pet Owner"))
                && (src.contains("NGO") || src.contains("Rescue"))
                && (src.contains("Veterinary") || src.contains("veterinary") || src.contains("hospitals") || src.contains("Hospitals"));
    }
    public boolean isSignInLinkVisible() { return isVisible(signInLink); }

    public String getPlaceholder(String fieldName) {
        By target = switch (fieldName.toLowerCase()) {
            case "fullname", "full name", "name" -> fullNameInput;
            case "phone"                          -> phoneInput;
            case "email"                          -> emailInput;
            case "password"                       -> passwordInput;
            case "confirmpassword", "confirm password" -> confirmPasswordInput;
            default -> throw new IllegalArgumentException("unknown field: " + fieldName);
        };
        try { return driver.findElement(target).getAttribute("placeholder"); }
        catch (Exception e) { return ""; }
    }

    // ── TC014 / TC015: form fields ──
    public void fillFullName(String v)        { type(fullNameInput, v); }
    public void fillPhone(String v)           { type(phoneInput, v); }
    public void fillEmail(String v)           { type(emailInput, v); }
    public void fillPassword(String v)        { type(passwordInput, v); }
    public void fillConfirmPassword(String v) { type(confirmPasswordInput, v); }

    public List<String> getAccountTypeOptions() {
        openAccountTypeDropdown();
        List<String> result = driver.findElements(matOptions).stream()
                .map(WebElement::getText).map(String::trim).toList();
        closeAccountTypeDropdown();
        return result;
    }

    public void selectAccountType(String roleHint) {
        openAccountTypeDropdown();
        String lc = roleHint.toLowerCase();
        WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//mat-option[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + lc + "')]")));
        try { opt.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
        }
    }

    public boolean isCreateAccountDisabled() {
        try {
            WebElement btn = driver.findElement(createAccountBtn);
            String disabled = btn.getAttribute("disabled");
            return (disabled != null && !disabled.equals("false"));
        } catch (Exception e) { return true; }
    }

    public boolean isCreateAccountEnabled() { return !isCreateAccountDisabled(); }

    public void clickCreateAccount() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(createAccountBtn));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    // ── TC019: error surface ──
    public boolean hasErrorMessage() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    // ── TC020: navigation ──
    public void clickSignInLink() { safeClick(signInLink); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
    private void openAccountTypeDropdown() {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(accountTypeTrigger));
        try { trigger.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
    }

    private void closeAccountTypeDropdown() {
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
    }

    private void type(By by, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.clear(); el.sendKeys(value);
    }

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
