package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC501_AppointmentsTest extends BaseApiTest {

    @Test(description = "TC501.1 — USER GET /appointments/my returns list")
    public void appointmentsMy_returnsList() {
        ExtentReportManager.createTest("TC501.1 GET /appointments/my (USER)",
                "Authenticated USER lists their appointments");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/appointments/my");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
        Assert.assertNotNull(r.jsonPath().getList("data"));
    }

    @Test(description = "TC501.2 — GET /appointments/slots with doctorId and date returns slot list")
    public void appointmentsSlots_returnsSlots() {
        ExtentReportManager.createTest("TC501.2 GET /appointments/slots",
                "Slots query for the seeded doctor");

        Response r = RestAssured.given(ApiSpecs.asUser())
                .queryParam("doctorId", 1)
                .queryParam("date", "2026-06-15")
                .when().get("/appointments/slots");

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertFalse(r.jsonPath().getList("data").isEmpty(), "Expected at least one slot");
    }

    @Test(description = "TC501.3 — HOSPITAL GET /appointments/hospital returns list")
    public void appointmentsHospital_returnsList() {
        ExtentReportManager.createTest("TC501.3 GET /appointments/hospital (HOSPITAL)",
                "Hospital role views appointments scheduled at it");

        Response r = RestAssured.given(ApiSpecs.asHospital()).when().get("/appointments/hospital");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
    }

    @Test(description = "TC501.4 — USER hitting /appointments/hospital gets 403")
    public void appointmentsHospital_asUser_returns403() {
        ExtentReportManager.createTest("TC501.4 /appointments/hospital as USER",
                "Role enforcement");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/appointments/hospital");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }
}
