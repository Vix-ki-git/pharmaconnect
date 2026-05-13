package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC402_HospitalsRoleTest extends BaseApiTest {

    @Test(description = "TC402.1 — HOSPITAL role gets its own profile")
    public void hospitalProfile_returnsOwn() {
        ExtentReportManager.createTest("TC402.1 GET /hospitals/profile (HOSPITAL)",
                "Authenticated HOSPITAL fetches its own profile");

        Response r = RestAssured.given(ApiSpecs.asHospital()).when().get("/hospitals/profile");

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertNotNull(r.jsonPath().getString("data.name"));
        Assert.assertEquals(r.jsonPath().getString("data.email"), "hospital@petz.com");
    }

    @Test(description = "TC402.2 — USER calling /hospitals/profile gets 403 Access denied")
    public void hospitalProfile_asUser_returns403() {
        ExtentReportManager.createTest("TC402.2 /hospitals/profile as USER",
                "Role enforcement: USER role can't access HOSPITAL-scoped endpoints");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/hospitals/profile");

        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), "Access denied.");
    }
}
