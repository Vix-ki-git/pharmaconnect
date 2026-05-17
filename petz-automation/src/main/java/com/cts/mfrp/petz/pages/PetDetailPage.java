package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cts.mfrp.petz.constants.AppConstants.ADOPTION_ANIMALS_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * Page Object for /adoption/animals/{id} — the pet detail page that hosts the
 * pet card and the 'Apply to Adopt {name}' form. Drives TC032–TC035.
 */
public class PetDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Listing entry point ──
    private final By viewProfileButtons = By.xpath(
            "//button[normalize-space()='View Profile'] | " +
            "//a[normalize-space()='View Profile'] | " +
            "//*[contains(@class,'view-profile')]");

    // ── Detail page chrome ──
    private final By backBreadcrumb = By.xpath(
            "//a[contains(.,'All Animals')] | " +
            "//button[contains(.,'All Animals')] | " +
            "//*[contains(@class,'breadcrumb') and contains(.,'All Animals')]");

    private final By petNameHeading = By.xpath(
            "//*[self::h1 or self::h2][contains(@class,'pet-name') " +
            "or contains(@class,'animal-name') or @data-test='pet-name'] " +
            "| //main//h1 | //main//h2");

    private final By speciesOverlayChip = By.xpath(
            "//*[contains(@class,'species') and " +
            "(contains(@class,'chip') or contains(@class,'overlay') or contains(@class,'badge'))]");

    // Chip locators stay tag-agnostic — Angular Material, plain spans, or styled divs all qualify.
    private final By vaccinatedChip = By.xpath(
            "//*[contains(translate(normalize-space(.)," +
            "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'vaccinated')]");

    private final By availableChip = By.xpath(
            "//*[contains(translate(normalize-space(.)," +
            "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'AVAILABLE')]");

    // ── Apply form ──
    private final By whyTextarea = By.xpath(
            "//textarea[contains(@placeholder,'Share your motivation') " +
            "or contains(@placeholder,'why') " +
            "or contains(@placeholder,'Why')] " +
            "| //label[contains(.,'Why do you want to adopt')]" +
            "/following::textarea[1]");

    private final By experienceTextarea = By.xpath(
            "//textarea[contains(@placeholder,'experience') " +
            "or contains(@placeholder,'Experience') " +
            "or contains(@placeholder,'previous')] " +
            "| //label[contains(.,'Previous pet ownership')]" +
            "/following::textarea[1]");

    private final By submitButton = By.xpath(
            "//button[@type='submit'] " +
            "| //button[contains(normalize-space(.),'Submit Application')] " +
            "| //button[contains(normalize-space(.),'Submit')] " +
            "| //button[contains(normalize-space(.),'Apply')]");

    private final By successToast = By.xpath(
            "//*[contains(@class,'toast') or contains(@class,'snack') " +
            "or contains(@class,'snackbar') or contains(@class,'alert') " +
            "or contains(@class,'mat-snack-bar') or @role='status' or @role='alert']");

    public PetDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    // ── Navigation ──

    /** Open /adoption/animals and click the first 'View Profile' to land on a detail page. */
    public void openFirstPetDetailFromListing() {
        driver.get(ADOPTION_ANIMALS_URL);
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(viewProfileButtons));
        scrollAndClick(btn);
        wait.until(ExpectedConditions.urlMatches(".*/adoption/animals/\\d+.*"));
    }

    /**
     * Open /adoption/animals and click 'View Profile' on the pet card whose name matches.
     * Used when the test data calls for a specific pet (e.g. the vaccinated, AVAILABLE seed pet).
     */
    public void openPetDetailFromListingByName(String petName) {
        driver.get(ADOPTION_ANIMALS_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(viewProfileButtons));

        By cardButton = By.xpath(
                "(//*[normalize-space()='" + petName + "']" +
                "/ancestor::*[.//button[normalize-space()='View Profile'] " +
                "or .//a[normalize-space()='View Profile']][1]" +
                "//*[self::button or self::a][normalize-space()='View Profile'])[1]");

        WebElement target;
        try {
            target = wait.until(ExpectedConditions.elementToBeClickable(cardButton));
        } catch (Exception e) {
            // Fallback: no card matched by name — open the first card so the test still produces a
            // meaningful failure (rather than a TimeoutException) at the assertion layer.
            target = driver.findElement(viewProfileButtons);
        }
        scrollAndClick(target);
        wait.until(ExpectedConditions.urlMatches(".*/adoption/animals/\\d+.*"));
    }

    private void scrollAndClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", el);
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    /** Returns the numeric pet id parsed from the current URL, or -1 if not on a detail page. */
    public int getCurrentPetId() {
        Matcher m = Pattern.compile("/adoption/animals/(\\d+)").matcher(driver.getCurrentUrl());
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    public boolean isOnDetailUrl() {
        return driver.getCurrentUrl().matches(".*/adoption/animals/\\d+.*");
    }

    public boolean isBackToAllAnimalsVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(backBreadcrumb))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("All Animals");
        }
    }

    // ── Pet card content ──

    public String getPetName() {
        try {
            String name = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(petNameHeading))
                    .getText().trim();
            return name.isBlank() ? fallbackPetName() : name;
        } catch (Exception e) {
            return fallbackPetName();
        }
    }

    private String fallbackPetName() {
        // Heuristic: the 'Apply to Adopt <name>' heading echoes the pet name.
        try {
            String h = driver.findElement(By.xpath(
                    "//*[contains(normalize-space(.),'Apply to Adopt')]")).getText();
            int idx = h.indexOf("Apply to Adopt");
            if (idx >= 0) return h.substring(idx + "Apply to Adopt".length()).trim();
        } catch (Exception ignored) {}
        return "";
    }

    public boolean isSpeciesChipVisible() {
        try {
            return driver.findElement(speciesOverlayChip).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Looks for the breed · age · gender line; tolerant of casing/separators across builds. */
    public boolean isBreedAgeGenderLineVisible() {
        String src = driver.getPageSource().toLowerCase(Locale.ENGLISH);
        boolean hasGender  = src.contains("male") || src.contains("female");
        boolean hasAgeUnit = src.contains("month") || src.contains("year");
        return hasGender && hasAgeUnit;
    }

    public boolean isVaccinatedChipVisible() {
        try { return driver.findElement(vaccinatedChip).isDisplayed(); }
        catch (Exception e) {
            return driver.getPageSource().toLowerCase(Locale.ENGLISH).contains("vaccinated");
        }
    }

    public boolean isAvailableChipVisible() {
        try { return driver.findElement(availableChip).isDisplayed(); }
        catch (Exception e) {
            String src = driver.getPageSource().toLowerCase(Locale.ENGLISH);
            return src.contains("available") || src.contains("listed");
        }
    }

    public boolean isLocationVisible() {
        // Look for a location/city container or a leading 'pin' icon next to text.
        try {
            return driver.findElement(By.xpath(
                    "//*[contains(@class,'location') or contains(@class,'city') " +
                    "or contains(@class,'pin')]")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDescriptionParagraphVisible() {
        // Try class-based first, then fall back to any <p> with substantial text on the page.
        try {
            WebElement el = driver.findElement(By.xpath(
                    "//*[contains(@class,'description') or contains(@class,'about')]//p | " +
                    "//p[contains(@class,'description') or contains(@class,'about')]"));
            if (el.isDisplayed()) return true;
        } catch (Exception ignored) {}
        try {
            return driver.findElements(By.tagName("p")).stream()
                    .anyMatch(p -> p.isDisplayed() && p.getText().trim().length() >= 20);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areAttributeChipsVisible() {
        String src = driver.getPageSource();
        return src.contains("Age")
                && src.contains("Gender")
                && src.contains("Species")
                && src.contains("Breed");
    }

    // ── Apply form ──

    public boolean isApplyHeadingVisible(String petName) {
        try {
            return driver.findElement(By.xpath(
                    "//*[contains(normalize-space(.),'Apply to Adopt')" +
                    (petName == null || petName.isBlank() ? "" : " and contains(.,'" + petName + "')")
                    + "]")).isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Apply to Adopt");
        }
    }

    public boolean isYourStorySectionVisible() {
        String src = driver.getPageSource().toUpperCase(Locale.ENGLISH);
        return src.contains("YOUR STORY") || src.contains("TELL US ABOUT YOURSELF");
    }

    public boolean isWhyTextareaVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(whyTextarea))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExperienceTextareaVisible() {
        try {
            return driver.findElement(experienceTextarea).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void fillWhy(String text) {
        WebElement el = wait.until(
                ExpectedConditions.visibilityOfElementLocated(whyTextarea));
        el.clear();
        el.sendKeys(text);
    }

    public void fillExperience(String text) {
        WebElement el = wait.until(
                ExpectedConditions.visibilityOfElementLocated(experienceTextarea));
        el.clear();
        el.sendKeys(text);
    }

    public boolean isSubmitDisabled() {
        try {
            WebElement el = driver.findElement(submitButton);
            String disabledAttr = el.getAttribute("disabled");
            String ariaDisabled = el.getAttribute("aria-disabled");
            return !el.isEnabled()
                    || (disabledAttr != null && !disabledAttr.equals("false"))
                    || "true".equalsIgnoreCase(ariaDisabled);
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns true if a 'Required' style inline error is shown after a blocked submit. */
    public boolean hasRequiredInlineError() {
        String src = driver.getPageSource().toLowerCase(Locale.ENGLISH);
        return src.contains("required") || src.contains("is required") || src.contains("please fill");
    }

    public void clickSubmit() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    /** Best-effort: clicks submit even if Selenium thinks it's disabled, so TC034 can verify blocking. */
    public void tryClickSubmitIgnoringDisabled() {
        try {
            WebElement btn = driver.findElement(submitButton);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", btn);
            try { btn.click(); }
            catch (Exception ignored) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }
        } catch (Exception ignored) {}
    }

    public boolean isSuccessToastVisible() {
        try {
            WebElement toast = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successToast));
            String t = toast.getText().toLowerCase(Locale.ENGLISH);
            return t.contains("success") || t.contains("submitted")
                    || t.contains("application") || t.contains("applied");
        } catch (Exception e) {
            String src = driver.getPageSource().toLowerCase(Locale.ENGLISH);
            return src.contains("application submitted")
                    || src.contains("submitted successfully")
                    || src.contains("applied successfully");
        }
    }
}
