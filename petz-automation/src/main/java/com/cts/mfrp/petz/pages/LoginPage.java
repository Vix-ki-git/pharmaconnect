package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

public class LoginPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // ── Inputs ────────────────────────────────────────────────
    private final By emailInput    = By.xpath(
            "//input[@type='email'] | //input[@placeholder='you@example.com']");
    private final By passwordInput = By.xpath(
            "//input[@type='password'] | //input[contains(@placeholder,'password') or contains(@placeholder,'min')]");

    // ── Buttons ───────────────────────────────────────────────
    private final By signInButton = By.xpath(
            "//button[contains(normalize-space(),'Sign In')] | //button[@type='submit'][contains(normalize-space(),'Sign')]");
    private final By eyeToggle    = By.xpath(
            "//button[contains(@class,'eye') or contains(@aria-label,'password')]"
            + " | //input[@type='password']/following-sibling::button"
            + " | //div[.//input[@type='password' or @type='text']]//button[.//svg or contains(@class,'toggle')]");

    // ── Navigation links ──────────────────────────────────────
    private final By backToHome   = By.xpath(
            "//a[contains(text(),'Back to home') or contains(text(),'back')]");
    private final By createOneLink = By.xpath(
            "//a[contains(text(),'Create one') or contains(text(),'create one')]"
            + " | //a[contains(@href,'register') and not(contains(text(),'Sign Up'))]");

    // ── Left panel ────────────────────────────────────────────
    private final By leftPanelHeadline = By.xpath(
            "//*[contains(text(),'Animal Welfare Platform') or contains(text(),'Welfare Platform')]");

    // ── Error / Toast ─────────────────────────────────────────
    private final By errorMessage = By.xpath(
            "//*[contains(@class,'error') or contains(@class,'toast') or contains(@class,'alert')]"
            + " | //*[contains(text(),'Invalid') or contains(text(),'incorrect') or contains(text(),'wrong')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    // ── Input helpers ─────────────────────────────────────────

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

    public void clearPassword() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
    }

    // ── Button state ──────────────────────────────────────────

    public boolean isSignInEnabled() {
        try {
            WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(signInButton));
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

    public void clickSignIn() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(signInButton));
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", driver.findElement(signInButton));
        }
    }

    // ── Eye toggle ────────────────────────────────────────────

    public String getPasswordFieldType() {
        try {
            // After toggling, the input type may change to 'text'
            List<WebElement> inputs = driver.findElements(
                    By.xpath("//input[@type='password' or @type='text'][contains(@placeholder,'') or @name]"));
            for (WebElement inp : inputs) {
                String type = inp.getAttribute("type");
                if ("password".equals(type) || "text".equals(type)) {
                    String placeholder = inp.getAttribute("placeholder");
                    if (placeholder != null && (placeholder.toLowerCase().contains("min")
                            || placeholder.toLowerCase().contains("password"))) {
                        return type;
                    }
                }
            }
            // fallback: check presence of password-type input
            List<WebElement> pwdInputs = driver.findElements(By.xpath("//input[@type='password']"));
            return pwdInputs.isEmpty() ? "text" : "password";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public void clickEyeToggle() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(eyeToggle));
            btn.click();
        } catch (Exception e) {
            // try JS click
            try {
                List<WebElement> btns = driver.findElements(eyeToggle);
                if (!btns.isEmpty()) {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();", btns.get(0));
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean isEyeTogglePresent() {
        try {
            return !driver.findElements(eyeToggle).isEmpty()
                    || driver.getPageSource().contains("eye")
                    || driver.getPageSource().contains("visibility");
        } catch (Exception e) {
            return false;
        }
    }

    // ── Navigation ────────────────────────────────────────────

    public void clickBackToHome() {
        try {
            driver.findElement(backToHome).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", driver.findElement(backToHome));
        }
    }

    public void clickCreateOne() {
        try {
            driver.findElement(createOneLink).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", driver.findElement(createOneLink));
        }
    }

    // ── Layout checks ─────────────────────────────────────────

    public boolean isLeftPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(leftPanelHeadline))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Animal Welfare Platform");
        }
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

    public boolean isSignInButtonPresent() {
        try {
            return !driver.findElements(signInButton).isEmpty();
        } catch (Exception e) { return false; }
    }

    public boolean isCreateOneLinkVisible() {
        try {
            return !driver.findElements(createOneLink).isEmpty()
                    || driver.getPageSource().contains("Create one");
        } catch (Exception e) { return false; }
    }

    public boolean isLeftPanelFeatureBulletsVisible() {
        String src = driver.getPageSource();
        return src.contains("Manage your pets")
                || src.contains("Manage")
                && src.contains("Report")
                && src.contains("Adopt")
                && src.contains("Book");
    }

    // ── Error handling ────────────────────────────────────────

    public boolean isErrorDisplayed() {
        try {
            List<WebElement> errors = driver.findElements(errorMessage);
            for (WebElement el : errors) {
                if (el.isDisplayed()) return true;
            }
            String src = driver.getPageSource().toLowerCase();
            return src.contains("invalid") || src.contains("incorrect")
                    || src.contains("wrong credentials") || src.contains("failed");
        } catch (Exception e) { return false; }
    }

    // ── URL ───────────────────────────────────────────────────

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
}
