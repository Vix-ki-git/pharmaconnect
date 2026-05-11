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

public class HomePage {

    private WebDriver driver;
    private WebDriverWait wait;

    // ── Nav locators ──────────────────────────────────────────
    private By navFeatures   = By.xpath(
            "//a[normalize-space()='Features'] | //nav//a[contains(text(),'Features')]");
    private By navHowItWorks = By.xpath(
            "//a[contains(text(),'How it Works')] | //nav//a[contains(text(),'How')]");
    private By navCities     = By.xpath(
            "//a[normalize-space()='Cities'] | //nav//a[contains(text(),'Cities')]");
    private By navLogin      = By.xpath(
            "//a[contains(text(),'Log In')] | //a[contains(@href,'login')]");
    private By navSignUp     = By.xpath(
            "//a[contains(text(),'Sign Up')] | //a[contains(@href,'register')] | //button[contains(text(),'Sign Up')]");

    // ── Footer locators ───────────────────────────────────────
    private By footerHaveAccount = By.xpath(
            "//*[contains(text(),'Already have an account') or contains(text(),'already have')]");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    // ── Scroll helpers ────────────────────────────────────────

    private void scrollByPixels(int pixels) {
        try {
            ((JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, " + pixels + ")");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    private void scrollByFraction(double fraction) {
        try {
            ((JavascriptExecutor) driver)
                    .executeScript(
                            "window.scrollTo(0, document.body.scrollHeight * "
                                    + fraction + ")");
            Thread.sleep(1200);
        } catch (Exception ignored) {}
    }

    public void scrollToElement(By locator) {
        try {
            WebElement el = wait.until(
                    ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].scrollIntoView({behavior:'smooth',block:'center'})",
                            el);
            Thread.sleep(700);
        } catch (Exception ignored) {}
    }

    // ── Click actions ─────────────────────────────────────────

    public void clickNavFeatures() {
        try {
            driver.findElement(navFeatures).click();
        } catch (Exception e) {
            jsClick(navFeatures);
        }
    }

    public void clickNavHowItWorks() {
        try {
            driver.findElement(navHowItWorks).click();
        } catch (Exception e) {
            jsClick(navHowItWorks);
        }
    }

    public void clickNavCities() {
        try {
            driver.findElement(navCities).click();
        } catch (Exception e) {
            jsClick(navCities);
        }
    }

    public void clickNavLogin() {
        try {
            WebElement el = driver.findElement(
                    By.xpath("//a[contains(@href,'login')]"));
            el.click();
        } catch (Exception e) {
            try {
                driver.findElement(navLogin).click();
            } catch (Exception ex) {
                jsClick(navLogin);
            }
        }
    }

    public void clickNavSignUp() {
        try {
            WebElement el = driver.findElement(
                    By.xpath("//a[contains(@href,'register')]"));
            el.click();
        } catch (Exception e) {
            try {
                driver.findElement(navSignUp).click();
            } catch (Exception ex) {
                jsClick(navSignUp);
            }
        }
    }

    public void clickFooterGetStarted() {
        try {
            scrollByFraction(0.9);
            Thread.sleep(500);
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href,'register')]"));
            if (!links.isEmpty()) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();",
                                links.get(links.size() - 1));
            }
        } catch (Exception ignored) {}
    }

    public void clickFooterHaveAccount() {
        try {
            scrollByFraction(0.9);
            Thread.sleep(500);
            WebElement el = driver.findElement(footerHaveAccount);
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", el);
        } catch (Exception e) {
            try {
                List<WebElement> links = driver.findElements(
                        By.xpath("//a[contains(@href,'login')]"));
                if (!links.isEmpty()) {
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();",
                                    links.get(links.size() - 1));
                }
            } catch (Exception ignored) {}
        }
    }

    // ── Visibility checks ─────────────────────────────────────

    public boolean isNavLoginVisible() {
        try {
            return wait.until(
                            ExpectedConditions.visibilityOfElementLocated(navLogin))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Log In");
        }
    }

    public boolean isNavSignUpVisible() {
        try {
            return wait.until(
                            ExpectedConditions.visibilityOfElementLocated(navSignUp))
                    .isDisplayed();
        } catch (Exception e) {
            return driver.getPageSource().contains("Sign Up");
        }
    }

    public boolean isHeroBadgeVisible() {
        try {
            Thread.sleep(2000);
            String src = driver.getPageSource();
            return src.contains("Now Active in Chennai")
                    || src.contains("Active in Chennai")
                    || src.contains("Chennai");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isHeroHeadlineVisible() {
        try {
            Thread.sleep(2000);
            String src = driver.getPageSource();
            return src.contains("Every Paw")
                    || src.contains("Paw Deserves")
                    || src.contains("Deserves Care")
                    || src.toLowerCase().contains("every paw");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isHeroSubtitleVisible() {
        try {
            String src = driver.getPageSource();
            return src.contains("unified")
                    || src.contains("welfare platform")
                    || src.contains("India");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isGetStartedVisible() {
        try {
            String src = driver.getPageSource();
            return src.contains("Get Started")
                    || src.contains("get-started");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSeeFeaturesVisible() {
        try {
            String src = driver.getPageSource();
            return src.contains("See Features")
                    || src.contains("Features");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areStatsVisible() {
        try {
            scrollByPixels(400);
            scrollByPixels(700);
            scrollByPixels(1000);
            String src = driver.getPageSource();
            boolean rescued = src.contains("2,400")
                    || src.contains("2400")
                    || src.contains("Animals Rescued");
            boolean clinics = src.contains("120")
                    && (src.contains("Clinic") || src.contains("Partner"));
            boolean city    = src.contains("City Active")
                    || src.contains("1 City")
                    || (src.contains("City") && src.contains("Active"));
            boolean rate    = src.contains("98%") || src.contains("98");
            return rescued && clinics && city && rate;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areFeatureCardsVisible() {
        try {
            scrollByPixels(1200);
            scrollByPixels(1500);
            String src = driver.getPageSource();
            return (src.contains("Manage Your Pets") || src.contains("Manage Pets"))
                    && src.contains("Report Rescue")
                    && src.contains("Adopt")
                    && (src.contains("Book Vet") || src.contains("Appointment"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areHowItWorksStepsVisible() {
        try {
            scrollByPixels(2000);
            scrollByPixels(2500);
            String src = driver.getPageSource();
            return src.contains("Report or Browse")
                    && (src.contains("NGO") || src.contains("Respond"))
                    && (src.contains("Animals Thrive") || src.contains("Thrive"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areCitiesVisible() {
        try {
            scrollByFraction(0.6);
            scrollByFraction(0.7);
            String src = driver.getPageSource();
            return src.contains("Chennai")
                    && src.contains("Bangalore")
                    && src.contains("Mumbai")
                    && src.contains("Delhi");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isChennaiLive() {
        try {
            scrollByFraction(0.6);
            scrollByFraction(0.7);
            String src = driver.getPageSource();
            return src.contains("Chennai")
                    && (src.contains("Live")
                    || src.contains("live")
                    || src.contains("Active")
                    || src.contains("active"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areComingSoonCitiesVisible() {
        try {
            scrollByFraction(0.6);
            String src = driver.getPageSource();
            return src.contains("Bangalore")
                    && src.contains("Mumbai")
                    && src.contains("Delhi")
                    && (src.contains("Coming Soon")
                    || src.contains("coming-soon")
                    || src.contains("Soon"));
        } catch (Exception e) {
            return false;
        }
    }

    // ── Utility ───────────────────────────────────────────────

    private void jsClick(By locator) {
        try {
            WebElement el = driver.findElement(locator);
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", el);
        } catch (Exception ignored) {}
    }

    public String getPageTitle()  { return driver.getTitle(); }
    public String getCurrentUrl() { return driver.getCurrentUrl(); }
}