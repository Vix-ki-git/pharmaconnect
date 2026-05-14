package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.REGISTER_URL;

public class TC014_RegisterAccountTypeOptionsTest extends BaseTest {

    private RegisterPage registerPage;

    @BeforeMethod
    public void openRegisterPage() {
        driver.get(REGISTER_URL);
        registerPage = new RegisterPage(driver);
    }

    @Test(description = "Step 1: Account Type dropdown has exactly 3 options with correct labels")
    public void step1_accountTypeDropdownHasThreeOptions() {
        ExtentReportManager.createTest("TC014 - Step 1",
                "Account Type dropdown shows exactly: Pet Owner, NGO/Rescue, Veterinary Hospital");

        List<String> options = registerPage.getAccountTypeOptions();
        ExtentReportManager.getTest().info("Options found: " + options);

        // Filter out any blank/default placeholder items (e.g. "Select account type")
        long meaningfulCount = options.stream()
                .filter(o -> !o.isEmpty()
                        && !o.toLowerCase().contains("select")
                        && !o.toLowerCase().contains("choose"))
                .count();

        Assert.assertEquals(meaningfulCount, 3L,
                "Expected exactly 3 role options but found " + meaningfulCount + ": " + options);

        boolean hasPetOwner = options.stream().anyMatch(
                o -> o.contains("Pet Owner") || o.contains("Pet owner"));
        boolean hasNgo      = options.stream().anyMatch(
                o -> o.contains("NGO") || o.contains("Rescue"));
        boolean hasHospital = options.stream().anyMatch(
                o -> o.contains("Veterinary") || o.contains("Hospital"));

        Assert.assertTrue(hasPetOwner,
                "'Pet Owner' option not found in dropdown. Options: " + options);
        Assert.assertTrue(hasNgo,
                "'NGO / Rescue Organisation' option not found in dropdown. Options: " + options);
        Assert.assertTrue(hasHospital,
                "'Veterinary Hospital' option not found in dropdown. Options: " + options);

        // Verify no unexpected extra options (Admin, Reporter, Volunteer, etc.)
        for (String opt : options) {
            String lower = opt.toLowerCase();
            boolean isKnown = lower.contains("pet owner")
                    || lower.contains("ngo") || lower.contains("rescue")
                    || lower.contains("veterinary") || lower.contains("hospital")
                    || lower.isEmpty() || lower.contains("select") || lower.contains("choose");
            Assert.assertTrue(isKnown,
                    "Unexpected option in Account Type dropdown: '" + opt + "'");
        }

        ExtentReportManager.getTest().pass("Step 1 passed — exactly 3 expected role options verified");
    }
}
