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
 * The top header shown on every authenticated page. Covers PETZ_TC085 + PETZ_TC087.
 */
public class HeaderComponent {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By logo            = By.xpath(
            "//header//*[normalize-space()='PETZ' or normalize-space()='Petz']" +
            " | //*[contains(@class,'app-header') or contains(@class,'topbar') or self::header]//*[normalize-space()='PETZ' or normalize-space()='Petz']");
    private final By hamburgerToggle = By.xpath(
            "//button[.//mat-icon[normalize-space()='menu' or normalize-space()='dehaze']]" +
            " | //button[@aria-label='Toggle sidebar' or @aria-label='menu']");
    private final By platformTitle   = By.xpath("//*[contains(normalize-space(),'Animal Welfare Platform')]");
    private final By bellIcon        = By.xpath(
            "//button[@title='Notifications']" +
            " | //button[.//mat-icon[normalize-space()='notifications' or normalize-space()='notifications_active' or normalize-space()='notifications_none']]");
    private final By avatar          = By.xpath(
            "//header//*[contains(@class,'avatar') or contains(@class,'initial-circle')]" +
            " | //*[contains(@class,'user-avatar')]");

    public HeaderComponent(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public boolean isLogoVisible()         { return isVisible(logo); }
    public boolean isHamburgerVisible()    { return isVisible(hamburgerToggle); }
    public boolean isPlatformTitleVisible(){ return isVisible(platformTitle); }
    public boolean isBellIconVisible()     { return isVisible(bellIcon); }
    public boolean isAvatarVisible()       { return isVisible(avatar); }

    public boolean isHeaderComplete() {
        return isLogoVisible() && isHamburgerVisible() && isPlatformTitleVisible()
                && isBellIconVisible() && isAvatarVisible();
    }

    public void clickHamburger()  { safeClick(hamburgerToggle); }
    public void clickBell()       { safeClick(bellIcon); }
    public void clickAvatar()     { safeClick(avatar); }

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
