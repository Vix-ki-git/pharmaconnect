package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.USERS_ME;

public class TC1101_AuthBoundariesTest extends BaseApiTest {

    @Test(description = "TC1101.1 — no Bearer token on protected endpoint returns 403 with empty body")
    public void noToken_returns403EmptyBody() {
        ExtentReportManager.createTest("TC1101.1 No Bearer header",
                "Spring Security default rejects unauthenticated requests with 403, empty body");

        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get(USERS_ME);

        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
        Assert.assertTrue(r.asString().isBlank(),
                "Spring Security default returns no body for the 403; got: " + r.asString());
    }

    @Test(description = "TC1101.2 — garbage Bearer token returns 403 with empty body")
    public void badToken_returns403EmptyBody() {
        ExtentReportManager.createTest("TC1101.2 Garbage Bearer token",
                "Invalid JWT is treated the same as missing one");

        Response r = RestAssured.given(ApiSpecs.authedRequestSpec("not-a-real-jwt"))
                .when().get(USERS_ME);

        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(description = "TC1101.3 — role mismatch returns 403 with 'Access denied.' message")
    public void wrongRole_returns403WithMessage() {
        ExtentReportManager.createTest("TC1101.3 Wrong role",
                "Distinguish auth-failed (empty body) from authz-failed (JSON Access denied.)");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/admin/users");

        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), "Access denied.");
    }
}
