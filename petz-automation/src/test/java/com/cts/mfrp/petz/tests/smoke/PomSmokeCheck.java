package com.cts.mfrp.petz.tests.smoke;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LandingPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Smoke check against the live deployment for public-page POMs (Landing, Login, Register).
 * Not part of the default suite — run via {@code mvn test -Dsurefire.suiteXmlFiles=smoke.xml}.
 * The assertions only check things we *positively* expect — they're not exhaustive test
 * cases, just a sanity net for the locators.
 */
public class PomSmokeCheck extends BaseTest {

    @Test(priority = 1)
    public void smokeLandingPage() {
        LandingPage page = new LandingPage(driver);
        page.open();

        Assert.assertTrue(page.getPageTitle().contains("Animal Welfare Platform"),
                "Page title mismatch — got: " + page.getPageTitle());
        Assert.assertTrue(page.isLogoVisible(),           "Logo not visible");
        Assert.assertTrue(page.areHeaderNavLinksVisible(),"Header nav links not all visible");
        Assert.assertTrue(page.isSignUpButtonVisible(),   "Sign Up Free button not visible");
        Assert.assertTrue(page.isLogInLinkVisible(),      "Log In link not visible");
        Assert.assertTrue(page.isHeroBadgeVisible(),      "Hero badge not visible");
        Assert.assertTrue(page.isHeroHeadlineVisible(),   "Hero headline not visible");
        Assert.assertTrue(page.isHeroSubtitleVisible(),   "Hero subtitle not visible");
        Assert.assertTrue(page.areHeroCTAsVisible(),      "Hero CTAs not visible");
        Assert.assertTrue(page.areStatsStripVisible(),    "Stats strip not visible");
        Assert.assertTrue(page.areFeatureCardsVisible(),  "Feature cards not visible");
        Assert.assertTrue(page.areHowItWorksStepsVisible(),"How-it-works steps not visible");
        Assert.assertTrue(page.areCityTilesVisible(),     "City tiles not visible");
    }

    @Test(priority = 2)
    public void smokeLoginPage() {
        LoginPage page = new LoginPage(driver);
        page.open();

        Assert.assertTrue(page.isWelcomeBackVisible(),    "Welcome back h2 not visible");
        Assert.assertTrue(page.isLeftPanelVisible(),      "Left panel not visible");
        Assert.assertTrue(page.leftPanelFeaturesVisible(),"Left panel feature bullets not visible");
        Assert.assertTrue(page.isCreateOneLinkVisible(),  "Create one link not visible");
        Assert.assertTrue(page.isBackToHomeLinkVisible(), "Back to home link not visible");
        Assert.assertTrue(page.isSignInDisabled(),        "Sign In should be disabled when fields empty");
        Assert.assertTrue(page.isPasswordMasked(),        "Password should start masked");

        page.fillEmail("user@example.com");
        page.fillPassword("password123");
        Assert.assertTrue(page.isSignInEnabled(), "Sign In should enable after both fields filled");

        page.clickEyeToggle();
        Assert.assertFalse(page.isPasswordMasked(), "Eye toggle should reveal password");
        page.clickEyeToggle();
        Assert.assertTrue(page.isPasswordMasked(),  "Eye toggle again should re-mask password");
    }

    @Test(priority = 3)
    public void smokeRegisterPage() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        Assert.assertTrue(page.isCardTitleVisible(),       "Create account h2 not visible");
        Assert.assertTrue(page.isLeftPanelTitleVisible(),  "Left panel h1 not visible");
        Assert.assertTrue(page.leftPanelRoleBulletsVisible(),"Left panel role bullets not visible");
        Assert.assertTrue(page.isSignInLinkVisible(),      "Sign in link not visible");
        Assert.assertTrue(page.isCreateAccountDisabled(),  "Create Account should be disabled when empty");

        Assert.assertEquals(page.getPlaceholder("fullName"), "John Doe");
        Assert.assertEquals(page.getPlaceholder("phone"),    "+91 00000 00000");
        Assert.assertEquals(page.getPlaceholder("email"),    "you@example.com");
        Assert.assertEquals(page.getPlaceholder("password"), "min. 6 characters");

        List<String> opts = page.getAccountTypeOptions();
        Assert.assertTrue(opts.size() >= 3, "Account Type dropdown should expose ≥3 options, got " + opts);
        boolean hasPet  = opts.stream().anyMatch(o -> o.toLowerCase().contains("pet owner"));
        boolean hasNgo  = opts.stream().anyMatch(o -> o.toLowerCase().contains("ngo") || o.toLowerCase().contains("rescue"));
        boolean hasVet  = opts.stream().anyMatch(o -> o.toLowerCase().contains("veterinary") || o.toLowerCase().contains("hospital"));
        Assert.assertTrue(hasPet && hasNgo && hasVet, "Expected Pet Owner / NGO / Vet options, got " + opts);
    }
}
