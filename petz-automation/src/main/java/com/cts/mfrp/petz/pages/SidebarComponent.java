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
 * Cross-cutting sidebar. Covers PETZ_TC025, TC062, TC086, TC089, plus the user widget
 * portion of every dashboard test (USER / NGO / HOSPITAL labels).
 */
public class SidebarComponent {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By sidebar = By.xpath(
            "//mat-sidenav | //*[contains(@class,'mat-sidenav')] | //*[contains(@class,'sidenav')] | //aside");

    private final By collapseChevron = By.xpath(
            "//mat-sidenav//button[.//mat-icon[normalize-space()='chevron_left' or normalize-space()='chevron_right']]" +
            " | //*[contains(@class,'sidenav')]//button[.//mat-icon[normalize-space()='chevron_left' or normalize-space()='chevron_right']]");

    private final By userWidget = By.xpath(
            "//mat-sidenav//*[contains(@class,'user-widget') or contains(@class,'user-info') or contains(@class,'sidebar-user')]" +
            " | //*[contains(@class,'sidenav')]//*[contains(@class,'user')]");

    private final By signOutItem = By.xpath(
            "//*[normalize-space()='Sign Out' or normalize-space()='Logout' or normalize-space()='Log Out' or normalize-space()='Log out']");

    public SidebarComponent(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public boolean isVisible() {
        try { return driver.findElement(sidebar).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /** True if every supplied label is present in the sidebar (in any order). */
    public boolean containsItems(String... labels) {
        String src;
        try { src = driver.findElement(sidebar).getText(); }
        catch (Exception e) { src = driver.getPageSource(); }
        for (String l : labels) if (!src.contains(l)) return false;
        return true;
    }

    /** Click a sidebar nav item by visible label. */
    public void clickItem(String label) {
        By loc = By.xpath(
                "//mat-sidenav//a[.//*[normalize-space()='" + label + "']]" +
                " | //*[contains(@class,'sidenav')]//a[.//*[normalize-space()='" + label + "']]" +
                " | //mat-sidenav//*[normalize-space()='" + label + "']/ancestor::a[1]" +
                " | //*[contains(@class,'sidenav')]//*[normalize-space()='" + label + "']/ancestor::a[1]");
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(loc));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    /** Returns the role label shown under the user widget ("USER", "NGO", "HOSPITAL"). */
    public String getRoleLabel() {
        try {
            String text = driver.findElement(userWidget).getText().toUpperCase();
            if (text.contains("HOSPITAL")) return "HOSPITAL";
            if (text.contains("NGO"))      return "NGO";
            if (text.contains("USER"))     return "USER";
            return text;
        } catch (Exception e) {
            String src = driver.getPageSource().toUpperCase();
            if (src.contains("HOSPITAL")) return "HOSPITAL";
            if (src.contains("NGO"))      return "NGO";
            return src.contains("USER") ? "USER" : "";
        }
    }

    public boolean isUserWidgetVisible() {
        try { return driver.findElement(userWidget).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /** Sidebar collapse / expand. */
    public void clickCollapseChevron() {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(collapseChevron));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    public boolean isCollapsed() {
        try {
            WebElement sn = driver.findElement(sidebar);
            String cls = sn.getAttribute("class");
            return cls != null && (cls.contains("collapsed") || cls.contains("mini") || cls.contains("icon-only"));
        } catch (Exception e) { return false; }
    }

    /** User widget click → menu → Sign Out. Works whether the user widget IS the menu (Material list) or a popover. */
    public void clickSignOut() {
        try {
            WebElement w = driver.findElement(userWidget);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", w);
            try { w.click(); } catch (Exception ignored) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", w);
            }
        } catch (Exception ignored) {}

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(signOutItem));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
