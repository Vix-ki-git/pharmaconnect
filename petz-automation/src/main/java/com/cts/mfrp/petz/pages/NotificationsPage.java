package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

public class NotificationsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By bellIcon  = By.xpath("//button[@title='Notifications']");
    private final By title     = By.xpath("//app-notifications//h1[normalize-space()='Notifications']");
    private final By subtitle  = By.xpath("//app-notifications//p[normalize-space()='Stay updated on your activity']");
    private final By notifCard = By.cssSelector("app-notifications .notif-card");
    private final By backArrow = By.xpath("//app-notifications//mat-icon[normalize-space()='arrow_back']/ancestor::button[1]");

    public NotificationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void clickBell() {
        wait.until(ExpectedConditions.elementToBeClickable(bellIcon)).click();
        wait.until(ExpectedConditions.urlContains("/notifications"));
    }

    public void clickBack() {
        wait.until(ExpectedConditions.elementToBeClickable(backArrow)).click();
        wait.until(d -> !d.getCurrentUrl().contains("/notifications"));
    }

    public boolean isTitleVisible() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(title)).isDisplayed();
    }

    public boolean isSubtitleVisible() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(subtitle)).isDisplayed();
    }

    public boolean isEmptyOrListVisible() {
        return wait.until(d ->
                d.getPageSource().contains("No notifications yet")
                || !d.findElements(notifCard).isEmpty());
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
