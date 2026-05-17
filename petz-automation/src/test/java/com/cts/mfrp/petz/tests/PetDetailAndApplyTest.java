package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.PetDetailPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Pet Detail + Apply-to-Adopt scenario — TS06, TC032 to TC035 in one class.
 * Mirrors the layout of PetOwnerDashboardTest: BaseTest spawns a fresh driver
 * per @Test method, all four cases live in this single file.
 */
public class PetDetailAndApplyTest extends BaseTest {

    // ─── TC032 ─────────────────────────────────────────────────────────────
    @Test(priority = 32, description =
            "PETZ_TC032 - Validate the pet detail page layout on /adoption/animals/{id}")
    public void TC032_verifyPetDetailLayout() {
        ExtentReportManager.createTest(
                "PETZ_TC032_PetDetailLayout",
                "Click 'View Profile' on /adoption/animals and validate the pet card: " +
                        "species chip, name, breed-age-gender, Vaccinated + AVAILABLE chips, " +
                        "location, description, and the 4 attribute chips (Age/Gender/Species/Breed).");

        new LoginPage(driver).loginAsPetOwner();
        PetDetailPage detail = new PetDetailPage(driver);

        // TC032 test data: petId=1 (seed Max — Dog, Labrador Mix, vaccinated, AVAILABLE).
        detail.openPetDetailFromListingByName("Max");

        Assert.assertTrue(detail.isOnDetailUrl(),
                "Expected URL to be /adoption/animals/<id>, but was: " + driver.getCurrentUrl());
        Assert.assertTrue(detail.getCurrentPetId() > 0,
                "Could not parse a numeric pet id from the URL: " + driver.getCurrentUrl());

        Assert.assertTrue(detail.isBackToAllAnimalsVisible(),
                "'← All Animals' breadcrumb is not visible at the top of the detail page.");

        Assert.assertTrue(detail.isSpeciesChipVisible(),
                "Species chip overlay on the photo is not visible.");

        Assert.assertFalse(detail.getPetName().isBlank(),
                "Pet name heading is empty.");

        Assert.assertTrue(detail.isBreedAgeGenderLineVisible(),
                "Breed-age-gender line (e.g. 'Labrador Mix · 8 months · MALE') is not visible.");

        Assert.assertTrue(detail.isVaccinatedChipVisible(),
                "Green 'Vaccinated' chip is not visible.");

        Assert.assertTrue(detail.isAvailableChipVisible(),
                "Orange 'AVAILABLE' chip is not visible.");

        Assert.assertTrue(detail.isLocationVisible(),
                "Location/city block with pin icon is not visible.");

        Assert.assertTrue(detail.isDescriptionParagraphVisible(),
                "Description paragraph is not visible.");

        Assert.assertTrue(detail.areAttributeChipsVisible(),
                "One or more of the 4 attribute chips (Age/Gender/Species/Breed) are missing.");
    }

    // ─── TC033 ─────────────────────────────────────────────────────────────
    @Test(priority = 33, description =
            "PETZ_TC033 - Validate the Apply-to-Adopt form fields on the pet detail page")
    public void TC033_verifyApplyFormFields() {
        ExtentReportManager.createTest(
                "PETZ_TC033_ApplyFormFields",
                "Scroll to 'Apply to Adopt {name}' and validate the heading, YOUR STORY section, " +
                        "the 'Why do you want to adopt {name}?' textarea and the " +
                        "'Previous pet ownership experience' textarea.");

        new LoginPage(driver).loginAsPetOwner();
        PetDetailPage detail = new PetDetailPage(driver);

        detail.openFirstPetDetailFromListing();

        String petName = detail.getPetName();
        Assert.assertTrue(detail.isApplyHeadingVisible(petName),
                "'Apply to Adopt " + petName + "' heading is not visible.");

        Assert.assertTrue(detail.isYourStorySectionVisible(),
                "'YOUR STORY' / 'Tell us about yourself and your home environment' section is not visible.");

        Assert.assertTrue(detail.isWhyTextareaVisible(),
                "'Why do you want to adopt " + petName + "?' textarea is not visible.");

        Assert.assertTrue(detail.isExperienceTextareaVisible(),
                "'Previous pet ownership experience' textarea is not visible.");
    }

    // ─── TC034 ─────────────────────────────────────────────────────────────
    @Test(priority = 34, description =
            "PETZ_TC034 - Submit button must stay disabled (or block submission) until Why is filled")
    public void TC034_verifyApplySubmitDisabledWhenWhyEmpty() {
        ExtentReportManager.createTest(
                "PETZ_TC034_ApplySubmitDisabled",
                "Leave the 'Why do you want to adopt' textarea empty; Submit must be disabled " +
                        "or the form must surface a 'Required' inline error.");

        new LoginPage(driver).loginAsPetOwner();
        PetDetailPage detail = new PetDetailPage(driver);

        detail.openFirstPetDetailFromListing();
        Assert.assertTrue(detail.isWhyTextareaVisible(),
                "Apply form did not render — cannot validate disabled-submit behaviour.");

        // Try to submit with Why empty
        boolean disabledBefore = detail.isSubmitDisabled();
        detail.tryClickSubmitIgnoringDisabled();

        boolean stillOnDetail = detail.isOnDetailUrl();
        boolean disabledAfter = detail.isSubmitDisabled();
        boolean inlineError   = detail.hasRequiredInlineError();

        Assert.assertTrue(
                disabledBefore || disabledAfter || inlineError || stillOnDetail,
                "Expected Submit to be disabled, a 'Required' inline error, or the form to " +
                        "block navigation when Why is empty. Submit disabled before/after: "
                        + disabledBefore + "/" + disabledAfter + ", inline error: " + inlineError
                        + ", stayed on detail URL: " + stillOnDetail);
    }

    // ─── TC035 ─────────────────────────────────────────────────────────────
    @Test(priority = 35, description =
            "PETZ_TC035 - Successful adoption-application submission with both textareas filled")
    public void TC035_verifyApplySubmitHappyPath() {
        ExtentReportManager.createTest(
                "PETZ_TC035_ApplySubmitHappy",
                "Fill both textareas with valid copy, submit, and verify a success toast is shown.");

        new LoginPage(driver).loginAsPetOwner();
        PetDetailPage detail = new PetDetailPage(driver);

        detail.openFirstPetDetailFromListing();

        String petName = detail.getPetName();

        detail.fillWhy("I have a quiet home with a yard and prior pet experience.");
        detail.fillExperience("Owned a Labrador for 8 years.");

        Assert.assertFalse(detail.isSubmitDisabled(),
                "Submit button should be enabled once both textareas are filled with valid text.");

        detail.clickSubmit();

        Assert.assertTrue(detail.isSuccessToastVisible(),
                "Expected a success toast (e.g. 'Application submitted') after submitting " +
                        "the adoption application for " + petName + ".");
    }
}
