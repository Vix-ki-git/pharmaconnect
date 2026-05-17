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
import static com.cts.mfrp.petz.constants.AppConstants.NGO_APPLICATIONS_URL;

/**
 * /ngo/applications. Covers PETZ_TC066 – PETZ_TC067.
 */
public class NGOApplicationsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Adoption Applications')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Review and process incoming adoption requests')]");

    private final By searchInput = By.xpath(
            "//input[contains(@placeholder,'Search') and (contains(@placeholder,'animal') or contains(@placeholder,'reason'))]");
    private final By statusFilter = By.xpath(
            "//mat-select[@formcontrolname='status' or @name='statusFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Status')][1]]");
    private final By sortFilter = By.xpath(
            "//mat-select[@formcontrolname='sort' or @name='sortFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Sort')][1]]");

    private final By emptyStateTitle = By.xpath("//*[contains(normalize-space(),'No applications yet')]");
    private final By applicationCards = By.cssSelector(
            ".application-card, app-application-card, [class*='application-card']");

    private final By matOptions = By.xpath("//mat-option | //*[contains(@class,'mat-mdc-option')]");

    public NGOApplicationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(NGO_APPLICATIONS_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }
    public boolean isSearchInputVisible() { return isVisible(searchInput); }
    public boolean isStatusFilterVisible(){ return isVisible(statusFilter); }
    public boolean isSortFilterVisible()  { return isVisible(sortFilter); }
    public boolean isEmptyStateVisible()  { return isVisible(emptyStateTitle); }

    public int getApplicationCardCount() { return driver.findElements(applicationCards).size(); }

    public List<String> openStatusOptions() { return openOptions(statusFilter); }
    public List<String> openSortOptions()   { return openOptions(sortFilter); }

    public void typeSearch(String query) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        el.clear(); el.sendKeys(query);
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    private List<String> openOptions(By trigger) {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(trigger));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
            return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            try { driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE); } catch (Exception ignored) {}
        }
    }

    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }
}
