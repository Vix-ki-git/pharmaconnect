package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailInput     = By.xpath(
            "//input[@type='email'] | //input[@name='email'] | //input[@id='email']");
    private final By passwordInput  = By.xpath(
            "//input[@type='password'] | //input[@name='password'] | //input[@id='password']");
    private final By submitButton   = By.xpath(
            "//button[@type='submit'] | //button[contains(.,'Sign In')] | //button[contains(.,'Log In')] | //button[contains(.,'Login')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() {
        driver.get(LOGIN_URL);
    }

    public void loginAsPetOwner() {
        login(PET_OWNER_EMAIL, PET_OWNER_PASSWORD);
    }

    public void login(String email, String password) {
        open();
        WebElement emailEl = wait.until(
                ExpectedConditions.visibilityOfElementLocated(emailInput));
        emailEl.clear();
        emailEl.sendKeys(email);

        WebElement pwdEl = driver.findElement(passwordInput);
        pwdEl.clear();
        pwdEl.sendKeys(password);

        try {
            driver.findElement(submitButton).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();",
                            driver.findElement(submitButton));
        }

        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }
}
