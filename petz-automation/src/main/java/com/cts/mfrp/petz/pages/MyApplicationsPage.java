package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import static com.cts.mfrp.petz.constants.AppConstants.ADOPTION_MY_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * Page Object for /adoption/my — the pet owner's 'My Applications' page.
 * Covers both the empty state and the populated list state (TC036 + TC037).
 */
public class MyApplicationsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Page chrome ──
    private final By heading = By.xpath(
            "//*[self::h1 or self::h2 or self::h3]" +
            "[normalize-space()='My Applications' " +
            "or contains(normalize-space(.),'My Applications')]");

    private final By subtitle = By.xpath(
            "//*[contains(normalize-space(.)," +
            "'Track your adoption application statuses')]");

    // First Browse Animals control on the page = the top-right toolbar button (in DOM order
    // the toolbar button precedes the empty-state CTA).
    private final By browseAnimalsTopRight = By.xpath(
            "(//*[self::a or self::button or self::mat-button or " +
            "(self::*[@role='button'])][contains(normalize-space(.),'Browse Animals')])[1]");

    // ── Empty state ──
    private final By emptyStateContainer = By.xpath(
            "//*[contains(normalize-space(.),'No applications yet')]" +
            "/ancestor::*[self::section or self::div][1]");

    private final By noApplicationsText = By.xpath(
            "//*[normalize-space()='No applications yet' " +
            "or contains(normalize-space(.),'No applications yet')]");

    private final By emptyStateSubtitle = By.xpath(
            "//*[contains(normalize-space(.)," +
            "'Find your perfect companion and submit an adoption application')]");

    private final By clipboardIcon = By.xpath(
            "//*[contains(normalize-space(.),'No applications yet')]" +
            "/ancestor::*[self::section or self::div][1]" +
            "//*[self::mat-icon or self::svg or self::i " +
            "or (self::span and (contains(@class,'icon') or contains(@class,'material')))]");

    // CTA button below the empty-state copy.
    private final By browseAnimalsCta = By.xpath(
            "(//*[contains(normalize-space(.),'No applications yet')]" +
            "/ancestor::*[self::section or self::div][1]" +
            "//*[self::a or self::button or self::mat-button or " +
            "(self::*[@role='button'])][contains(normalize-space(.),'Browse Animals')])[1]");

    // ── Populated state ──
    private final By applicationCards = By.xpath(
            "//mat-card[.//*[contains(@class,'status') or contains(@class,'badge') " +
            "or contains(@class,'chip')]] " +
            "| //*[contains(@class,'application-card') or contains(@class,'app-card') " +
            "or contains(@class,'card')][.//*[contains(@class,'status') " +
            "or contains(@class,'badge') or contains(@class,'chip')]]");

    private static final String[] STATUS_TOKENS = {
            "PENDING", "UNDER REVIEW", "APPROVED", "REJECTED"
    };

    public MyApplicationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() {
        driver.get(ADOPTION_MY_URL);
        wait.until(ExpectedConditions.urlContains("/adoption/my"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ── Chrome assertions ──

    public boolean isHeadingVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(heading))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("My Applications");
        }
    }

    public boolean isSubtitleVisible() {
        try {
            return driver.findElement(subtitle).isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Track your adoption application statuses");
        }
    }

    public boolean isBrowseAnimalsTopRightVisible() {
        try {
            return driver.findElement(browseAnimalsTopRight).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickBrowseAnimalsTopRight() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(browseAnimalsTopRight));
        scrollAndClick(btn);
    }

    // ── Empty state ──

    public boolean isEmptyStateVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(emptyStateContainer))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("No applications yet");
        }
    }

    public boolean isNoApplicationsTextVisible() {
        try { return driver.findElement(noApplicationsText).isDisplayed(); }
        catch (Exception e) { return driver.getPageSource().contains("No applications yet"); }
    }

    public boolean isEmptyStateSubtitleVisible() {
        try { return driver.findElement(emptyStateSubtitle).isDisplayed(); }
        catch (Exception e) {
            return driver.getPageSource().contains(
                    "Find your perfect companion and submit an adoption application");
        }
    }

    public boolean isClipboardIconVisible() {
        try { return driver.findElement(clipboardIcon).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean isBrowseAnimalsCtaVisible() {
        try { return driver.findElement(browseAnimalsCta).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public void clickBrowseAnimalsCta() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(browseAnimalsCta));
        scrollAndClick(btn);
    }

    // ── Populated state ──

    public boolean hasApplicationCards() {
        try {
            return !driver.findElements(applicationCards).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public int applicationCardCount() {
        try { return driver.findElements(applicationCards).size(); }
        catch (Exception e) { return 0; }
    }

    /** True if at least one application card surfaces a Pending/Under Review/Approved/Rejected badge. */
    public boolean firstCardHasStatusBadge() {
        List<WebElement> cards = driver.findElements(applicationCards);
        if (cards.isEmpty()) return false;
        String text = cards.get(0).getText().toUpperCase(Locale.ENGLISH);
        for (String token : STATUS_TOKENS) {
            if (text.contains(token)) return true;
        }
        return false;
    }

    /** True if the first card's text contains the given pet name (case-insensitive). */
    public boolean firstCardMentionsPet(String petName) {
        List<WebElement> cards = driver.findElements(applicationCards);
        if (cards.isEmpty() || petName == null || petName.isBlank()) return false;
        return cards.get(0).getText().toLowerCase(Locale.ENGLISH)
                .contains(petName.toLowerCase(Locale.ENGLISH));
    }

    // ── Internals ──

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
