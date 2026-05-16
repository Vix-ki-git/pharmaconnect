package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.ADOPTION_ANIMALS_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * /adoption/animals. Covers PETZ_TC026 – PETZ_TC031.
 */
public class BrowseAnimalsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Title
    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Find Your Companion')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Animals looking for a loving forever home')]");

    // Top-right CTA
    private final By myApplicationsBtn = By.xpath(
            "//a[normalize-space()='My Applications'] | //button[normalize-space()='My Applications']");

    // Search row
    private final By speciesSelect = By.xpath(
            "//mat-select[@formcontrolname='species' or @name='species']" +
            " | //label[contains(normalize-space(),'Species')]/following::mat-select[1]" +
            " | //*[contains(@class,'mat-mdc-select') and (.//*[contains(.,'Species')])]");
    private final By cityInput     = By.xpath(
            "//input[@formcontrolname='city' or @name='city' or contains(@placeholder,'City or area') or contains(@placeholder,'city or area')]");
    private final By searchButton  = By.xpath(
            "//button[normalize-space()='Search'] | //button[@type='submit' and contains(.,'Search')]");

    // Counter row
    private final By counter = By.xpath("//*[contains(normalize-space(),'animals available for adoption') or contains(normalize-space(),'animal available for adoption')]");

    // Pet cards + the View Profile button on each
    private final By petCards    = By.cssSelector(".pet-card, .animal-card, app-pet-card, [class*='pet-card'], [class*='animal-card']");
    private final By viewProfile = By.xpath("//a[normalize-space()='View Profile'] | //button[normalize-space()='View Profile']");

    // mat-select options
    private final By selectOptions = By.xpath("//mat-option | //*[contains(@class,'mat-mdc-option')]");

    // Empty state
    private final By emptyState = By.xpath(
            "//*[contains(normalize-space(),'No animals found') or contains(normalize-space(),'no animals')]");

    public BrowseAnimalsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(ADOPTION_ANIMALS_URL); }

    // Layout
    public boolean isTitleVisible()             { return isVisible(title); }
    public boolean isSubtitleVisible()          { return isVisible(subtitle); }
    public boolean isMyApplicationsBtnVisible() { return isVisible(myApplicationsBtn); }
    public boolean isSpeciesSelectVisible()     { return isVisible(speciesSelect); }
    public boolean isCityInputVisible()         { return isVisible(cityInput); }
    public boolean isSearchButtonVisible()      { return isVisible(searchButton); }

    public String getCounterText() {
        try { return driver.findElement(counter).getText(); }
        catch (Exception e) { return ""; }
    }

    // Search controls
    public void selectSpecies(String species) {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(speciesSelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectOptions));
        WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//mat-option[normalize-space()='" + species + "']" +
                         " | //*[contains(@class,'mat-mdc-option') and normalize-space()='" + species + "']")));
        try { opt.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
        }
    }

    public void typeCity(String city) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(cityInput));
        el.clear(); el.sendKeys(city);
    }

    public void clickSearch() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickMyApplications() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(myApplicationsBtn));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    // Listing
    public int getPetCardCount() { return driver.findElements(petCards).size(); }

    /** Click View Profile on the first card on the page. */
    public void openFirstPetProfile() {
        List<WebElement> buttons = driver.findElements(viewProfile);
        if (buttons.isEmpty()) {
            throw new IllegalStateException("No 'View Profile' buttons on /adoption/animals.");
        }
        WebElement btn = buttons.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public boolean cardShowsSpeciesChip(String species) {
        // The test cards have a species chip overlay, often uppercase
        String src = driver.getPageSource();
        return src.toUpperCase().contains(species.toUpperCase());
    }

    public boolean cardShowsVaccinatedChip() {
        return driver.getPageSource().contains("Vaccinated");
    }

    public boolean cardShowsNeuteredChip() {
        return driver.getPageSource().contains("Neutered");
    }

    public boolean cardShowsLocationPin() {
        // Locator-only: chips are usually wrapped with a Material location_on icon.
        try {
            return !driver.findElements(By.xpath(
                    "//mat-icon[normalize-space()='location_on' or normalize-space()='place']")).isEmpty();
        } catch (Exception e) { return false; }
    }

    public boolean isEmptyStateVisible() { return isVisible(emptyState); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }
}
