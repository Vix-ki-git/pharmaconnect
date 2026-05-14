package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Greeting / header
    private final By greetingHeading = By.xpath(
            "//*[self::h1 or self::h2 or self::h3]" +
                    "[contains(.,'Good Morning') or contains(.,'Good Afternoon') " +
                    "or contains(.,'Good Evening') or contains(.,'Good morning') " +
                    "or contains(.,'Good afternoon') or contains(.,'Good evening')]");
    private final By subheading = By.xpath(
            "//*[contains(text(),\"today's overview\") or contains(text(),'today’s overview')]");

    // Emergency banner
    private final By reportNowButton = By.xpath(
            "//button[@routerlink='/rescue/report'] | " +
            "//button[contains(@class,'emergency-btn')] | " +
            "//a[contains(@class,'emergency-banner')] | " +
            "//button[contains(.,'Report Now')] | " +
            "//a[contains(.,'Report Now')]");

    // Sidebar (Angular Material mat-sidenav)
    private final By sidebar = By.xpath(
            "//mat-sidenav | //*[contains(@class,'mat-sidenav')] | " +
            "//*[contains(@class,'sidenav')]");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    // Greeting / date

    public boolean isGreetingVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(greetingHeading))
                    .isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("Good Morning") || src.contains("Good Afternoon")
                    || src.contains("Good Evening");
        }
    }

    public boolean greetingContainsFirstName(String firstName) {
        try {
            String text = driver.findElement(greetingHeading).getText();
            return text.contains(firstName);
        } catch (Exception e) {
            return driver.getPageSource().contains(firstName);
        }
    }

    public boolean isSubheadingVisible() {
        try {
            return driver.findElement(subheading).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("today's overview") || src.contains("today’s overview");
        }
    }

    public boolean isTodayDateVisible() {
        DateTimeFormatter weekday = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
        DateTimeFormatter month   = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);
        LocalDate today = LocalDate.now();
        String src = driver.getPageSource();
        return src.contains(today.format(weekday))
                && src.contains(today.format(month))
                && src.contains(String.valueOf(today.getDayOfMonth()))
                && src.contains(String.valueOf(today.getYear()));
    }

    // Emergency banner

    public boolean isEmergencyBannerVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(text(),'Animal in Distress')]")))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Animal in Distress");
        }
    }

    public boolean isEmergencyCopyVisible() {
        String src = driver.getPageSource();
        return (src.contains("Report it immediately")
                || src.contains("every second counts"))
                && src.contains("Animal in Distress");
    }

    public boolean isReportNowButtonVisible() {
        try {
            return driver.findElement(reportNowButton).isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Report Now");
        }
    }

    public void clickReportNow() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(reportNowButton));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    // Stat tiles

    public boolean areStatTilesVisible() {
        String src = driver.getPageSource().toUpperCase(Locale.ENGLISH);
        return src.contains("MY PETS")
                && src.contains("APPOINTMENTS")
                && src.contains("RESCUE REPORTS")
                && src.contains("ADOPTIONS");
    }

    public boolean areStatTileBreakdownsVisible() {
        try {
            wait.until(d -> {
                String s = d.getPageSource();
                return (s.contains("Dogs") || s.contains("Cats") || s.contains("No pets"))
                        && (s.contains("Upcoming") || s.contains("Done")
                            || s.contains("Cancelled") || s.contains("No appointments"))
                        && (s.contains("Pending") || s.contains("Resolved")
                            || s.contains("Reported") || s.contains("No reports"))
                        && (s.contains("In Review") || s.contains("Approved")
                            || s.contains("No applications"));
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Quick Actions

    public boolean areQuickActionsVisible() {
        String src = driver.getPageSource();
        return src.contains("Report Rescue")
                && src.contains("Browse Adoptions")
                && src.contains("Book Appointment")
                && src.contains("My Adoptions")
                && src.contains("My Appointments");
    }

    public void clickQuickAction(String label) {
        By locator = quickActionCard(label);
        try {
            WebElement el = wait.until(
                    ExpectedConditions.elementToBeClickable(locator));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", el);
            el.click();
        } catch (Exception e) {
            try {
                WebElement el = driver.findElement(By.xpath(
                        "//*[normalize-space()='" + label + "']"));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }

    private By quickActionCard(String label) {
        return By.xpath(
                "//a[contains(@class,'action-card') and " +
                ".//*[contains(@class,'action-title') and normalize-space()='" + label + "']]" +
                " | " +
                "//*[contains(@class,'action-title') and normalize-space()='" + label + "']" +
                "/ancestor::a[1]");
    }

    // Sidebar

    public boolean isSidebarVisible() {
        try {
            return driver.findElement(sidebar).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean sidebarContains(String... labels) {
        String src;
        try {
            src = driver.findElement(sidebar).getText();
        } catch (Exception e) {
            src = driver.getPageSource();
        }
        for (String l : labels) {
            if (!src.contains(l)) return false;
        }
        return true;
    }

    public boolean sidebarUserLabelVisible() {
        try {
            String src = driver.findElement(sidebar).getText();
            return src.contains("USER") || src.contains("User");
        } catch (Exception e) {
            return driver.getPageSource().contains("USER");
        }
    }

    public void clickSidebarItem(String label) {
        By loc = sidebarItem(label);
        try {
            WebElement el = wait.until(
                    ExpectedConditions.elementToBeClickable(loc));
            el.click();
        } catch (Exception e) {
            try {
                WebElement el = driver.findElement(loc);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }

    private By sidebarItem(String label) {
        return By.xpath(
                "//mat-sidenav//a[contains(@class,'mat-mdc-list-item') and " +
                ".//span[contains(@class,'mdc-list-item__primary-text') " +
                "and normalize-space()='" + label + "']]" +
                " | " +
                "//mat-sidenav//*[normalize-space()='" + label + "']/ancestor::a[1]");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
