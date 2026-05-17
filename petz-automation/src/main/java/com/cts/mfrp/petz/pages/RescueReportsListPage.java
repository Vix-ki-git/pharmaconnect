package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.RESCUE_URL;

/**
 * /rescue. Covers PETZ_TC047 – PETZ_TC048.
 */
public class RescueReportsListPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Rescue Reports')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),\"reported that need help\") or contains(normalize-space(),'reported that need help')]");

    private final By reportAnimalBtn = By.xpath(
            "//a[contains(normalize-space(),'Report Animal')] | //button[contains(normalize-space(),'Report Animal')]");

    private final By loadingSpinner = By.xpath(
            "//*[contains(normalize-space(),'Loading reports') or contains(@class,'mat-mdc-progress-spinner') or contains(@class,'spinner')]");

    private final By reportCards = By.cssSelector(
            ".report-card, app-report-card, [class*='report-card'], [class*='rescue-card']");

    private final By emptyState = By.xpath(
            "//*[contains(normalize-space(),'No reports') or contains(normalize-space(),\"haven't reported\")]");

    public RescueReportsListPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(RESCUE_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }

    public boolean isReportAnimalBtnVisible() { return isVisible(reportAnimalBtn); }
    public void clickReportAnimal() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(reportAnimalBtn));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    /** Wait until the spinner disappears OR a list / empty state becomes visible. */
    public void waitForLoadingToFinish() {
        try {
            wait.until(d -> {
                boolean spinnerGone = d.findElements(loadingSpinner).isEmpty()
                        || d.findElements(loadingSpinner).stream().noneMatch(WebElement::isDisplayed);
                boolean listOrEmpty = !d.findElements(reportCards).isEmpty()
                        || !d.findElements(emptyState).isEmpty()
                        || d.getPageSource().contains("No reports");
                return spinnerGone || listOrEmpty;
            });
        } catch (Exception ignored) {}
    }

    public int getReportCardCount() { return driver.findElements(reportCards).size(); }
    public boolean isEmptyStateVisible() { return isVisible(emptyState); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }
}
