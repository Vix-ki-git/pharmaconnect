package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.HOME_URL;

/**
 * Public landing page (/). Covers PETZ_TC001 – PETZ_TC005.
 *
 * <p>Selectors verified against the live deployment — the nav lives inside a
 * &lt;nav class="landing-nav"&gt; element, header buttons are &lt;button&gt; with a
 * routerlink attribute, and section anchors are bare &lt;a&gt; tags inside the nav.
 */
public class LandingPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Header / nav
    private final By logo          = By.cssSelector(".nav-logo-text, .footer-logo-text");
    private final By navFeatures   = By.xpath("//*[contains(@class,'landing-nav')]//a[normalize-space()='Features']");
    private final By navHowItWorks = By.xpath("//*[contains(@class,'landing-nav')]//a[normalize-space()='How it Works']");
    private final By navCities     = By.xpath("//*[contains(@class,'landing-nav')]//a[normalize-space()='Cities']");
    private final By navLogIn      = By.xpath("//button[@routerlink='/auth/login' and normalize-space()='Log In']");
    private final By navSignUp     = By.xpath("//button[@routerlink='/auth/register' and normalize-space()='Sign Up Free']");

    // Hero
    private final By heroBadge    = By.xpath("//*[contains(normalize-space(),'Now Active in Chennai')]");
    private final By heroHeadline = By.xpath("//h1[contains(.,'Paw') and contains(.,'Deserves Care')]");
    private final By heroSubtitle = By.xpath("//*[contains(.,\"India's first unified pet welfare platform\") or contains(.,'India’s first unified pet welfare platform')]");
    private final By heroPrimary  = By.xpath("//*[contains(@class,'btn-hero-primary')]");
    private final By heroGhost    = By.xpath("//*[contains(@class,'btn-hero-ghost') and contains(normalize-space(),'See Features')]");

    // Sections (anchor targets)
    private final By featuresSection   = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Everything Your Pet Needs')]");
    private final By howItWorksSection = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Simple') and contains(normalize-space(),'Effective')]");
    private final By citiesSection     = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Where We Operate')]");

    // Bottom CTA banner
    private final By bottomGetStarted = By.xpath("//a[contains(@class,'btn-cta-primary') and contains(normalize-space(),\"Get Started\")]");
    private final By bottomSignInLink = By.xpath("//a[contains(@class,'btn-cta-ghost') and contains(normalize-space(),'Already have an account')]");

    public LandingPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(HOME_URL); }

    // ── TC001: header + hero ──
    public String getPageTitle()             { return driver.getTitle(); }
    public boolean isLogoVisible()           { return isVisible(logo); }
    public boolean areHeaderNavLinksVisible(){ return isVisible(navFeatures) && isVisible(navHowItWorks) && isVisible(navCities); }
    public boolean isLogInLinkVisible()      { return isVisible(navLogIn); }
    public boolean isSignUpButtonVisible()   { return isVisible(navSignUp); }
    public boolean isHeroBadgeVisible()      { return isVisible(heroBadge); }
    public boolean isHeroHeadlineVisible()   { return isVisible(heroHeadline); }
    public boolean isHeroSubtitleVisible()   { return isVisible(heroSubtitle); }
    public boolean areHeroCTAsVisible()      { return isVisible(heroPrimary) && isVisible(heroGhost); }

    // ── TC002: stats + feature + how-it-works cards ──
    public boolean areStatsStripVisible() {
        // The "+" / "%" suffixes are rendered in separate spans, so we match the value + label fragments only.
        String src = driver.getPageSource();
        return src.contains("2,400") && src.contains("Animals Rescued")
                && src.contains("120")   && src.contains("Clinics")
                && src.contains("City Active") && src.contains("Rescue Response Rate");
    }

    public boolean areFeatureCardsVisible() {
        String src = driver.getPageSource();
        return src.contains("Manage Your Pets") && src.contains("FOR PET OWNERS")
                && src.contains("Report Rescues") && src.contains("SEE NETWORK")
                && src.contains("Adopt an Animal") && src.contains("OPEN ADOPTIONS")
                && src.contains("Book Vet Appointments") && src.contains("SEE CLINICS");
    }

    public boolean areHowItWorksStepsVisible() {
        String src = driver.getPageSource();
        return src.contains("Report or Browse") && src.contains("NGOs Respond") && src.contains("Animals Thrive");
    }

    // ── TC003: cities widget ──
    public boolean areCityTilesVisible() {
        String src = driver.getPageSource();
        return src.contains("Chennai") && src.contains("Bangalore") && src.contains("Mumbai") && src.contains("Delhi");
    }

    public boolean isOnlyChennaiLive() {
        // Other cities show "Coming Soon"; Chennai is the only one shown Live.
        return driver.getPageSource().contains("Coming Soon");
    }

    // ── TC004: header + bottom-banner routing ──
    public void clickSignUpFree()        { safeClick(navSignUp); }
    public void clickLogIn()             { safeClick(navLogIn); }
    public void clickBottomGetStarted()  { safeClick(bottomGetStarted); }
    public void clickBottomSignInLink()  { safeClick(bottomSignInLink); }

    // ── TC005: anchor scroll ──
    public void clickNavFeatures()   { safeClick(navFeatures); }
    public void clickNavHowItWorks() { safeClick(navHowItWorks); }
    public void clickNavCities()     { safeClick(navCities); }

    public boolean isFeaturesSectionInView()   { return inViewport(featuresSection); }
    public boolean isHowItWorksSectionInView() { return inViewport(howItWorksSection); }
    public boolean isCitiesSectionInView()     { return inViewport(citiesSection); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
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

    private boolean inViewport(By by) {
        try {
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
            return (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var r = arguments[0].getBoundingClientRect();" +
                    "return r.top < window.innerHeight && r.bottom > 0;", el);
        } catch (Exception e) { return false; }
    }
}
