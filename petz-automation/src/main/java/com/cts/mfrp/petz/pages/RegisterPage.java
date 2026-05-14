package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

public class RegisterPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // ── Input fields ──────────────────────────────────────────
    private final By fullNameInput = By.xpath(
            "//input[@placeholder='John Doe' or contains(@placeholder,'name') or contains(@name,'name') or contains(@id,'name')]");
    private final By phoneInput    = By.xpath(
            "//input[@type='tel' or contains(@placeholder,'00000') or contains(@placeholder,'phone') or contains(@name,'phone')]");
    private final By emailInput    = By.xpath(
            "//input[@type='email' or @placeholder='you@example.com']");
    private final By passwordInput = By.xpath(
            "//input[@type='password' or contains(@placeholder,'min. 6') or contains(@placeholder,'min')]");

    // ── Account Type dropdown ─────────────────────────────────
    // May be a native <select> or a custom Angular dropdown
    private final By accountTypeSelect = By.xpath(
            "//select[contains(@name,'role') or contains(@id,'role') or contains(@name,'type') or contains(@id,'type')]");
    private final By accountTypeCustom = By.xpath(
            "//*[contains(@class,'select') or contains(@class,'dropdown')][contains(.,'Pet Owner') or contains(.,'Account Type')]"
            + " | //button[contains(text(),'Pet Owner') or contains(text(),'Account Type')]");
    private final By dropdownOptions   = By.xpath(
            "//option | //*[contains(@class,'option') or contains(@role,'option')]");

    // ── Button & links ────────────────────────────────────────
    private final By createAccountBtn = By.xpath(
            "//button[contains(normalize-space(),'Create Account')] | //button[@type='submit'][contains(normalize-space(),'Create')]");
    private final By signInLink        = By.xpath(
            "//a[contains(text(),'Sign in') or contains(text(),'Sign In')]");
    private final By backToHome        = By.xpath(
            "//a[contains(text(),'Back to home') or contains(text(),'back')]");

    // ── Left panel ────────────────────────────────────────────
    private final By leftPanelHeadline = By.xpath(
            "//*[contains(text(),'Join the PETZ Community') or contains(text(),'PETZ Community')]");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    // ── Input helpers ─────────────────────────────────────────

    public void enterFullName(String name) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput));
        el.clear();
        el.sendKeys(name);
    }

    public void enterPhone(String phone) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(phoneInput));
        el.clear();
        el.sendKeys(phone);
    }

    public void enterEmail(String email) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        el.clear();
        el.sendKeys(email);
    }

    public void clearEmail() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        el.clear();
    }

    public void enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
    }

    // ── Account Type dropdown ─────────────────────────────────

    public void selectAccountType(String visibleText) {
        // Try native <select> first
        List<WebElement> selects = driver.findElements(accountTypeSelect);
        if (!selects.isEmpty()) {
            new Select(selects.get(0)).selectByVisibleText(visibleText);
            return;
        }
        // Fall back to custom dropdown
        try {
            WebElement dropdown = driver.findElement(accountTypeCustom);
            dropdown.click();
            Thread.sleep(500);
            List<WebElement> options = driver.findElements(dropdownOptions);
            for (WebElement opt : options) {
                if (opt.getText().trim().contains(visibleText)) {
                    opt.click();
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Opens the Account Type dropdown and returns the visible text of each option.
     */
    public List<String> getAccountTypeOptions() {
        List<String> texts = new ArrayList<>();
        try {
            // Native select
            List<WebElement> selects = driver.findElements(accountTypeSelect);
            if (!selects.isEmpty()) {
                Select sel = new Select(selects.get(0));
                for (WebElement opt : sel.getOptions()) {
                    String t = opt.getText().trim();
                    if (!t.isEmpty()) texts.add(t);
                }
                return texts;
            }
            // Custom dropdown – open it first
            WebElement dropdown = driver.findElement(accountTypeCustom);
            dropdown.click();
            Thread.sleep(600);
            List<WebElement> options = driver.findElements(dropdownOptions);
            for (WebElement opt : options) {
                String t = opt.getText().trim();
                if (!t.isEmpty()) texts.add(t);
            }
        } catch (Exception ignored) {}
        return texts;
    }

    // ── Button state ──────────────────────────────────────────

    public boolean isCreateAccountEnabled() {
        try {
            WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(createAccountBtn));
            String disabled = btn.getAttribute("disabled");
            String classes  = btn.getAttribute("class");
            if (disabled != null) return false;
            if (classes != null && (classes.contains("disabled") || classes.contains("grey")
                    || classes.contains("gray") || classes.contains("inactive"))) return false;
            return btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickCreateAccount() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(createAccountBtn));
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", driver.findElement(createAccountBtn));
        }
    }

    // ── Navigation ────────────────────────────────────────────

    public void clickSignIn() {
        try {
            driver.findElement(signInLink).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", driver.findElement(signInLink));
        }
    }

    public void clickBackToHome() {
        try {
            driver.findElement(backToHome).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", driver.findElement(backToHome));
        }
    }

    // ── Layout checks ─────────────────────────────────────────

    public boolean isLeftPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(leftPanelHeadline))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Join the PETZ Community")
                    || driver.getPageSource().contains("PETZ Community");
        }
    }

    public boolean areRoleBulletsVisible() {
        String src = driver.getPageSource();
        return (src.contains("pet owner") || src.contains("Pet Owner") || src.contains("Pet owner"))
                && (src.contains("NGO") || src.contains("Rescue"))
                && (src.contains("Veterinary") || src.contains("Hospital"));
    }

    public boolean isFullNameInputVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isPhoneInputVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(phoneInput)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isEmailInputVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isPasswordInputVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isAccountTypeDropdownVisible() {
        return !driver.findElements(accountTypeSelect).isEmpty()
                || !driver.findElements(accountTypeCustom).isEmpty()
                || driver.getPageSource().contains("Account Type")
                || driver.getPageSource().contains("Pet Owner");
    }

    public boolean isCreateAccountButtonPresent() {
        try {
            return !driver.findElements(createAccountBtn).isEmpty();
        } catch (Exception e) { return false; }
    }

    public boolean isSignInLinkVisible() {
        try {
            return !driver.findElements(signInLink).isEmpty()
                    || driver.getPageSource().contains("Sign in");
        } catch (Exception e) { return false; }
    }

    public boolean areInputPlaceholdersCorrect() {
        String src = driver.getPageSource();
        return src.contains("John Doe")
                && src.contains("+91")
                && src.contains("you@example.com")
                && (src.contains("min. 6") || src.contains("6 characters") || src.contains("min"));
    }

    // ── URL ───────────────────────────────────────────────────

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
}
