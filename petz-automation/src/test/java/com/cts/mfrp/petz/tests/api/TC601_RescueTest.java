package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC601_RescueTest extends BaseApiTest {

    @Test(description = "TC601.1 — USER GET /rescue/my returns own rescue reports")
    public void rescueMy_returnsList() {
        ExtentReportManager.createTest("TC601.1 GET /rescue/my (USER)",
                "Authenticated USER lists their rescue reports");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/rescue/my");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
    }

    @Test(description = "TC601.2 — NGO GET /rescue/ngo returns incoming reports")
    public void rescueNgo_returnsList() {
        ExtentReportManager.createTest("TC601.2 GET /rescue/ngo (NGO)",
                "NGO role views incoming rescue reports");

        Response r = RestAssured.given(ApiSpecs.asNgo()).when().get("/rescue/ngo");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
    }

    @Test(description = "TC601.3 — USER hitting /rescue/ngo gets 403")
    public void rescueNgo_asUser_returns403() {
        ExtentReportManager.createTest("TC601.3 /rescue/ngo as USER",
                "Role enforcement");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/rescue/ngo");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    // POST /rescue is multipart/form-data — deferred from this pass to keep the suite simple.
}
