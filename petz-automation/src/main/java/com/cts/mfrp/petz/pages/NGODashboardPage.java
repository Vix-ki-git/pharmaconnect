package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.NGO_URL;

/**
 * /ngo. Covers PETZ_TC059 – PETZ_TC062.
 */
public class NGODashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'NGO Dashboard')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Manage your rescue operations and adoption listings')]");

    private final By seeQueueLink = By.xpath(
            "//a[contains(normalize-space(),'See Queue')] | //button[contains(normalize-space(),'See Queue')]");
    private final By reviewLink   = By.xpath(
            "//a[contains(normalize-space(),'Review')] | //button[contains(normalize-space(),'Review')]");

    // Quick actions
    private final By manageAnimalsCard = quickActionCard("Manage Animals");
    private final By rescueQueueCard   = quickActionCard("Rescue Queue");
    private final By adoptionAppsCard  = quickActionCard("Adoption Applications");

    public NGODashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(NGO_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }

    public boolean areStatTilesVisible() {
        String src = driver.getPageSource().toUpperCase();
        return src.contains("ANIMALS LISTED") && src.contains("TOTAL RESCUES")
                && src.contains("APPLICATIONS") && src.contains("COMPLETED RESCUES");
    }

    public boolean isRescuePipelineChartVisible() {
        String src = driver.getPageSource();
        return src.contains("Rescue Pipeline")
                && src.contains("Assigned") && src.contains("In Progress") && src.contains("Completed");
    }

    public boolean isApplicationStatusChartVisible() {
        String src = driver.getPageSource();
        return src.contains("Application Status")
                && src.contains("Pending") && src.contains("Approved") && src.contains("Rejected");
    }

    public boolean isSeeQueueLinkVisible() { return isVisible(seeQueueLink); }
    public boolean isReviewLinkVisible()   { return isVisible(reviewLink); }

    public boolean areQuickActionsVisible() {
        String src = driver.getPageSource();
        return src.contains("Manage Animals") && src.contains("Rescue Queue") && src.contains("Adoption Applications");
    }

    public void clickManageAnimals()       { safeClick(manageAnimalsCard); }
    public void clickRescueQueue()         { safeClick(rescueQueueCard); }
    public void clickAdoptionApplications(){ safeClick(adoptionAppsCard); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    private static By quickActionCard(String label) {
        return By.xpath(
                "//*[normalize-space()='" + label + "']/ancestor::a[1]" +
                " | //*[normalize-space()='" + label + "']/ancestor::*[contains(@class,'action-card') or contains(@class,'card')][1]");
    }

    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private void safeClick(By by) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
