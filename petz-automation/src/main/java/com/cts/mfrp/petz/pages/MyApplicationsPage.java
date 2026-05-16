package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.ADOPTION_MY_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * /adoption/my. Covers PETZ_TC036 – PETZ_TC037.
 */
public class MyApplicationsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'My Applications')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Track your adoption application statuses')]");

    private final By browseAnimalsBtnTop = By.xpath(
            "(//a[normalize-space()='Browse Animals'] | //button[normalize-space()='Browse Animals'])[1]");
    private final By browseAnimalsBtnEmpty = By.xpath(
            "(//a[normalize-space()='Browse Animals'] | //button[normalize-space()='Browse Animals'])[last()]");

    private final By emptyStateTitle = By.xpath("//*[contains(normalize-space(),'No applications yet')]");
    private final By emptyStateBody  = By.xpath("//*[contains(normalize-space(),'Find your perfect companion')]");

    // Application card
    private final By applicationCards = By.cssSelector(
            ".application-card, app-application-card, [class*='application-card'], [class*='app-card']");
    private final By statusBadges     = By.xpath(
            "//*[contains(@class,'status') or contains(@class,'badge')]" +
            "[contains(translate(.,'PENDINGUNDERAPPROVEDREJECTED','pendingunderapprovedrejected'),'pending') " +
            "or contains(translate(.,'PENDINGUNDERAPPROVEDREJECTED','pendingunderapprovedrejected'),'under') " +
            "or contains(translate(.,'PENDINGUNDERAPPROVEDREJECTED','pendingunderapprovedrejected'),'approved') " +
            "or contains(translate(.,'PENDINGUNDERAPPROVEDREJECTED','pendingunderapprovedrejected'),'rejected')]");

    public MyApplicationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(ADOPTION_MY_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }

    public boolean isEmptyStateVisible() {
        return isVisible(emptyStateTitle) && isVisible(emptyStateBody);
    }

    public void clickBrowseAnimalsTop()   { safeClick(browseAnimalsBtnTop); }
    public void clickBrowseAnimalsEmpty() { safeClick(browseAnimalsBtnEmpty); }

    public int getApplicationCardCount() { return driver.findElements(applicationCards).size(); }
    public boolean hasStatusBadge()      { return !driver.findElements(statusBadges).isEmpty(); }

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
