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
import static com.cts.mfrp.petz.constants.AppConstants.NGO_RESCUES_URL;

/**
 * /ngo/rescues. Covers PETZ_TC068 – PETZ_TC069.
 */
public class NGORescueQueuePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Rescue Queue')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Rescue assignments waiting for your response')]");

    private final By searchInput = By.xpath(
            "//input[contains(@placeholder,'Search') and (contains(@placeholder,'animal') or contains(@placeholder,'address') or contains(@placeholder,'type'))]");

    private final By statusFilter  = By.xpath(
            "//mat-select[@formcontrolname='status' or @name='statusFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Status')][1]]");
    private final By urgencyFilter = By.xpath(
            "//mat-select[@formcontrolname='urgency' or @name='urgencyFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Urgency')][1]]");
    private final By sortFilter    = By.xpath(
            "//mat-select[@formcontrolname='sort' or @name='sortFilter']" +
            " | //*[contains(@class,'mat-mdc-select') and ancestor::*[contains(.,'Sort')][1]]");

    private final By emptyStateTitle = By.xpath("//*[contains(normalize-space(),'Queue is clear')]");
    private final By rescueCards     = By.cssSelector(
            ".rescue-card, app-rescue-card, [class*='rescue-card'], [class*='queue-card']");

    private final By acceptBtn  = By.xpath("//button[normalize-space()='Accept']");
    private final By declineBtn = By.xpath("//button[normalize-space()='Decline']");

    private final By matOptions = By.xpath("//mat-option | //*[contains(@class,'mat-mdc-option')]");

    public NGORescueQueuePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(NGO_RESCUES_URL); }

    public boolean isTitleVisible()       { return isVisible(title); }
    public boolean isSubtitleVisible()    { return isVisible(subtitle); }
    public boolean isSearchInputVisible() { return isVisible(searchInput); }
    public boolean areFiltersVisible() {
        return isVisible(statusFilter) && isVisible(urgencyFilter) && isVisible(sortFilter);
    }
    public boolean isEmptyStateVisible() { return isVisible(emptyStateTitle); }

    public int getRescueCardCount() { return driver.findElements(rescueCards).size(); }

    public boolean firstCardHasAcceptDecline() {
        return !driver.findElements(acceptBtn).isEmpty() && !driver.findElements(declineBtn).isEmpty();
    }

    public void clickAcceptOnFirstCard() {
        List<WebElement> btns = driver.findElements(acceptBtn);
        if (btns.isEmpty()) throw new IllegalStateException("No Accept button visible.");
        WebElement btn = btns.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickDeclineOnFirstCard() {
        List<WebElement> btns = driver.findElements(declineBtn);
        if (btns.isEmpty()) throw new IllegalStateException("No Decline button visible.");
        WebElement btn = btns.get(0);
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public List<String> openStatusOptions()  { return openOptions(statusFilter); }
    public List<String> openUrgencyOptions() { return openOptions(urgencyFilter); }
    public List<String> openSortOptions()    { return openOptions(sortFilter); }

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
