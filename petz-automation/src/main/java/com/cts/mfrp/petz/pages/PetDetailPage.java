package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * /adoption/animals/{id}. Covers PETZ_TC032 – PETZ_TC035.
 */
public class PetDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Breadcrumb
    private final By allAnimalsLink = By.xpath(
            "//a[contains(normalize-space(),'All Animals')] | //button[contains(normalize-space(),'All Animals')]");

    // Pet card
    private final By petName       = By.xpath("//*[self::h1 or self::h2 or self::h3][ancestor::*[contains(@class,'pet-card') or contains(@class,'animal-card')] or position()<=3]");
    private final By availableChip = By.xpath("//*[normalize-space()='AVAILABLE' or normalize-space()='Available']");
    private final By vaccinatedChip = By.xpath("//*[normalize-space()='Vaccinated']");

    // Apply form
    private final By applyHeading        = By.xpath("//*[contains(normalize-space(),'Apply to Adopt')]");
    private final By whyTextarea         = By.xpath(
            "//textarea[@formcontrolname='why' or @formcontrolname='reason' or @name='reason' " +
            "or contains(@placeholder,'Share your motivation') or contains(@placeholder,'Why do you want')]");
    private final By experienceTextarea  = By.xpath(
            "//textarea[@formcontrolname='experience' or @name='experience' " +
            "or ancestor::*[contains(.,'Previous pet ownership experience')]]");
    private final By submitButton = By.xpath(
            "//button[normalize-space()='Submit Application' or normalize-space()='Apply' or normalize-space()='Submit' " +
            "or normalize-space()='Send Application' or @type='submit']");

    public PetDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    // Breadcrumb
    public boolean isAllAnimalsBreadcrumbVisible() { return isVisible(allAnimalsLink); }

    public void clickAllAnimals() { safeClick(allAnimalsLink); }

    // Layout queries
    public String getPetName() {
        try { return driver.findElement(petName).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public boolean isAvailableChipVisible()  { return isVisible(availableChip); }
    public boolean isVaccinatedChipVisible() { return isVisible(vaccinatedChip); }

    public boolean hasAttributeChips() {
        String src = driver.getPageSource();
        return src.contains("Age") && src.contains("Gender") && src.contains("Species") && src.contains("Breed");
    }

    public boolean hasLocationPin() {
        try {
            return !driver.findElements(By.xpath(
                    "//mat-icon[normalize-space()='location_on' or normalize-space()='place']")).isEmpty();
        } catch (Exception e) { return false; }
    }

    // Apply form
    public boolean isApplyFormVisible() { return isVisible(applyHeading); }

    public void fillWhy(String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(whyTextarea));
        el.clear(); el.sendKeys(text);
    }

    public void fillExperience(String text) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(experienceTextarea));
            el.clear(); el.sendKeys(text);
        } catch (Exception ignored) {
            // Field is optional; ignore if not present in current build.
        }
    }

    public boolean isSubmitDisabled() {
        try {
            WebElement btn = driver.findElement(submitButton);
            String disabled = btn.getAttribute("disabled");
            String aria     = btn.getAttribute("aria-disabled");
            return (disabled != null && !disabled.equals("false"))
                    || "true".equalsIgnoreCase(aria);
        } catch (Exception e) { return true; }
    }

    public void clickSubmit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

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
