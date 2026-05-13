package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.clients.AuthClient;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.models.auth.RegisterRequest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TC102_AuthRegisterTest extends BaseApiTest {

    private AuthClient auth;

    @BeforeClass
    public void init() {
        auth = new AuthClient();
    }

    private RegisterRequest validUser(String emailSuffix) {
        long ts = System.currentTimeMillis();
        return new RegisterRequest(
                "Auto User " + ts,
                "auto_" + ts + emailSuffix + "@example.com",
                "Strong@123",
                "98765" + String.format("%05d", (int) (Math.random() * 100_000)),
                "USER");
    }

    @Test(description = "TC102.1 — register USER happy path returns 200 with token + isApproved=true")
    public void registerUser_happyPath() {
        ExtentReportManager.createTest("TC102.1 Register USER happy",
                "POST /auth/register creates a fresh USER; response contains JWT and isApproved=true");

        Response r = auth.register(validUser("a"));

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
        Assert.assertNotNull(r.jsonPath().getString("data.token"));
        Assert.assertEquals(r.jsonPath().getString("data.role"), "USER");
        Assert.assertEquals(r.jsonPath().getBoolean("data.isApproved"), true,
                "USER role should be auto-approved");
    }

    @Test(description = "TC102.2 — register with empty body returns 400 with validation map")
    public void registerEmptyBody_returns400() {
        ExtentReportManager.createTest("TC102.2 Register - empty body",
                "POST /auth/register with {} fails @NotBlank on name/email/password");

        Response r = auth.register(new RegisterRequest());

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), "Validation failed.");
        Assert.assertEquals(r.jsonPath().getString("data.name"),     "must not be blank");
        Assert.assertEquals(r.jsonPath().getString("data.email"),    "must not be blank");
        Assert.assertEquals(r.jsonPath().getString("data.password"), "must not be blank");
    }

    @Test(description = "TC102.3 — register with invalid email format returns 400")
    public void registerInvalidEmail_returns400() {
        ExtentReportManager.createTest("TC102.3 Register - invalid email",
                "POST /auth/register with malformed email fails @Email");

        RegisterRequest body = validUser("b");
        body.setEmail("not-an-email");
        Response r = auth.register(body);

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("data.email"),
                "must be a well-formed email address");
    }

    @Test(description = "TC102.4 — register with short password returns 400")
    public void registerShortPassword_returns400() {
        ExtentReportManager.createTest("TC102.4 Register - short password",
                "POST /auth/register with password under 6 chars fails @Size");

        RegisterRequest body = validUser("c");
        body.setPassword("ab");
        Response r = auth.register(body);

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertNotNull(r.jsonPath().getString("data.password"));
    }

    @Test(description = "TC102.5 — register duplicate email returns 4xx",
            dependsOnMethods = "registerUser_happyPath")
    public void registerDuplicateEmail_returnsError() {
        ExtentReportManager.createTest("TC102.5 Register - duplicate email",
                "Reusing an existing email should fail (deployed returns 400 with business message)");

        RegisterRequest body = validUser("d");
        body.setEmail("user@petz.com");
        Response r = auth.register(body);

        Assert.assertTrue(r.statusCode() >= 400 && r.statusCode() < 500,
                "Expected 4xx for duplicate, got " + r.statusCode() + ". Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getBoolean("success"), false);
    }
}
